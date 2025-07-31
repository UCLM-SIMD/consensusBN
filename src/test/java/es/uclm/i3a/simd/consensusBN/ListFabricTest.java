package es.uclm.i3a.simd.consensusBN;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ListFabricTest {

    @BeforeEach
    public void setUp() {
        // Aseguramos el valor por defecto del maxSize antes de cada test
        ListFabric.MAX_SIZE = 2;
    }

    @Test
    public void testGetList_size3_maxSize2() {
        List<Integer> result = Arrays.stream(ListFabric.generateList(3)).boxed().collect(Collectors.toList());
        List<Integer> expected = Arrays.asList(
            0b000, // []
            0b001, // [C]
            0b010, // [B]
            0b100, // [A]
            0b011, // [B,C]
            0b101, // [A,C]
            0b110  // [A,B]
        );
        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));
    }

    @Test
    public void testGetList_size0() {
        List<Integer> result = Arrays.stream(ListFabric.generateList(0)).boxed().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(0, (int) result.get(0)); // Solo el conjunto vacío
    }

    @Test
    public void testGetList_maxSize0() {
        ListFabric.MAX_SIZE = 0;
        List<Integer> result = Arrays.stream(ListFabric.generateList(3)).boxed().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(0, (int) result.get(0)); // Solo el conjunto vacío
    }

    @Test
    public void testNoSubsetExceedsMaxSize() {
        int size = 4;
        List<Integer> result = Arrays.stream(ListFabric.generateList(size)).boxed().collect(Collectors.toList());
        for (int subset : result) {
            int ones = Integer.bitCount(subset);
            assertTrue(ones <= ListFabric.MAX_SIZE,
                "Subset " + Integer.toBinaryString(subset) + " has " + ones + " bits set");
        }
    }

    @Test
    public void testSymmetry_sizeEqualsMaxSize() {
        int size = 3;
        ListFabric.MAX_SIZE = 3;
        List<Integer> result = Arrays.stream(ListFabric.generateList(size)).boxed().collect(Collectors.toList());
        // All subsets should be included (2^3 = 8)
        assertEquals(8, result.size());
    }
}
