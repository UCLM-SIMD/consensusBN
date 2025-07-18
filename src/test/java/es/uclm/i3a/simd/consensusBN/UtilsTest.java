package es.uclm.i3a.simd.consensusBN;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
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

    //find naYX tests

    @Test
    public void testFindNaYX_singleUndirectedCommonNeighbor() {
        Graph graph = new EdgeListGraph();

        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node z = new GraphNode("Z");

        graph.addNode(x);
        graph.addNode(y);
        graph.addNode(z);

        graph.addEdge(new Edge(x, z, Endpoint.TAIL, Endpoint.TAIL)); // undirected
        graph.addEdge(new Edge(y, z, Endpoint.TAIL, Endpoint.TAIL)); // undirected

        List<Node> result = Utils.findNaYX(x, y, graph);
        assertEquals(1, result.size());
        assertTrue(result.contains(z));
    }

    @Test
    public void testFindNaYX_directedEdgeShouldBeExcluded() {
        Graph graph = new EdgeListGraph();

        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node z = new GraphNode("Z");

        graph.addNode(x);
        graph.addNode(y);
        graph.addNode(z);

        graph.addEdge(new Edge(x, z, Endpoint.ARROW, Endpoint.TAIL)); // x → z
        graph.addEdge(new Edge(y, z, Endpoint.ARROW, Endpoint.TAIL)); // y → z

        List<Node> result = Utils.findNaYX(x, y, graph);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindNaYX_mixedNeighbors() {
        Graph graph = new EdgeListGraph();

        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node z1 = new GraphNode("Z1"); // undirected common
        Node z2 = new GraphNode("Z2"); // directed common
        Node z3 = new GraphNode("Z3"); // only adjacent to x

        graph.addNode(x);
        graph.addNode(y);
        graph.addNode(z1);
        graph.addNode(z2);
        graph.addNode(z3);

        // z1: undirected edge with both
        graph.addEdge(new Edge(x, z1, Endpoint.TAIL, Endpoint.TAIL));
        graph.addEdge(new Edge(y, z1, Endpoint.TAIL, Endpoint.TAIL));

        // z2: directed edges with both
        graph.addEdge(new Edge(x, z2, Endpoint.TAIL, Endpoint.ARROW));
        graph.addEdge(new Edge(y, z2, Endpoint.TAIL, Endpoint.ARROW));

        // z3: only adjacent to x
        graph.addEdge(new Edge(x, z3, Endpoint.TAIL, Endpoint.TAIL));

        List<Node> result = Utils.findNaYX(x, y, graph);
        assertEquals(1, result.size());
        assertTrue(result.contains(z1));
        assertFalse(result.contains(z2));
        assertFalse(result.contains(z3));
    }

    @Test
    public void testFindNaYX_multipleUndirectedCommonNeighbors() {
        Graph graph = new EdgeListGraph();

        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");

        graph.addNode(x);
        graph.addNode(y);
        graph.addNode(a);
        graph.addNode(b);

        graph.addEdge(new Edge(x, a, Endpoint.TAIL, Endpoint.TAIL));
        graph.addEdge(new Edge(y, a, Endpoint.TAIL, Endpoint.TAIL));
        graph.addEdge(new Edge(x, b, Endpoint.TAIL, Endpoint.TAIL));
        graph.addEdge(new Edge(y, b, Endpoint.TAIL, Endpoint.TAIL));

        List<Node> result = Utils.findNaYX(x, y, graph);
        assertEquals(2, result.size());
        assertTrue(result.contains(a));
        assertTrue(result.contains(b));
    }

    @Test
    public void testFindNaYX_noCommonNeighbors() {
        Graph graph = new EdgeListGraph();

        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");

        graph.addNode(x);
        graph.addNode(y);
        graph.addNode(a);
        graph.addNode(b);

        graph.addEdge(new Edge(x, a, Endpoint.TAIL, Endpoint.TAIL));
        graph.addEdge(new Edge(y, b, Endpoint.TAIL, Endpoint.TAIL));

        List<Node> result = Utils.findNaYX(x, y, graph);
        assertTrue(result.isEmpty());
    }
}
