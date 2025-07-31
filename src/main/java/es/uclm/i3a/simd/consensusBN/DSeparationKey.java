package es.uclm.i3a.simd.consensusBN;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import edu.cmu.tetrad.graph.Node;

/**
 * This class represents a key for D-separation checks in a Bayesian network.
 * It encapsulates two nodes (x and y) and a set of conditioning nodes.
 * The key is used to efficiently check if two nodes are d-separated given a conditioning set.
 * The equality and hashCode methods ensure that keys with the same nodes and conditioning set are treated as equal.
 */
public class DSeparationKey {
    /**
     * The node x in the D-separation key.
     * This node is one of the two nodes being checked for d-separation.
     */
    private final Node x;

    /**
     * The node y in the D-separation key.
     * This node is the other node being checked for d-separation.
     */
    private final Node y;

    /**
     * The set of conditioning nodes in the D-separation key.
     * This set contains nodes that are conditioned on when checking for d-separation between x and y.
     * It is stored as a defensive copy to ensure immutability.
     */
    private final Set<Node> conditioningSet;

    /**
     * Constructor for DSeparationKey that initializes the key with two nodes and a set of conditioning nodes.
     * The nodes x and y are stored in a consistent order to ensure that the key is symmetric.
     * The conditioning set is stored as a defensive copy to prevent external modifications.
     * @param x the first node in the D-separation key.
     * @param y the second node in the D-separation key.
     * @param conditioningSet the set of conditioning nodes in the D-separation key.
     */
    public DSeparationKey(Node x, Node y, Set<Node> conditioningSet) {
        // Since D-separation is symmetric, we ensure a consistent order for x and y
        if (x.getName().compareTo(y.getName()) <= 0) {
            this.x = x;
            this.y = y;
        } else {
            this.x = y;
            this.y = x;
        }
        this.conditioningSet = new HashSet<>(conditioningSet); // copia defensiva
    }

    /**
     * Checks if this D-separation key is equal to another object.
     * Two keys are considered equal if they have the same nodes (x and y) and
     * the same set of conditioning nodes.
     * The equality is symmetric, meaning the order of x and y does not matter.
     * @param obj the object to compare with this D-separation key.
     * @return true if the other object is a DSeparationKey with the same nodes and conditioning set, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DSeparationKey)) return false;

        DSeparationKey other = (DSeparationKey) obj;
        return y.equals(other.y)
                && x.equals(other.x)
                && conditioningSet.equals(other.conditioningSet);
    }

    /**
     * Returns the hash code for this D-separation key.
     * The hash code is computed based on the nodes x and y, and the conditioning set.
     * This ensures that two keys that are equal will have the same hash code.
     * @return the hash code for this D-separation key.
     */
    @Override
    public int hashCode() {
        return Objects.hash(y, x, conditioningSet);
    }

    /**
     * Returns the node y in the D-separation key.
     * @return the node y in the D-separation key.
     */
    public Node getY() {
        return this.y;
    }

    /**
     * Returns the node x in the D-separation key.
     * @return the node x in the D-separation key.
     */
    public Node getX() {
        return this.x;
    }

    /**
     * Returns the set of conditioning nodes in the D-separation key.
     * This set is unmodifiable to prevent external modifications.
     * @return the set of conditioning nodes in the D-separation key.
     */
    public Set<Node> getConditioningSet() {
        return Collections.unmodifiableSet(this.conditioningSet);
    }


}

