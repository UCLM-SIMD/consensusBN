package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

public class PowerSetTest {

    private List<Node> nodeList;

    @BeforeEach
    public void setUp() {
        ListFabric.MAX_SIZE = Integer.MAX_VALUE; // Reset MAX_SIZE before each test
        nodeList = new ArrayList<>();
        nodeList.add(new GraphNode("X"));
        nodeList.add(new GraphNode("Y"));
        nodeList.add(new GraphNode("Z"));
    }

    @Test
    public void testPowerSetWithMaxSize() {
        PowerSet ps = new PowerSet(nodeList, 2);
        List<Set<Node>> result = new ArrayList<>();
        while (ps.hasMoreElements()) {
            result.add(ps.nextElement());
        }

        // Verifica que no hay subconjuntos de tamaño > 2
        for (Set<Node> subset : result) {
            assertTrue(subset.size() <= 2, "Subset size should be <= 2");
        }

        // Comprobamos algunos subconjuntos esperados
        HashSet<Node> expected1 = new HashSet<>();
        expected1.add(nodeList.get(0));
        HashSet<Node> expected2 = new HashSet<>();
        expected2.add(nodeList.get(0));
        expected2.add(nodeList.get(1));
        assertTrue(result.contains(new HashSet<>(expected1)));
        assertTrue(result.contains(new HashSet<>(expected2)));
    }

    @Test
    public void testPowerSetWithoutMaxSize() {
        PowerSet ps = new PowerSet(nodeList);
        List<Set<Node>> result = new ArrayList<>();
        while (ps.hasMoreElements()) {
            result.add(ps.nextElement());
        }
        // Number of subsets should be 2^n.
        assertEquals(8, result.size());
        
        // Empty set is included in the result
        assertTrue(result.stream().anyMatch(Set::isEmpty));
        
        // Full set is included in the result
        assertTrue(result.contains(new HashSet<>(nodeList)));
    }

    @Test
    public void testResetIndex() {
        PowerSet ps = new PowerSet(nodeList);
        assertTrue(ps.hasMoreElements());
        ps.nextElement();
        ps.resetIndex();
        assertTrue(ps.hasMoreElements());
    }

    @Test
    public void testMaxPowerSetSize() {
        PowerSet powerSet = new PowerSet(nodeList); // Esto actualiza el valor de maxPow
        assertEquals(8L, powerSet.maxPowerSetSize());
    }

    @Test
    public void testMaxSizeIsNegativeShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new PowerSet(nodeList, -1));
    }
}
