package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    public void testConsensusBESConsistency() {
        ConsensusBES consensusBES1 = new ConsensusBES(inputDags);
        consensusBES1.fusion();
        Dag outputDag1 = consensusBES1.getFusion();
        assertNotNull(outputDag1);

        ConsensusBES consensusBES2 = new ConsensusBES(inputDags);
        consensusBES2.fusion2();
        Dag outputDag2 = consensusBES2.getFusion();
        assertNotNull(outputDag2);

        // Check that both outputs are the same
        assertNotNull(outputDag1);
        assertNotNull(outputDag2);
        assertEquals(outputDag1, outputDag2);
        assertEquals(outputDag1.getNodes().size(), outputDag2.getNodes().size());
        assertEquals(outputDag1.getEdges().size(), outputDag2.getEdges().size());

        for (Node node : outputDag1.getNodes()) {
            assert outputDag2.getNodes().contains(node);
        }
        for(Edge edge : outputDag1.getEdges()) {
            assert outputDag2.getEdges().contains(edge);
        }
    }
}
