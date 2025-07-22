package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.tetrad.graph.Node;

/**
 * PowerSet generates all subsets of a given set of nodes, with an optional maximum size constraint.
 * It implements Enumeration to allow iteration over the subsets.
 */
public class PowerSet implements Enumeration<Set<Node>> {
	/**
	 * List of nodes for which the power set is generated.
	 */
	List<Node> nodes;
	/**
	 * List to store the generated subsets.
	 */
	private final List<Set<Node>> subSets = new ArrayList<>();
	/**
	 * Index to track the current position in the enumeration.
	 */
	private int index;
	/**
	 * List of integers representing the subsets in binary form.
	 */
	private int[] binaryList;

	/**
	 * A map to store the subsets with their corresponding binary representation.
	 * The key is the binary representation of the subset, and the value is the subset itself.
	 */
	private HashMap<Integer,Set<Node>> subset;

	/**
	 * Maximum size of the subsets to be generated.
	 * If set to a value less than the number of nodes, it limits the size of the subsets.
	 */
	private static int maxPow = 0;
	
	/**
	 * Builds a PowerSet with subsets of the given nodes, limited to a maximum size.
	 * @param nodes List of nodes to generate subsets from.
	 * @param maxSize Maximum size of the subsets to be generated. Assuring that k does not exceed the number of nodes.
	 * * If maxSize is negative, it will throw an IllegalArgumentException.
	 * @throws IllegalArgumentException if maxSize is negative.
	 
	 */
    public PowerSet(List<Node> nodes, int maxSize) {
		if (maxSize < 0) {
			throw new IllegalArgumentException("maxSize cannot be negative");
		}
        if (maxSize >= nodes.size()) {
            maxSize = nodes.size();
        }
        this.nodes = nodes;
        initializeSubsets(maxSize);
    }

    /**
	 * Builds a PowerSet with all subsets of the given nodes, without size limitation.
     *
     * @param nodes Lista de nodos de entrada.
     */
    public PowerSet(List<Node> nodes) {
        if (nodes.size() > maxPow) {
            maxPow = nodes.size();
        }
        this.nodes = nodes;
        initializeSubsets(nodes.size()); // sin límite: k = nodes.size()
    }

	/**
	 * Initializes the subsets based on the nodes and the maximum size.
	 * This method generates all possible subsets of the nodes, respecting the maximum size constraint.
	 * @param maxSize Maximum size of the subsets to be generated.
	 * If maxSize is greater than the number of nodes, it will generate subsets of all sizes.
	 * If maxSize is 0, it will only generate the empty set.
	 */
	private void initializeSubsets(int maxSize) {
        subset = new HashMap<>();
        index = 0;
        binaryList = ListFabric.generateList(nodes.size());

        for (int i : binaryList) {
            Set<Node> newSubSet = new HashSet<>();
            String selection = Integer.toBinaryString(i);

            for (int j = selection.length() - 1; j >= 0; j--) {
                if (selection.charAt(j) == '1') {
                    int idx = selection.length() - j - 1;
                    newSubSet.add(nodes.get(idx));
                }
            }

            if (newSubSet.size() <= maxSize) {
                subSets.add(newSubSet);
                subset.put(i, newSubSet);
            }
        }
    }

	/**
	 * Checks if there are more subsets to iterate over.
	 * @return true if there are more subsets, false otherwise.
	 */
    @Override
	public boolean hasMoreElements() {
		return index<subSets.size();
	}

	/**
	 * Returns the next subset in the enumeration.
	 * @return The next subset as a Set<Node>.
	 */
	@Override
	public Set<Node> nextElement() {
		return subSets.get(index++);
	}
	
	/**
	 * Resets the index to allow re-iteration over the subsets.
	 * This method allows the enumeration to start over from the beginning.
	 */
	public void resetIndex(){
		this.index = 0;
	}
	
	/**
	 * Returns the maximum size of the power set based on the maximum number of nodes.
	 * This method calculates the size of the power set as 2 raised to the power of the maximum number of nodes.
	 * @return The maximum size of the power set.
	 */
	public static long maxPowerSetSize() {
		return (long) Math.pow(2,maxPow);
	}
}
