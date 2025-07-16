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

    private Dag createSimpleDag() {
        // A -> B -> C
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");

        Dag dag = new Dag();
        dag.addNode(a);
        dag.addNode(b);
        dag.addNode(c);

        dag.addDirectedEdge(a, b);
        dag.addDirectedEdge(b, c);

        return dag;
    }

    private ArrayList<Dag> createDagList(int copies) {
        ArrayList<Dag> list = new ArrayList<>();
        for (int i = 0; i < copies; i++) {
            list.add(createSimpleDag());
        }
        return list;
    }

    @Test
    void testApplyBESdDoesNotThrow() {
        Dag unionDag = createSimpleDag();
        ArrayList<Dag> initialDags = createDagList(3);
        ArrayList<Dag> transformedDags = createDagList(3);

        BackwardEquivalenceSearchDSep besd = new BackwardEquivalenceSearchDSep(unionDag, initialDags, transformedDags);

        assertDoesNotThrow(() -> {
            Dag output = besd.applyBackwardEliminationWithDSeparation();
            assertNotNull(output);
        });
    }

    @Test
    void testOutputIsDAG() {
        Dag unionDag = createSimpleDag();
        ArrayList<Dag> initialDags = createDagList(2);
        ArrayList<Dag> transformedDags = createDagList(2);

        BackwardEquivalenceSearchDSep besd = new BackwardEquivalenceSearchDSep(unionDag, initialDags, transformedDags);
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
        // sin conexión

        ArrayList<Dag> initialDags = new ArrayList<>();
        initialDags.add(dag1);
        ArrayList<Dag> transformedDags = new ArrayList<>();
        transformedDags.add(dag1);

        BackwardEquivalenceSearchDSep besd = new BackwardEquivalenceSearchDSep(unionDag, initialDags, transformedDags);
        Dag outputDag = besd.applyBackwardEliminationWithDSeparation();

        // Debe eliminar la arista A -> B por no tener soporte
        Edge deletedEdge = outputDag.getEdge(a, b);
        assertNull(deletedEdge, "La arista A -> B debería haberse eliminado.");
    }

    @Test
    void testGetNumberOfInsertedEdgesReflectsChanges() {
        Dag unionDag = createSimpleDag();
        ArrayList<Dag> dags = createDagList(2);

        BackwardEquivalenceSearchDSep besd = new BackwardEquivalenceSearchDSep(unionDag, dags, dags);
        besd.applyBackwardEliminationWithDSeparation();

        int insertedEdges = besd.getNumberOfInsertedEdges();
        // En el peor de los casos no ha eliminado ninguna, pero nunca debe ser negativo
        assertTrue(insertedEdges >= 0, "El número de aristas insertadas no puede ser negativo.");
    }
}
