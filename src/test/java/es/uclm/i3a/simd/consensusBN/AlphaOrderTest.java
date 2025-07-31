package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

class AlphaOrderTest {

    private Node a, b, c;
    private Dag dag1, dag2;
    private ArrayList<Dag> dags;

    @BeforeEach
    void setup() {
        a = new GraphNode("A");
        b = new GraphNode("B");
        c = new GraphNode("C");

        // DAG 1: A → B → C
        dag1 = new Dag();
        dag1.addNode(a);
        dag1.addNode(b);
        dag1.addNode(c);
        dag1.addDirectedEdge(a, b);
        dag1.addDirectedEdge(b, c);

        // DAG 2: A → B, A → C
        dag2 = new Dag();
        dag2.addNode(a);
        dag2.addNode(b);
        dag2.addNode(c);
        dag2.addDirectedEdge(a, b);
        dag2.addDirectedEdge(a, c);

        dags = new ArrayList<>(Arrays.asList(dag1, dag2));
    }

    @Test
    void constructorThrowsOnNullInput() { 
        assertThrows(IllegalArgumentException.class, () -> new AlphaOrder(null));
    }

    @Test
    void constructorThrowsOnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new AlphaOrder(new ArrayList<>()));
    }

    @Test
    void constructorThrowsOnSingleDAG() {
        ArrayList<Dag> singleDagList = new ArrayList<>();
        singleDagList.add(dag1);
        assertThrows(IllegalArgumentException.class, () -> new AlphaOrder(singleDagList));
    }

    @Test
    void constructorThrowsOnDifferentNodes() {
        // Crear otro DAG con nodos diferentes
        Dag dagDifferent = new Dag();
        dagDifferent.addNode(new GraphNode("X"));
        dagDifferent.addNode(new GraphNode("Y"));
        dagDifferent.addDirectedEdge(dagDifferent.getNode("X"), dagDifferent.getNode("Y"));

        ArrayList<Dag> badList = new ArrayList<>(Arrays.asList(dag1, dagDifferent));
        assertThrows(IllegalArgumentException.class, () -> new AlphaOrder(badList));
    }

    @Test
    void computeAlphaReturnsValidOrder() {
        AlphaOrder alphaOrder = new AlphaOrder(dags);
        alphaOrder.computeAlpha();
        List<Node> order = alphaOrder.getOrder();

        assertNotNull(order);
        assertEquals(3, order.size());
        assertTrue(order.contains(a));
        assertTrue(order.contains(b));
        assertTrue(order.contains(c));

        // Optional: check for uniqueness
        assertEquals(3, order.stream().distinct().count());
    }

    @Test
    void computeAlphaForTwoSimpleDags(){
        AlphaOrder alphaOrder = new AlphaOrder(dags);
        alphaOrder.computeAlpha();
        List<Node> order = alphaOrder.getOrder();

        // Basic assertions to check the order
        assertNotNull(order);
        assertEquals(3, order.size());
        assertTrue(order.contains(a));
        assertTrue(order.contains(b));
        assertTrue(order.contains(c));

        // Check that the order is A, B, C
        assertEquals(a, order.get(0));
        assertEquals(b, order.get(1));
        assertEquals(c, order.get(2));

        // Check for uniqueness
        assertEquals(3, order.stream().distinct().count());
        
    }
}
  
	

