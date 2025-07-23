package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;

class BackwardEquivalenceSearchDSepTest {


    private ArrayList<Dag> createRandomDagList(int copies) {
        ArrayList<Dag> setOfDags = new ArrayList<>();
        setOfDags.addAll(GraphTestHelper.generateRandomDagList(20, copies, 50, 49, 49, 49, true, 0));
        return setOfDags;
    }

    @Test
    void testApplyBESdDoesNotThrow() {
        // Setting up consensus union
        ArrayList<Dag> initialDags = createRandomDagList(3);
        ConsensusUnion consensusUnion = new ConsensusUnion(initialDags);
        Dag unionDag = consensusUnion.union();
        
        // Running Backward Equivalence Search with d-separation
        BackwardEquivalenceSearchDSep besd = new BackwardEquivalenceSearchDSep(unionDag, initialDags, consensusUnion.getTransformedDags());

        // No exceptions should be thrown during the process
        assertDoesNotThrow(() -> {
            Dag output = besd.applyBackwardEliminationWithDSeparation();
            assertNotNull(output);
        });
    }

    @Test
    void testOutputIsDAG() {
        // Setting up consensus union
        ArrayList<Dag> initialDags = createRandomDagList(3);
        ConsensusUnion consensusUnion = new ConsensusUnion(initialDags);
        Dag unionDag = consensusUnion.union();
        
        // Running Backward Equivalence Search with d-separation
        BackwardEquivalenceSearchDSep besd = new BackwardEquivalenceSearchDSep(unionDag, initialDags, consensusUnion.getTransformedDags());
        Dag outputDag = besd.applyBackwardEliminationWithDSeparation();

        assertTrue(GraphUtils.isDag(outputDag), "El resultado no es un DAG válido.");
    }

    @Test
    void testAristasSePuedenEliminar() {
        // Creamos un grafo donde A -> B, pero en todos los DAGs está A   B (no conectados)
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");

        Dag unionDag = new Dag();
        unionDag.addNode(a);
        unionDag.addNode(b);
        unionDag.addDirectedEdge(a, b);

        // DAGs originales sin esa arista
        Dag dag1 = new Dag();
        dag1.addNode(a);
        dag1.addNode(b);

        Dag dag2 = new Dag();
        dag2.addNode(a);
        dag2.addNode(b);
        // Aquí no hay aristas, A y B están desconectados
        // sin conexión

        ArrayList<Dag> initialDags = new ArrayList<>();
        initialDags.add(dag1);
        initialDags.add(dag2);
        AlphaOrder alphaOrder = new AlphaOrder(initialDags);
        alphaOrder.computeAlpha();
        ArrayList<Dag> transformedDags = (new TransformDags(initialDags, alphaOrder.getOrder())).transform();

        BackwardEquivalenceSearchDSep besd = new BackwardEquivalenceSearchDSep(unionDag, initialDags, transformedDags);
        Dag outputDag = besd.applyBackwardEliminationWithDSeparation();

        // Debe eliminar la arista A -> B por no tener soporte
        Edge deletedEdge = outputDag.getEdge(a, b);
        assertNull(deletedEdge, "La arista A -> B debería haberse eliminado.");
    }

    @Test
    void testGetNumberOfInsertedEdgesReflectsChanges() {
        ArrayList<Dag> initialDags = createRandomDagList(2);
        ConsensusUnion consensusUnion = new ConsensusUnion(initialDags);
        Dag unionDag = consensusUnion.union();
        ArrayList<Dag> transformedDags = consensusUnion.getTransformedDags();
        int insertedEdgesBefore = consensusUnion.getNumberOfInsertedEdges();

        BackwardEquivalenceSearchDSep besd = new BackwardEquivalenceSearchDSep(unionDag, initialDags, transformedDags);
        besd.applyBackwardEliminationWithDSeparation();

        int insertedEdgesAfter = insertedEdgesBefore - besd.getNumberOfRemovedEdges();
        // En el peor de los casos no ha eliminado ninguna, pero nunca debe ser negativo
        assertTrue(insertedEdgesAfter >= 0, "The number of inserted edges should not be negative.");
        assertTrue(insertedEdgesAfter <= insertedEdgesBefore, "The number of inserted edges should decrease after BES.");
    }
}
