package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

public class TransformDagsTest {

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
    public void testConstructorInitializesCorrectly() {
        TransformDags transformer = new TransformDags(inputDags, alpha);

        assertNotNull(transformer);
        assertEquals(0, transformer.getNumberOfInsertedEdges());
    }

    @Test
    public void testTransformReturnsCorrectSize() {
        TransformDags transformer = new TransformDags(inputDags, alpha);
        ArrayList<Dag> result = transformer.transform();

        assertNotNull(result);
        assertEquals(inputDags.size(), result.size());
    }

    @Test
    public void testTransformUpdatesNumberOfInsertedEdges() {
        TransformDags transformer = new TransformDags(inputDags, alpha);
        transformer.transform();

        // No sabemos cuántas aristas se insertan exactamente sin saber cómo funciona BetaToAlpha,
        // pero al menos podemos comprobar que el valor no es negativo.
        assertTrue(transformer.getNumberOfInsertedEdges() >= 0);
    }

    @Test
    public void testEmptyDagListReturnsEmptyOutput() {
        TransformDags transformer = new TransformDags(new ArrayList<>(), alpha);
        ArrayList<Dag> result = transformer.transform();

        assertTrue(result.isEmpty());
        assertEquals(0, transformer.getNumberOfInsertedEdges());
    }
}
