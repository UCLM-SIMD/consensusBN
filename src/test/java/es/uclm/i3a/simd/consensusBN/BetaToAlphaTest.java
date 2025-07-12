package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

public class BetaToAlphaTest {

    private Dag dag;
    private Node a, b, c, d;
    private ArrayList<Node> alphaOrder;

    @BeforeEach
    void setUp() {
        a = new GraphNode("A");
        b = new GraphNode("B");
        c = new GraphNode("C");
        d = new GraphNode("D");

        // DAG: A → B, A → C, B → D, C → D
        dag = new Dag();
        dag.addNode(a);
        dag.addNode(b);
        dag.addNode(c);
        dag.addNode(d);
        dag.addDirectedEdge(a, b);
        dag.addDirectedEdge(a, c);
        dag.addDirectedEdge(b, d);
        dag.addDirectedEdge(c, d);

        // Define an alpha order that requires modifying the graph
        alphaOrder = new ArrayList<>(Arrays.asList(d, c, b, a));
    }

    @Test
    void testTransformRespectsAlphaOrder() {
        BetaToAlpha bta = new BetaToAlpha(dag, alphaOrder);
        bta.transform();

        // El grafo debería haber invertido al menos algunos arcos
        assertTrue(bta.getNumberOfInsertedEdges() > 0);

        // Validamos que el orden resultante es compatible con alpha
        for (Edge edge : dag.getEdges()) {
            Node from = edge.getNode1();
            Node to = edge.getNode2();

            int fromIndex = alphaOrder.indexOf(from);
            int toIndex = alphaOrder.indexOf(to);

            assertTrue(fromIndex < toIndex, "Edge violates alpha order: " + from + " → " + to);
        }
    }

    @Test
    void testRandomAlphaProducesPermutation() {
        BetaToAlpha bta = new BetaToAlpha(dag);
        List<Node> randomAlpha = bta.randomAlfa(new Random(42));

        assertNotNull(randomAlpha);
        assertEquals(dag.getNumNodes(), randomAlpha.size());

        Set<Node> originalNodes = new HashSet<>(dag.getNodes());
        Set<Node> shuffled = new HashSet<>(randomAlpha);

        assertEquals(originalNodes, shuffled); // misma colección, diferente orden
    }

    @Test
    void testComputeAlphaHashBuildsCorrectMap() {
        BetaToAlpha bta = new BetaToAlpha(dag, alphaOrder);
        bta.computeAlphaHash();

        for (int i = 0; i < alphaOrder.size(); i++) {
            Node node = alphaOrder.get(i);
            assertEquals(i, (bta.getAlphaHash()).get(node));
        }
    }
}
