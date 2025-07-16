package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Node;

/**
 * This class implements the Optimal Fusion GES^h_d algorithm, which applies a Consensus Union followed by a Backward Equivalence Search (BES) with D-separation.
 * The algorithm first computes a consensus DAG from a set of input DAGs using the ConsensusUnion class.
 * After obtaining the consensus DAG, it applies the Backward Equivalence Search with D-separation to refine the graph, achieving the optimal fusion BN.
 * The resulting output DAG is stored in the outputDag attribute.
 */
public class ConsensusBES implements Runnable {
	
	/**
	 * Final output DAG after applying the Consensus Union and Backward Equivalence Search with D-separation.
	 * This DAG represents the optimal fusion of the input DAGs.
	 * It is computed by first merging the input DAGs into a consensus DAG and then refining it using the BES with D-separation.
	 * 
	 * @see ConsensusUnion
	 * @see BackwardEquivalenceSearchDSepTest
	 */
	private Dag outputDag;

	/**
	 * Instance of ConsensusUnion used to compute the consensus DAG from the input DAGs.
	 * This instance is initialized with the set of input DAGs and computes the alpha order of nodes using AlphaOrder heuristic (Greedy Heuristic Order).
	 * 
	 * @see ConsensusUnion
	 * @see AlphaOrder
	 */
	private final ConsensusUnion consensusUnion;

	/**
	 * List of input DAGs to be fused using the ConsensusBES algorithm.
	 */
	private final ArrayList<Dag> inputDags;

	/**
	 * List of transformed DAGs after applying the alpha order to the input DAGs.
	 * @see BetaToAlpha
	 * @see TransformDags 
	 */
	private ArrayList<Dag> transformedDags;

	/**
	 * Resulting DAG afther applying the Consensus Union algorithm.
	 * This DAG contains the union of all edges from the transformed input DAGs, ensuring that the resulting graph is acyclic.
	 * The number of edges inserted during the union process can be retrieved using getNumberOfInsertedEdges.
	 */
	private Dag union = null;

	/**
	 * Number of edges inserted during the consensus union process and the Backward Equivalence Search process.
	 */
	int numberOfInsertedEdges = 0;
	
	/**
	 * Local score map used to store the scores of graph changes during the Backward Equivalence Search.
	 * The key is a string representation of the nodes and their conditioning set, and the value is the score associated with that configuration.
	 */
	private final Map<String, Double> localScore = new HashMap<>();
	
	/**
	 * Constructor for ConsensusBES that initializes the union process with a list of DAGs.
	 * It creates an instance of ConsensusUnion to compute the consensus DAG.
	 * @param dags the list of input DAGs to be merged.
	 */
	public ConsensusBES(ArrayList<Dag> dags){
		this.inputDags = dags;
		this.consensusUnion = new ConsensusUnion(this.inputDags);
	}
	
	/**
	 * Performs the consensus union operation by calling the union method of the ConsensusUnion instance.
	 * This method initializes the union process, transforming the input DAGs based on the alpha order and merging them into a single consensus DAG.
	 * After the union, it retrieves the transformed DAGs and updates the number of inserted edges.
	 */
	public void consensusUnion(){
		this.union = this.consensusUnion.union();
		this.transformedDags = this.consensusUnion.getTransformedDags();
		this.numberOfInsertedEdges += consensusUnion.getNumberOfInsertedEdges();
	}
	
	/**
	 * Applies the fusion process by first performing the consensus union and then applying the Backward Equivalence Search with D-separation.
	 * This method modifies the outputDag attribute to contain the final fused DAG after applying both steps.
	 */
	public void fusion(){
		// 1. Apply ConsensusUnion to the set of dags
		consensusUnion();
		// 2. Apply Backward Equivalence Search with D-separation
		BackwardEquivalenceSearchDSep bes = new BackwardEquivalenceSearchDSep(this.union, this.inputDags, this.transformedDags);
		this.outputDag = bes.applyBackwardEliminationWithDSeparation();
		// 3. Updating numberOfInsertedEdges
		this.numberOfInsertedEdges += bes.getNumberOfInsertedEdges();
	}
	
	/**
	 * Returns the output DAG after applying the Consensus Union and Backward Equivalence Search with D-separation.
	 * This method retrieves the final fused DAG, which represents the optimal fusion of the input DAGs.
	 * @return the resulting output DAG after the fusion process.
	 */
    public Dag getFusion(){
    	return this.outputDag;
    }
    
	/**
	 * Returns a valid ancestral order of the nodes in the fused DAG.
	 * @return
	 */
    public List<Node> getOrderFusion(){
    	return  this.getFusion().paths().getValidOrder(this.getFusion().getNodes(),true);
    }
    
	/**
	 * Returns the number of edges inserted during the consensus union and removed in the Backward Equivalence Search with D-separation.
	 * @return
	 */
	public int getNumberOfInsertedEdges(){
		return this.numberOfInsertedEdges;
	}

	/**
	 * Returns the union DAG resulting from the consensus union process.
	 * @return the union DAG after merging the transformed input DAGs.
	 */
	public Dag getUnion() {
		return this.union;
	}

	/**
	 * Returns the ConsensusUnion instance used in this ConsensusBES.
	 * This instance contains the logic for merging the input DAGs and computing the alpha order.
	 * @return the ConsensusUnion instance associated with this ConsensusBES.
	 */
	public ConsensusUnion getConsensusUnion() {
		return this.consensusUnion;
	}
	
	/**
	 * Returns the list of transformed DAGs after applying the alpha order to the input DAGs.
	 * This method retrieves the transformed DAGs that were used in the consensus union process.
	 * @return the list of transformed DAGs.
	 */
	public ArrayList<Dag> getTransformedDags() {
		if (this.transformedDags != null) {
			return this.transformedDags;
		} else {
			throw new IllegalStateException("Transformed DAGs have not been initialized. Please call fusion() first.");
		}
	}
	
	/**
	 * Runs the ConsensusBES algorithm in a thread, performing the consensus union and the Backward Equivalence Search with D-separation.
	 */
	@Override
	public void run() {
		this.fusion();
	}
}
