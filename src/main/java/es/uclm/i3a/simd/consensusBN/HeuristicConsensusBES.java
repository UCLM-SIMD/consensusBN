package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;

import edu.cmu.tetrad.graph.Dag;

public class HeuristicConsensusBES extends ConsensusBES{

    private final int maxSize;
    private final double percentage;

    /**
     * Constructor for HeuristicConsensusBES2.
     * This class extends ConsensusBES to implement a heuristic approach
     * for backward equivalence search in directed acyclic graphs (DAGs).
     */
    public HeuristicConsensusBES(ArrayList<Dag> dags, int maxSize, double percentage) {
        super(dags);
        this.maxSize = maxSize;
        this.percentage = percentage;
    }

    // Additional methods and overrides can be added here as needed
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
