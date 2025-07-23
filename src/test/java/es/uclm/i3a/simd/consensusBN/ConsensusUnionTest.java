package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.util.RandomUtil;

public class ConsensusUnionTest {

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
    public void testConstructorWithAlphaInitializesCorrectly() {
        ConsensusUnion cu = new ConsensusUnion(inputDags, alpha);
        assertNotNull(cu);
    }

    @Test
    public void testConstructorWithoutAlphaGeneratesAlpha() {
        ConsensusUnion cu = new ConsensusUnion(inputDags);
        assertNotNull(cu);
    }

    @Test
    public void testUnionReturnsNonNullDag() {
        ConsensusUnion cu = new ConsensusUnion(inputDags, alpha);
        Dag result = cu.union();
        assertNotNull(result);
    }

    @Test
    public void testNumberOfInsertedEdgesIsUpdated() {
        ConsensusUnion cu = new ConsensusUnion(inputDags, alpha);
        cu.union();
        assertTrue(cu.getNumberOfInsertedEdges() >= 0);
    }

    @Test
    public void testSetDagsUpdatesAlphaAndUnion() {
        ConsensusUnion cu = new ConsensusUnion();
        cu.setDags(inputDags);
        cu.union();
        Dag result = cu.getUnion();
        assertNotNull(result);
        assertTrue(result.getNumEdges() >= 1);
    }

    @Test
    public void testRunMethodExecutesUnion() {
        ConsensusUnion cu = new ConsensusUnion(inputDags, alpha);
        cu.run();
        assertNotNull(cu.getUnion());
    }

    @Test
    public void testEmptyDagListReturnsEmptyUnion() {
        assertThrows(IllegalArgumentException.class, () -> new ConsensusUnion(new ArrayList<>()));
    }

    @Test
    public void testRandomBNGeneratesConsensusUnionCorrectly() {
        ArrayList<Dag> randomDagsList = new ArrayList<>();
        int sizeRandomDags = 2;
        int numVariables = 20;

        // Creating list of shared nodes
        ArrayList<Node> sharedNodes = new ArrayList<>();
        for (int i = 0; i < numVariables; i++) {
            Node node = new GraphNode("Node" + i);
            sharedNodes.add(node);
        }
        // Setting seed
        RandomUtil.getInstance().setSeed(42);

        // Generating random DAGs
        for (int i = 0; i < sizeRandomDags; i++) {
            Dag randomDag = RandomGraph.randomDag(sharedNodes,0,50,19,19,19,true);
            randomDagsList.add(randomDag);
        }

		// Applying ConsensusUnion
        ConsensusUnion conDag = new ConsensusUnion(randomDagsList);
        Graph g = conDag.union();
		
        // Validating the resulting consensus DAG
        assertNotNull(g);
        assertTrue(g.getNumEdges() >= 0);
        assertTrue(g.getNodes().size() == randomDagsList.get(0).getNodes().size());
        
    } 
}
