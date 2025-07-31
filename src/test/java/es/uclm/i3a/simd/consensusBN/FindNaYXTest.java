package es.uclm.i3a.simd.consensusBN;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;


public class FindNaYXTest {
    
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
