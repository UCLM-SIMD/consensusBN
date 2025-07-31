package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;

public class HeuristicConsensusMVotingTest {

    private Dag createSimpleDag(String from, String to) {
        Node n1 = new GraphNode(from);
        Node n2 = new GraphNode(to);
        Dag dag = new Dag(Arrays.asList(n1, n2));
        dag.addDirectedEdge(n1, n2);
        return dag;
    }

    @Test
    public void testFusionCreatesDAGWithExpectedEdges() {
        Dag dag1 = createSimpleDag("A", "B");
        Dag dag2 = createSimpleDag("A", "B");
        ArrayList<Dag> dags = new ArrayList<>(Arrays.asList(dag1, dag2));

        HeuristicConsensusMVoting mvoting = new HeuristicConsensusMVoting(dags, 0.5);
        Dag consensus = mvoting.fusion();

        assertNotNull(consensus);
        assertTrue(GraphUtils.isDag(consensus));
        assertEquals(2, consensus.getNumNodes());
        assertEquals(1, consensus.getNumEdges());
        assertTrue(consensus.isParentOf(getNodeByName(consensus, "A"), getNodeByName(consensus, "B")));

        // Test getters
        assertEquals(0.5, mvoting.getPercentage());
        assertEquals(2, mvoting.getVariables().size());
        assertEquals(2, mvoting.getWeight().length);
        assertEquals(2, mvoting.getWeight()[0].length);
        assertEquals(2, mvoting.getWeight()[1].length);
        assertEquals(0.0, mvoting.getWeight()[0][1]);
        assertEquals(0.0, mvoting.getWeight()[1][0]);
        assertEquals(dags, mvoting.getSetOfdags());
        assertEquals(consensus, mvoting.getOutputDag());
        
    }

    @Test
    public void testFusionDoesNotAddLowWeightEdges() {
        Dag dag1 = createSimpleDag("A", "B");
        Dag dag2 = createSimpleDag("B", "A"); // Conflicting direction

        ArrayList<Dag> dags = new ArrayList<>(Arrays.asList(dag1, dag2));
        HeuristicConsensusMVoting mvoting = new HeuristicConsensusMVoting(dags, 0.75);
        Dag consensus = mvoting.fusion();

        // Expect no edge because weight is 0.5 < 0.75
        assertEquals(0, consensus.getNumEdges());
    }

    @Test
    public void testFusionDoesNotCreateCycle() {
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");

        Dag dag1 = new Dag(Arrays.asList(a, b, c));
        dag1.addDirectedEdge(a, b);
        dag1.addDirectedEdge(b, c);

        Dag dag2 = new Dag(Arrays.asList(a, b, c));
        dag2.addDirectedEdge(a, b);
        dag2.addDirectedEdge(b, c);

        ArrayList<Dag> dags = new ArrayList<>(Arrays.asList(dag1, dag2));
        HeuristicConsensusMVoting mvoting = new HeuristicConsensusMVoting(dags, 0.5);
        Dag consensus = mvoting.fusion();

        assertTrue(GraphUtils.isDag(consensus), "La fusión no debe crear ciclos.");
    }

    @Test
    public void testWeightMatrixCorrectlyComputed() {
        Dag dag1 = createSimpleDag("A", "B");
        Dag dag2 = createSimpleDag("A", "B");
        ArrayList<Dag> dags = new ArrayList<>(Arrays.asList(dag1, dag2));

        HeuristicConsensusMVoting mvoting = new HeuristicConsensusMVoting(dags, 0.5);

        int indexA = mvoting.getVariables().indexOf(new GraphNode("A"));
        int indexB = mvoting.getVariables().indexOf(new GraphNode("B"));

        double weightAB = mvoting.getWeight()[indexA][indexB];
        double expectedWeight = 1.0; // Dos DAGS, misma dirección

        assertEquals(expectedWeight, weightAB, 1e-6);
    }

    private Node getNodeByName(Dag dag, String name) {
        return dag.getNodes().stream()
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + name));
    }
}
