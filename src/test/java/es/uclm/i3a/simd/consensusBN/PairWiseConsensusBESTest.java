package es.uclm.i3a.simd.consensusBN;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
