package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;


/**
 * This class implements the PairWiseConsensusBES algorithm, which measures the similarity between two DAGs by fusing them with the ConsensusBES algorithm.
 * It first fuses the two input DAGs into a consensus DAG using the ConsensusBES class.
 * After obtaining the consensus DAG, it calculates the Hamming distance between the fused DAG and the original input DAGs.
 * The resulting output DAG is stored in the consensusDAG attribute, and the number of inserted edges during the fusion process can be retrieved using getNumberOfInsertedEdges.
 */
public class PairWiseConsensusBES implements Runnable{
	/** 
	 * The first input DAG to be fused.
	*/
	private Dag firstDag = null;
	
	/**
	 * The second input DAG to be fused.
	*/
	private Dag secondDag = null;
	
	/**
	 * The resulting consensus DAG after applying the fusion process.
	 */
	private Dag consensusDAG = null;

	/**
	 * Instance of ConsensusBES used to compute the consensus DAG from the input DAGs.
	 */
	private ConsensusBES consensusBES= null;
	
	/**
	 * Number of total edges inserted during the fusion process.
	 */
	private int numberOfInsertedEdges = 0;

	/**
	 * Number of edges inserted during the consensus union process. 
	 */
	private int numberOfUnionEdges = 0;
	
	/**
	 * Constructor for the PairWiseConsensusBES class.
	 * It initializes the instance with two input DAGs and checks if they are valid.
	 * If the input DAGs are not valid, it throws an IllegalArgumentException.
	 * @param firstDag
	 * @param secondDag
	 */
	public PairWiseConsensusBES(Dag firstDag, Dag secondDag) {
		checkInput(firstDag, secondDag);
		this.firstDag = firstDag;
		this.secondDag = secondDag;
	}
	/**
	 * Checks if the input DAGs are valid.
	 * Validity is determined by ensuring that the DAGs are not null, contain at least one node and one edge, and have the same set of nodes.
	 * If any of these conditions are not met, an IllegalArgumentException is thrown.
	 * @param firstDag first input DAG
	 * @param secondDag second input DAG
	 * @throws IllegalArgumentException if the input DAGs are not valid
	 */
	private void checkInput(Dag firstDag, Dag secondDag) {
		if (firstDag == null || secondDag == null) {
			throw new IllegalArgumentException("Input DAGs cannot be null.");
		}
		if (firstDag.getNumNodes() == 0 || secondDag.getNumNodes() == 0) {
			throw new IllegalArgumentException("Input DAGs must contain at least one node.");
		}
		if (firstDag.getNumEdges() == 0 || secondDag.getNumEdges() == 0) {
			throw new IllegalArgumentException("Input DAGs must contain at least one edge.");
		}
		if (firstDag.getNodes().size() != secondDag.getNodes().size()) {
			throw new IllegalArgumentException("Input DAGs must have the same number of nodes.");
		}
		if (!firstDag.getNodes().containsAll(secondDag.getNodes())) {
			throw new IllegalArgumentException("Input DAGs must have the same set of nodes.");
		}
	}

	/**
	 * Performs the fusion process by first applying the consensus union and then applying the Backward Equivalence Search.
	 */
	public void fusion(){
		// Creating a list of DAGs to be fused
		ArrayList<Dag> setOfDags = new ArrayList<>();
		setOfDags.add(this.firstDag);
		setOfDags.add(this.secondDag);
		// Applying the ConsensusBES algorithm to fuse the DAGs
		consensusBES = new ConsensusBES(setOfDags);
		consensusBES.fusion();
		// Retrieving the resulting DAG and the number of inserted edges
		this.numberOfInsertedEdges = consensusBES.getNumberOfInsertedEdges();
		this.numberOfUnionEdges  = consensusBES.getUnion().getNumEdges();
		this.consensusDAG = consensusBES.getFusionDag();
	}

	/**
	 * Returns the number of edges inserted during the fusion process.
	 * This method retrieves the number of edges that were added to the consensus DAG during the fusion process.
	 * It is useful for understanding how many edges were introduced in the consensus DAG compared to the original input DAGs.
	 * @return
	 */
	public int getNumberOfInsertedEdges(){
		return this.numberOfInsertedEdges;
	}
	
	/**
	 * Returns the number of edges in the union DAG after the consensus union process.
	 * This method retrieves the number of edges that were present in the union DAG after merging the transformed input DAGs.
	 * It is useful for understanding the size of the union DAG before applying the Backward Equivalence Search.
	 * This number can be used to compare with the number of edges in the final consensus DAG after the Backward Equivalence Search.
	 * 
	 * @see ConsensusBES#getUnion()
	 * @see ConsensusBES#getNumberOfInsertedEdges()
	 * @return
	 */
	public int getNumberOfUnionEdges(){
		return this.numberOfUnionEdges;
	}
	
	/**
	 * Calculates the Hamming distance between the optimum fusion DAG and the original input DAGs.
	 * @return The Hamming distance between the fused DAG and the original input DAGs.
	 */
	public int calculateHammingDistance(){
		if(this.consensusDAG==null) this.fusion();
		int distance = 0;
		for(Edge ed: this.consensusDAG.getEdges()){
			Node tail = ed.getNode1();
			Node head = ed.getNode2();
			for(Dag g: consensusBES.getTransformedDags()){	
				Edge edge1 = g.getEdge(tail, head);
				Edge edge2 = g.getEdge(head, tail);
				if(edge1 == null && edge2==null) distance++;
			}
		}
		return distance+this.getNumberOfInsertedEdges();
	}
	
	/**
	 * Returns the resulting consensus DAG after applying the fusion process.
	 * This method retrieves the final fused DAG, which represents the optimal fusion of the input DAGs.
	 * It is useful for obtaining the consensus structure after the fusion process has been completed.
	 * 
	 * @see ConsensusBES#getFusionDag()
	 * @return
	 */
	public Dag getDagFusion(){
		return this.consensusDAG;
		
	}

	/**
	 * Runs the fusion process in a thread, performing the consensus union and the Backward Equivalence Search with D-separation.
	 */
	@Override
	public void run() {
		this.fusion();
	}

}
