package es.uclm.i3a.simd.consensusBN;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

public class PairWiseConsensusBESTest {

    private Node A, B, C;
    private Dag dag1, dag2;

    @BeforeEach
    public void setup() {
        A = new GraphNode("A");
        B = new GraphNode("B");
        C = new GraphNode("C");

        dag1 = new Dag();
        dag1.addNode(A);
        dag1.addNode(B);
        dag1.addNode(C);
        dag1.addDirectedEdge(A, B);
        dag1.addDirectedEdge(B, C);

        dag2 = new Dag();
        dag2.addNode(A);
        dag2.addNode(B);
        dag2.addNode(C);
        dag2.addDirectedEdge(A, C);
        dag2.addDirectedEdge(C, B);
    }

    @Test
    public void testFusionCreatesNonNullDag() {
        PairWiseConsensusBES pwc = new PairWiseConsensusBES(dag1, dag2);
        pwc.fusion();

        Dag fusion = pwc.getDagFusion();
        assertNotNull(fusion, "The fusion DAG should not be null");
    }

    @Test
    public void testGetNumberOfInsertedEdges() {
        PairWiseConsensusBES pwc = new PairWiseConsensusBES(dag1, dag2);
        pwc.fusion();

        int inserted = pwc.getNumberOfInsertedEdges();
        assertTrue(inserted >= 0, "Inserted edges should be >= 0");
    }

    @Test
    public void testGetNumberOfUnionEdges() {
        PairWiseConsensusBES pwc = new PairWiseConsensusBES(dag1, dag2);
        pwc.fusion();

        PairWiseConsensusBES samePwc = new PairWiseConsensusBES(dag1, dag1);
        samePwc.fusion();

        int unionEdges = pwc.getNumberOfUnionEdges();
        assertTrue(unionEdges > 0, "Union should contain some edges");

        int sameUnionEdges = samePwc.getNumberOfUnionEdges();
        assertTrue(sameUnionEdges == dag1.getNumEdges(), "Union edges should match the number of edges in a single DAG");
    }

    @Test
    public void testGetHammingDistance() {
        PairWiseConsensusBES pwc = new PairWiseConsensusBES(dag1, dag2);
        int distance = pwc.calculateHammingDistance();
        PairWiseConsensusBES samePwc = new PairWiseConsensusBES(dag1, dag1);
        int sameDistance = samePwc.calculateHammingDistance();

        assertTrue(distance >= 0, "Hamming distance should be >= 0");
        assertTrue(sameDistance == 0, "Hamming distance for identical DAGs should be 0");
    }

    @Test
    public void testRunCallsGetFusion() {
        PairWiseConsensusBES pwc = new PairWiseConsensusBES(dag1, dag2);
        pwc.run();

        assertNotNull(pwc.getDagFusion(), "Fusion DAG should be created after run()");
        assertTrue(pwc.getNumberOfUnionEdges() > 0, "Union edges should be computed");
    }

    @Test
    public void testFusionIsConsistentWithInput() {
        PairWiseConsensusBES pwc = new PairWiseConsensusBES(dag1, dag2);
        pwc.fusion();
        Dag fusion = pwc.getDagFusion();

        Set<Edge> edges = fusion.getEdges();
        assertNotNull(edges);
        assertFalse(edges.isEmpty(), "Fusion DAG should contain edges");
    }

    // Exception throws test cases

    @Test
    public void testNullDags() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> new PairWiseConsensusBES(null, createValidDag()));
        assertEquals("Input DAGs cannot be null.", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> new PairWiseConsensusBES(createValidDag(), null));
        assertEquals("Input DAGs cannot be null.", ex2.getMessage());
    }

    @Test
    public void testEmptyNodes() {
        Dag dag1 = new Dag(); // no nodes
        Dag dag2 = createValidDag();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new PairWiseConsensusBES(dag1, dag2));
        assertEquals("Input DAGs must contain at least one node.", ex.getMessage());
    }

    @Test
    public void testEmptyEdges() {
        Node n1 = new GraphNode("X");
        Node n2 = new GraphNode("Y");
        Dag dag1 = new Dag(Arrays.asList(n1, n2)); // 2 nodes, no edges
        Dag dag2 = createValidDag(); // tiene al menos un edge

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new PairWiseConsensusBES(dag1, dag2));
        assertEquals("Input DAGs must contain at least one edge.", ex.getMessage());
    }

    @Test
    public void testDifferentNodeCounts() {
        Dag dag1 = createValidDag();
        Dag dag2 = createValidDag();
        dag2.addNode(new GraphNode("Extra"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new PairWiseConsensusBES(dag1, dag2));
        assertEquals("Input DAGs must have the same number of nodes.", ex.getMessage());
    }

    @Test
    public void testDifferentNodeSets() {
        Node n1 = new GraphNode("A");
        Node n2 = new GraphNode("B");
        Node n3 = new GraphNode("C");

        Dag dag1 = new Dag(Arrays.asList(n1, n2));
        dag1.addDirectedEdge(n1, n2);

        Dag dag2 = new Dag(Arrays.asList(n1, n3)); // C en vez de B
        dag2.addDirectedEdge(n1, n3);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new PairWiseConsensusBES(dag1, dag2));
        assertEquals("Input DAGs must have the same set of nodes.", ex.getMessage());
    }

    // Helper para crear un DAG válido
    private Dag createValidDag() {
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");

        Dag dag = new Dag(Arrays.asList(a, b));
        dag.addDirectedEdge(a, b);
        return dag;
    }
}
