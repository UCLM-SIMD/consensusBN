package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Node;


/**
 * This class implements the Consensus Union algorithm which applies a fusion between multiple Directed Acyclic Graphs (DAGs).
 * It constructs a consensus DAG by merging the input DAGs based on a specified order of nodes (alpha).
 * The alpha order is computed with the AlphaOrder class which implements a Greedy Heuristic Order (GHO) search, achieving a good order to transform the input DAGs.
 * Once each DAG is transformed, the union method creates a new DAG that contains all the edges from the input DAGs, ensuring that the resulting graph is acyclic.
 * The number of edges inserted during the union process can be retrieved using getNumberOfInsertedEdges. 
 * 
 * This class is also runnable, allowing it to be executed in a separate thread.
 */
public class ConsensusUnion implements Runnable{
	
	/**
	 * The alpha order of nodes in the consensus DAG.
	 * This order is used to transform the input DAGs into a compatible I-Maps before merging.
	 * It is computed using the AlphaOrder class.
	 * 
	 * @see AlphaOrder
	 */
	private ArrayList<Node> alpha;
	/**
	 * The AlphaOrder heuristic used to compute the alpha order.
	 */
	private AlphaOrder heuristic = null;

	/**
	 * The TransformDags instance that transforms the input DAGs based on the alpha order.
	 */
	private TransformDags imaps2alpha;

	/**
	 * List of input DAGs to be merged.
	 */
	private ArrayList<Dag> setOfdags = null;

	/**
	 * The output DAG resulting from the union of the transformed input DAGs.
	 */
	Dag union = null;

	/**
	 * Number of edges inserted during the consensus union process.
	 */
	int numberOfInsertedEdges = 0;
	
	/**
	 * Constructor for ConsensusUnion that initializes the union process with a list of DAGs and an alpha order.
	 * @param dags the list of input DAGs to be merged.
	 * @param order the alpha order of nodes to be used for transforming the input DAGs.
	 */
	public ConsensusUnion(ArrayList<Dag> dags, ArrayList<Node> order){
		this.setOfdags = dags;
		this.alpha = order;
	}	
	
	/**
	 * Constructor for ConsensusUnion that initializes the union process with a list of DAGs and uses the AlphaOrder object to generate an alpha order.
	 * @see AlphaOrder
	 * @param dags the list of input DAGs to be merged.
	 */
	public ConsensusUnion(ArrayList<Dag> dags){
		this.setOfdags = dags;
		this.heuristic = new AlphaOrder(this.setOfdags);
	}	
	
	/**
	 * Default constructor for ConsensusUnion that initializes an empty union.
	 * This constructor can be used when the DAGs are set later using the setDags method.
	 */
	public ConsensusUnion(){
		this.setOfdags = null;
	}
	
	/**
	 * Returns the number of edges inserted during the union process.
	 * This value is updated after the union method is called.
	 * @return the number of edges inserted in the consensus DAG.
	 */
	public int getNumberOfInsertedEdges(){
		return this.numberOfInsertedEdges;
	}
	
	/**
	 * Performs the union of the input DAGs based on the alpha order. If no alpha order is set, it computes it first.
	 * The method transforms each input DAG according to the alpha order and then merges them into a single consensus DAG.
	 * The resulting DAG contains all edges from the transformed input DAGs, ensuring that it remains acyclic.
	 * 
	 * @throws IllegalStateException if the alpha order is not set before calling this method.
	 * @throws IllegalArgumentException if the input DAGs are null or empty.
	 * @throws NullPointerException if the alpha order is null.
	 * @return the resulting consensus DAG after merging the transformed input DAGs.
	 * @see AlphaOrder
	 * @see TransformDags
	 */
	public Dag union(){
		
		// Computing Alpha Order if not set, using the Greedy Heuristic Order (GHO)
		if(this.alpha == null){
			this.heuristic.computeAlpha();
			this.alpha = this.heuristic.getOrder();
		}
		
		// Transforming each DAG with the alpha order
		this.imaps2alpha = new TransformDags(this.setOfdags,this.alpha);
		this.imaps2alpha.transform();
		this.numberOfInsertedEdges = this.imaps2alpha.getNumberOfInsertedEdges();
	
		// Applying a union of the edges of the transformed DAGs
		this.union = new Dag(this.alpha);
		for(Node nodei: this.alpha){
			for(Dag d : this.imaps2alpha.getSetOfOutputDags()){
				List<Node>parent = d.getParents(nodei);
				for(Node pa: parent){
					if(!this.union.isParentOf(pa, nodei)) this.union.addEdge(new Edge(pa,nodei,Endpoint.TAIL,Endpoint.ARROW));
				}
			}
			
		}
		return this.union;
		
	}
	
	/**
	 * Returns the resulting consensus DAG after the union process.
	 * This method should be called after the union method to ensure that the union has been performed.
	 * @return the consensus DAG resulting from the union of the input DAGs.
	 */
	public Dag getUnion(){
	
		return this.union;
		
	}
	
	/**
	 * sets the list of input DAGs for the ConsensusUnion instance and applies the AlphaOrder heuristic to compute the alpha order.
	 * This method also updates the alpha order and transforms the input DAGs accordingly.
	 * @param dags
	 */
	void setDags(ArrayList<Dag> dags){
		this.setOfdags = dags;
		this.heuristic = new AlphaOrder(this.setOfdags);
		this.heuristic.computeAlpha();
		this.alpha = this.heuristic.getOrder();
		this.imaps2alpha = new TransformDags(this.setOfdags,this.alpha);
		this.imaps2alpha.transform();
	}

	/**
	 * Runs the ConsensusUnion process in a separate thread.
	 */
	@Override
	public void run() {
		this.union = this.union();
	}
		
		
		
	
}
