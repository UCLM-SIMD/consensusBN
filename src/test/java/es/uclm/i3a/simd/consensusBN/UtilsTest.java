package es.uclm.i3a.simd.consensusBN;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;


public class UtilsTest {
     // d-separation tests

    private Node node(String name) {
        return new GraphNode(name);
    }

    private Dag createDag(Edge... edges) {
        Dag dag = new Dag();
        for (Edge edge : edges) {
            dag.addNode(edge.getNode1());
            dag.addNode(edge.getNode2());
            dag.addDirectedEdge(edge.getNode1(), edge.getNode2());
        }
        return dag;
    }

    @Test
    public void testDirectConnection() {
        Node A = node("A"), B = node("B");
        Dag dag = createDag(Edges.directedEdge(A, B));
        List<Node> conditioning = Collections.emptyList();
        assertFalse(Utils.dSeparated(dag, A, B, conditioning));
    }

    @Test
    public void testChainNoCondition() {
        Node A = node("A"), B = node("B"), C = node("C");
        Dag dag = createDag(Edges.directedEdge(A, B), Edges.directedEdge(B, C));
        List<Node> conditioning = Collections.emptyList();
        assertFalse(Utils.dSeparated(dag, A, C, conditioning));
    }

    @Test
    public void testChainWithCondition() {
        Node A = node("A"), B = node("B"), C = node("C");
        Dag dag = createDag(Edges.directedEdge(A, B), Edges.directedEdge(B, C));
        
        assertTrue(Utils.dSeparated(dag, A, C, Collections.singletonList(B)));
    }

    @Test
    public void testColliderNoCondition() {
        Node A = node("A"), B = node("B"), C = node("C");
        Dag dag = createDag(Edges.directedEdge(A, B), Edges.directedEdge(C, B));
        List<Node> conditioning = Collections.emptyList();
        assertTrue(Utils.dSeparated(dag, A, C, conditioning));
    }

    @Test
    public void testColliderConditionedOnCollider() {
        Node A = node("A"), B = node("B"), C = node("C");
        Dag dag = createDag(Edges.directedEdge(A, B), Edges.directedEdge(C, B));

        assertFalse(Utils.dSeparated(dag, A, C, Collections.singletonList(B)));
    }

    @Test
    public void testDivergingNoCondition() {
        Node A = node("A"), B = node("B"), C = node("C");
        Dag dag = createDag(Edges.directedEdge(B, A), Edges.directedEdge(B, C));
        List<Node> conditioning = Collections.emptyList();
        assertFalse(Utils.dSeparated(dag, A, C, conditioning));
    }

    @Test
    public void testDivergingConditionedOnCommonParent() {
        Node A = node("A"), B = node("B"), C = node("C");
        Dag dag = createDag(Edges.directedEdge(B, A), Edges.directedEdge(B, C));

        assertTrue(Utils.dSeparated(dag, A, C, Collections.singletonList(B)));
    }

    @Test
    public void testColliderConditionedOnDescendant() {
        Node A = node("A"), B = node("B"), C = node("C"), D = node("D");
        Dag dag = createDag(
            Edges.directedEdge(A, B),
            Edges.directedEdge(C, B),
            Edges.directedEdge(B, D)
        );

        assertFalse(Utils.dSeparated(dag, A, C, Collections.singletonList(D)));
    }

}
