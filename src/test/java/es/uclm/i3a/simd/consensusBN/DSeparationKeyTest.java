package es.uclm.i3a.simd.consensusBN;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

class DSeparationKeyTest {

    private final Node X = new GraphNode("X");
    private final Node Y = new GraphNode("Y");
    private final Node Z = new GraphNode("Z");
    private final Node W = new GraphNode("W");

    @Test
    void testConstructorAndGetters() {
        Set<Node> zSet = new HashSet<>(Arrays.asList(Z, W));
        DSeparationKey key = new DSeparationKey(X, Y, zSet);

        assertEquals(X, key.getX());
        assertEquals(Y, key.getY());
        assertEquals(new HashSet<>(Arrays.asList(Z, W)), key.getConditioningSet());
    }

    @Test
    void testEqualsSameContentDifferentOrder() {
        Set<Node> zSet1 = new HashSet<>(Arrays.asList(Z, W));
        Set<Node> zSet2 = new HashSet<>(Arrays.asList(W, Z));

        DSeparationKey key1 = new DSeparationKey(X, Y, zSet1);
        DSeparationKey key2 = new DSeparationKey(X, Y, zSet2);

        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    void testNotEqualsDifferentZ() {
        Set<Node> zSet1 = new HashSet<>(Collections.singletonList(Z));
        Set<Node> zSet2 = new HashSet<>(Arrays.asList(Z, W));

        DSeparationKey key1 = new DSeparationKey(X, Y, zSet1);
        DSeparationKey key2 = new DSeparationKey(X, Y, zSet2);

        assertNotEquals(key1, key2);
    }

    @Test
    void testSimmetryBetweenXandY() {
        DSeparationKey key1 = new DSeparationKey(X, Y, Collections.emptySet());
        DSeparationKey key2 = new DSeparationKey(Y, X, Collections.emptySet());

        assertEquals(key1, key2);
    }

    @Test
    void testEqualsSelf() {
        DSeparationKey key = new DSeparationKey(X, Y, Collections.singleton(Z));
        assertEquals(key, key);
    }

    @Test
    void testNotEqualsNullOrDifferentClass() {
        DSeparationKey key = new DSeparationKey(X, Y, Collections.singleton(Z));

        assertNotEquals(null, key);
        assertNotEquals("NotAKey", key);
    }

    @Test
    void testKeyAsMapKey() {
        DSeparationKey key1 = new DSeparationKey(X, Y, new HashSet<>(Arrays.asList(Z, W)));
        DSeparationKey key2 = new DSeparationKey(X, Y, new HashSet<>(Arrays.asList(W, Z)));

        Map<DSeparationKey, Boolean> map = new HashMap<>();
        map.put(key1, true);

        assertTrue(map.containsKey(key2));
        assertTrue(map.get(key2));
    }
}
