package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

public class ConsensusBESTest {
    private ArrayList<Dag> inputDags;
    private ArrayList<Node> alpha;

    @BeforeEach
    public void setUp() {
        inputDags = new ArrayList<>();
        
        // We use 4 nodes for the DAGs
        Node nodeA = new GraphNode("A");
        Node nodeB = new GraphNode("B");
        Node nodeC = new GraphNode("C");
        Node nodeD = new GraphNode("D");

        // Create first DAG with these edges: A -> B, A -> C, B -> D, C -> D
        Dag dag1 = new Dag();
        dag1.addNode(nodeA);
        dag1.addNode(nodeB);
        dag1.addNode(nodeC);
        dag1.addNode(nodeD);

        // Adding directed edges to the DAG
        dag1.addDirectedEdge(nodeA, nodeB);
        dag1.addDirectedEdge(nodeA, nodeC);
        dag1.addDirectedEdge(nodeB, nodeD);
        dag1.addDirectedEdge(nodeC, nodeD);

        // Adding the DAG to the list
        inputDags.add(dag1);

        // Create second DAG with these edges: D -> C, D -> B, C -> A, B -> A
        Dag dag2 = new Dag();
        dag2.addNode(nodeA);
        dag2.addNode(nodeB);
        dag2.addNode(nodeC);
        dag2.addNode(nodeD);

        // Adding directed edges to the second DAG
        dag2.addDirectedEdge(nodeD, nodeC);
        dag2.addDirectedEdge(nodeD, nodeB);
        dag2.addDirectedEdge(nodeC, nodeA);
        dag2.addDirectedEdge(nodeB, nodeA);

        // Adding the second DAG to the list
        inputDags.add(dag2);

        // Apply AlphaOrder algorithm to these dags:
        AlphaOrder alphaOrder = new AlphaOrder(inputDags);
        alphaOrder.computeAlpha();
        alpha = alphaOrder.getOrder();

    }

    @Test
    public void testConsensusUnionConsistency() {
        ConsensusUnion cu = new ConsensusUnion(inputDags, alpha);
        Dag expected = cu.union();
        assertNotNull(cu);
        assertNotNull(expected);
        
        ConsensusBES consensusBES = new ConsensusBES(inputDags);
        consensusBES.consensusUnion();

        // Check if the union DAG is not null and has nodes
        Dag unionDag = consensusBES.getUnion();
        assertNotNull(unionDag);
        assertNotNull(unionDag.getNodes());

        // Check that the union DAG is equal to the expected DAG
        assertNotNull(unionDag.getNodes());
        assertNotNull(expected.getNodes());
        assertEquals(expected, unionDag);
        assertEquals(expected.getNodes().size(), unionDag.getNodes().size());
        assertEquals(expected.getEdges().size(), unionDag.getEdges().size());
        
        for (Node node : expected.getNodes()) {
            assert unionDag.getNodes().contains(node);
        }
        for(Edge edge : expected.getEdges()) {
            assert unionDag.getEdges().contains(edge);
        }
    }

    @Test
    public void testRandomBNFusion(){
        // (seed, n. variables, n egdes max, n.dags, mutation(n. de operaciones))
		RandomBN setOfDags = new RandomBN(0, 20, 50,
				4,3);
		setOfDags.setMaxInDegree(4);
		setOfDags.setMaxOutDegree(4);
		setOfDags.generate();

    	ConsensusBES conDag = new ConsensusBES(setOfDags.setOfRandomDags);
    	conDag.fusion();
    	Dag besDag = conDag.getFusion();
        Dag unionDag = conDag.getUnion();
        ConsensusUnion consensusUnion = conDag.getConsensusUnion();
        int totalNumberOfInsertedEdges = conDag.getNumberOfInsertedEdges();
        int consensusNumberOfInsertedEdges = consensusUnion.getNumberOfInsertedEdges();

        assertNotNull(besDag);
        assertNotNull(unionDag);
        assertNotNull(consensusUnion);
        assertEquals(besDag.getNodes().size(), unionDag.getNodes().size());
        assert consensusNumberOfInsertedEdges >= 0;
        assert consensusNumberOfInsertedEdges >= totalNumberOfInsertedEdges;        
    }


    @Test
    void testFusionProducesDag() {
        ConsensusBES fusionAlgorithm = new ConsensusBES(inputDags);
        fusionAlgorithm.fusion();

        Dag result = fusionAlgorithm.getFusion();
        assertNotNull(result, "El DAG de salida no debe ser null.");
        assertFalse(result.paths().existsDirectedCycle(), "El DAG resultante no debe tener ciclos.");
    }

    @Test
    void testEdgeInsertionCountIsCorrectlyComputed() {
        ConsensusBES fusionAlgorithm = new ConsensusBES(inputDags);
        fusionAlgorithm.fusion();

        int insertedEdges = fusionAlgorithm.getNumberOfInsertedEdges();
        assertTrue(insertedEdges >= 0, "El número de aristas insertadas debe ser >= 0.");
    }

    @Test
    void testFusionOrderIsValid() {
        ConsensusBES fusionAlgorithm = new ConsensusBES(inputDags);
        fusionAlgorithm.fusion();

        List<Node> order = fusionAlgorithm.getOrderFusion();
        assertNotNull(order, "El orden de fusión no debe ser null.");
        assertEquals(4, order.size(), "El orden de fusión debe tener 3 nodos.");
    }

    @Test
    void testTransformedDagsAreAccessibleAfterFusion() {
        ConsensusBES fusionAlgorithm = new ConsensusBES(inputDags);
        fusionAlgorithm.fusion();

        ArrayList<Dag> transformed = fusionAlgorithm.getTransformedDags();
        assertEquals(2, transformed.size(), "Debe haber 2 DAGs transformados.");
    }

    @Test
    void testGetTransformedDagsWithoutFusionThrowsException() {
        ConsensusBES fusionAlgorithm = new ConsensusBES(inputDags);

        assertThrows(IllegalStateException.class, fusionAlgorithm::getTransformedDags,
                "Debe lanzar una excepción si se accede a los DAGs transformados sin llamar a fusion().");
    }

    @Test
    void testThreadExecutionWithRunMethod() {
        ConsensusBES fusionAlgorithm = new ConsensusBES(inputDags);
        Thread thread = new Thread(fusionAlgorithm);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail("El hilo fue interrumpido.");
        }

        assertNotNull(fusionAlgorithm.getFusion(), "El DAG resultante debe existir tras ejecutar run().");
    }

}
