package es.uclm.i3a.simd.consensusBN;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import edu.cmu.tetrad.graph.Node;

public class DSeparationKey {
    private final Node y;
    private final Node x;
    private final Set<Node> conditioningSet;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DSeparationKey)) return false;

        DSeparationKey other = (DSeparationKey) obj;
        return y.equals(other.y)
                && x.equals(other.x)
                && conditioningSet.equals(other.conditioningSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(y, x, conditioningSet);
    }


    public Node getY() {
        return this.y;
    }


    public Node getX() {
        return this.x;
    }


    public Set<Node> getConditioningSet() {
        return Collections.unmodifiableSet(this.conditioningSet);
    }


}

