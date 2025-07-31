package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
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

public class DseparationTest {
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

    @Test
    public void testSimmetryBetweenXandY() {
        Node A = node("A"), B = node("B"), C = node("C"), D = node("D");
        Dag dag = createDag(Edges.directedEdge(A, B), Edges.directedEdge(C, A), Edges.directedEdge(D, A));
        List<Node> conditioning = Collections.emptyList();
        
        // No colliders between A and B, so they are not d-separated
        assertFalse(Utils.dSeparated(dag, A, B, conditioning));
        assertFalse(Utils.dSeparated(dag, B, A, conditioning));

        // C->A and D->A makes A a collider for C and D, and therefore C and D are d-separated from each other
        assertTrue(Utils.dSeparated(dag, C, D, conditioning));
        assertTrue(Utils.dSeparated(dag, D, C, conditioning));
        
    }

    @Test
    public void testDseparationMethodsAreEquivalent() {
        Node A = node("A"), B = node("B"), C = node("C");
        Dag dag = createDag(Edges.directedEdge(A, B), Edges.directedEdge(B, C));
        
        // Using the dSeparated method with no conditioning
        assertFalse(Utils.dSeparated(dag, A, C));
        
        // Using the dSeparated method with an empty conditioning set
        assertFalse(Utils.dSeparated(dag, A, C, Collections.emptyList()));
    }

    @Test
    public void testDseparationRule1(){
        // Scenarios taken from https://yuyangyy.medium.com/understand-d-separation-471f9aada503
        // Scenario 1: Z is empty, namely, we don't dondition on any variables
        // Rule 1: If there exists a path from x to y and there is no collider on the path, then x and y are not d-separated.
        Node x = new GraphNode("x");
        Node r = new GraphNode("r");
        Node s = new GraphNode("s");
        Node t = new GraphNode("t");
        Node u = new GraphNode("u");
        Node v = new GraphNode("v");
        Node y = new GraphNode("y");

        Dag dag = new Dag();
        dag.addNode(x);
        dag.addNode(r);
        dag.addNode(s);
        dag.addNode(t);
        dag.addNode(u);
        dag.addNode(v);
        dag.addNode(y);

        // Edges: x -> r -> s -> t <- u <- v -> y
        dag.addDirectedEdge(x, r);
        dag.addDirectedEdge(r, s);
        dag.addDirectedEdge(s, t);
        dag.addDirectedEdge(u, t);
        dag.addDirectedEdge(v, u);
        dag.addDirectedEdge(v, y);

        // Check d-separations
        assertFalse(Utils.dSeparated(dag, x, r));
        assertFalse(Utils.dSeparated(dag, x, s));
        assertFalse(Utils.dSeparated(dag, x, t));
        
        assertTrue(Utils.dSeparated(dag, x, u));
        assertTrue(Utils.dSeparated(dag, x, v));
        assertTrue(Utils.dSeparated(dag, x, y));

        assertFalse(Utils.dSeparated(dag, u, v));
        assertFalse(Utils.dSeparated(dag, u, y));
    }

    public void testDseparationRule2(){
        // Scenarios taken from https://yuyangyy.medium.com/understand-d-separation-471f9aada503
        // Scenario 2: Z is non-empty, and the colliders don't belong to Z or have no children in Z.
        // Rule 2: If there exists a path from x to y and none of the nodes on the path belongs to Z, then x and y are not d-separated.
        Node x = new GraphNode("x");
        Node r = new GraphNode("r");
        Node s = new GraphNode("s");
        Node t = new GraphNode("t");
        Node u = new GraphNode("u");
        Node v = new GraphNode("v");
        Node y = new GraphNode("y");

        Dag dag = new Dag();
        dag.addNode(x);
        dag.addNode(r);
        dag.addNode(s);
        dag.addNode(t);
        dag.addNode(u);
        dag.addNode(v);
        dag.addNode(y);

        // Edges: x -> r -> s -> t <- u <- v -> y
        dag.addDirectedEdge(x, r);
        dag.addDirectedEdge(r, s);
        dag.addDirectedEdge(s, t);
        dag.addDirectedEdge(u, t);
        dag.addDirectedEdge(v, u);
        dag.addDirectedEdge(v, y);

        // Creating a conditioning set Z that does not include any colliders
        List<Node> Z = new ArrayList<>();
        Z.add(r);
        Z.add(v);

        // Check d-separations
        // Node x
        assertTrue(Utils.dSeparated(dag, x, r, Z));
        assertTrue(Utils.dSeparated(dag, x, s, Z));
        assertTrue(Utils.dSeparated(dag, x, t, Z));
        assertTrue(Utils.dSeparated(dag, x, u, Z));
        assertTrue(Utils.dSeparated(dag, x, v, Z));
        assertTrue(Utils.dSeparated(dag, x, y, Z));

        // Node r
        assertTrue(Utils.dSeparated(dag, r, s, Z));
        assertTrue(Utils.dSeparated(dag, r, t, Z));
        assertTrue(Utils.dSeparated(dag, r, u, Z));
        assertTrue(Utils.dSeparated(dag, r, v, Z));
        assertTrue(Utils.dSeparated(dag, r, y, Z));

        // Node s
        assertFalse(Utils.dSeparated(dag, s, t, Z)); // No node on the path belongs to Z, so d-separated
        assertTrue(Utils.dSeparated(dag, s, u, Z));
        assertTrue(Utils.dSeparated(dag, s, v, Z));
        assertTrue(Utils.dSeparated(dag, s, y, Z));

        // Node t
        assertFalse(Utils.dSeparated(dag, t, u, Z)); // No node on the path belongs to Z, so d-separated
        assertTrue(Utils.dSeparated(dag, t, v, Z));
        assertTrue(Utils.dSeparated(dag, t, y, Z));

        // Node u
        assertTrue(Utils.dSeparated(dag, u, v, Z));
        assertTrue(Utils.dSeparated(dag, u, y, Z));

        // Node v
        assertTrue(Utils.dSeparated(dag, v, y, Z));


    }

    @Test
    public void testDseparationRule3(){
        // Scenarios taken from https://yuyangyy.medium.com/understand-d-separation-471f9aada503
        // Scenario 3: Z is non-empty, and there are colliders either inside Z or have children in Z.
        // Rule 3: For colliders that fall inside Z or have children in Z, they are no longer seen as colliders.
    
        Node x = new GraphNode("x");
        Node r = new GraphNode("r");
        Node s = new GraphNode("s");
        Node t = new GraphNode("t");
        Node u = new GraphNode("u");
        Node v = new GraphNode("v");
        Node y = new GraphNode("y");
        Node w = new GraphNode("w");
        Node p = new GraphNode("p");
        Node q = new GraphNode("q");

        Dag dag = new Dag();
        dag.addNode(x);
        dag.addNode(r);
        dag.addNode(s);
        dag.addNode(t);
        dag.addNode(u);
        dag.addNode(v);
        dag.addNode(y);
        dag.addNode(w);
        dag.addNode(p);
        dag.addNode(q);

        // Edges: x -> r -> s -> t <- u <- v -> y + r->w, t->p, v->q
        dag.addDirectedEdge(x, r);
        dag.addDirectedEdge(r, s);
        dag.addDirectedEdge(s, t);
        dag.addDirectedEdge(u, t);
        dag.addDirectedEdge(v, u);
        dag.addDirectedEdge(v, y);
        dag.addDirectedEdge(r, w);
        dag.addDirectedEdge(t, p);
        dag.addDirectedEdge(v, q);

        // Creating a conditioning set Z that includes colliders and their children
        List<Node> Z = new ArrayList<>();
        Z.add(r);
        Z.add(p);

        // Check d-separations
        // Node x
        assertTrue(Utils.dSeparated(dag, x, s, Z));
        assertTrue(Utils.dSeparated(dag, x, t, Z));
        assertTrue(Utils.dSeparated(dag, x, u, Z));
        assertTrue(Utils.dSeparated(dag, x, v, Z));
        assertTrue(Utils.dSeparated(dag, x, y, Z));
        assertTrue(Utils.dSeparated(dag, x, w, Z));
        assertTrue(Utils.dSeparated(dag, x, q, Z));

        // Node w
        assertTrue(Utils.dSeparated(dag, w, s, Z));
        assertTrue(Utils.dSeparated(dag, w, t, Z));
        assertTrue(Utils.dSeparated(dag, w, u, Z));
        assertTrue(Utils.dSeparated(dag, w, v, Z));
        assertTrue(Utils.dSeparated(dag, w, y, Z));
        assertTrue(Utils.dSeparated(dag, w, q, Z));

        // Node s
        assertFalse(Utils.dSeparated(dag, s, t, Z));
        assertFalse(Utils.dSeparated(dag, s, u, Z));
        assertFalse(Utils.dSeparated(dag, s, v, Z));
        assertFalse(Utils.dSeparated(dag, s, q, Z));
        assertFalse(Utils.dSeparated(dag, s, y, Z));

        // Node t
        assertFalse(Utils.dSeparated(dag, t, u, Z));
        assertFalse(Utils.dSeparated(dag, t, v, Z));
        assertFalse(Utils.dSeparated(dag, t, y, Z));
        assertFalse(Utils.dSeparated(dag, t, q, Z));
        
        // Node u
        assertFalse(Utils.dSeparated(dag, u, v, Z));
        assertFalse(Utils.dSeparated(dag, u, y, Z));
        assertFalse(Utils.dSeparated(dag, u, q, Z));

        // Node v
        assertFalse(Utils.dSeparated(dag, v, y, Z));
        assertFalse(Utils.dSeparated(dag, v, q, Z));
        
        // Node y
        assertFalse(Utils.dSeparated(dag, y, q, Z));

    }
}
