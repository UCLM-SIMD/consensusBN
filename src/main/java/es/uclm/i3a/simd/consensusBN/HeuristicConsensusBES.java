package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;

import edu.cmu.tetrad.graph.Dag;

/**
 * The {@code HeuristicConsensusBES} class extends the {@code ConsensusBES} class
 * to implement a heuristic approach for backward equivalence search in directed acyclic graphs (DAGs).
 * <p>
 * This class is designed to perform a consensus structure learning algorithm
 * by applying a heuristic method for backward equivalence search (BES) with D-separation.
 * It allows for a more efficient search by limiting the size of the conditioning set and applying a
 * percentage threshold for determining d-separation between nodes.
 * <p>
 * The constructor initializes the class with a list of input DAGs, a maximum size for the
 * conditioning set, and a percentage threshold for d-separation.
 * The `fusion` method applies the consensus union to compute a consensus DAG from the input DAGs,
 * and then applies the backward equivalence search with D-separation with the specified parameters to refine the graph.
 */
public class HeuristicConsensusBES extends ConsensusBES{

    private final int maxSize;
    private final double percentage;

    /**
     * Constructor for HeuristicConsensusBES.
     * This class extends ConsensusBES to implement a heuristic approach
     * for backward equivalence search in directed acyclic graphs (DAGs).
     * 
     * @param dags the list of input DAGs to be fused.
     * @param maxSize the maximum size of the conditioning set for d-separation checks.
     * @param percentage the percentage/threshold for determining d-separation between nodes.
     */
    public HeuristicConsensusBES(ArrayList<Dag> dags, int maxSize, double percentage) {
        super(dags);
        this.maxSize = maxSize;
        this.percentage = percentage;
    }

    /**
     * Executes the heuristic consensus backward equivalence search.
     * This method first applies the ConsensusUnion to compute a consensus DAG from the input DAGs,
     * and then applies the Backward Equivalence Search with D-separation to refine the graph, setting the maxSize and percentage parameters for an heuristic search.
     * The resulting output DAG is stored in the outputDag attribute.
     */
    @Override
    public void fusion(){
        // 1. Apply ConsensusUnion
        consensusUnion();
        // 2. Apply Heuristic BES with D-separation
        BackwardEquivalenceSearchDSep bes = new BackwardEquivalenceSearchDSep(this.getUnion(), this.getInputDags(), this.getTransformedDags());
        bes.setMaxSize(maxSize);
        bes.setPercentage(percentage);
        this.outputDag = bes.applyBackwardEliminationWithDSeparation();
        // 3. Updating numberOfInsertedEdges
        this.numberOfInsertedEdges -= bes.getNumberOfRemovedEdges();
    }
}
