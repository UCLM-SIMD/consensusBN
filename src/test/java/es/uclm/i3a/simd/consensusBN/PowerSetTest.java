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

        // Número de subconjuntos debería ser 2^n
        assertEquals(7, result.size());

        // El conjunto vacío debería estar incluido
        assertTrue(result.contains(new HashSet<Node>()));

        // El conjunto completo no está incluido en el resultado
        assertTrue(!result.contains(new HashSet<>(nodeList)));
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
    public void testMaxPowerSetSizeStatic() {
        new PowerSet(nodeList); // Esto actualiza el valor de maxPow
        assertEquals(8L, PowerSet.maxPowerSetSize());
    }

    @Test
    public void testMaxSizeIsNegativeShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new PowerSet(nodeList, -1));
    }
}
