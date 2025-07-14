package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Node;

/**
 * This class transforms a set of DAGs by applying the BetaToAlpha transformation to each DAG with a given alpha order.
 */
public class TransformDags {
	/**
	 * List of input DAGs to be transformed.
	 */
	private final ArrayList<Dag> setOfDags;
	/**
	 * List of output DAGs after transformation.
	 */
	private final ArrayList<Dag> setOfOutputDags;
	/**
	 * The alpha order used for the transformation.
	 */
	private final ArrayList<Node> alpha;
	/**
	 * The transformation objects for each DAG.
	 * Each BetaToAlpha object applies the transformation to a corresponding DAG in setOfDags using the alpha order provided.
	 */
	private ArrayList<BetaToAlpha> transformers= null;

	/**
	 * Number of edges inserted during the transformation process.
	 * This is used to track how many edges were added to the transformed DAGs.
	 */
	private int numberOfInsertedEdges = 0;
	
	/**
	 * Constructor for TransformDags.
	 * Initializes the object with a list of DAGs and an alpha order.
	 * It creates a new BetaToAlpha transformation for each DAG in the input list.
	 * Each DAG in the input list will be transformed according to this alpha order.
	 * The transformation is applied by creating a BetaToAlpha object for each DAG.
	 * The transformed DAGs will be stored in setOfOutputDags after calling the transform() method.
	 * @see BetaToAlpha
	 * @param dags List of DAGs to be transformed.
	 * @param alpha List of nodes representing the alpha order for the transformation.
	 */
	public TransformDags(ArrayList<Dag> dags, ArrayList<Node> alpha){
		
		this.setOfDags = dags;
		this.setOfOutputDags = new ArrayList<>();
		this.transformers = new ArrayList<>();
		this.alpha = alpha;
		// Initialize the BetaToAlpha transformation for each DAG in the input list
		for (Dag i : setOfDags)	{
			Dag out = new Dag(i);
			this.transformers.add(new BetaToAlpha(out,this.alpha));
		}
		
	}
	
	/**
	 * Transforms the input DAGs by applying the BetaToAlpha transformation.
	 * This method iterates through each BetaToAlpha object, applies the transformation,
	 * and collects the transformed DAGs into setOfOutputDags.
	 * It also counts the total number of edges inserted during the transformations.
	 * 
	 * @see BetaToAlpha#transform()
	 * @see BetaToAlpha#getNumberOfInsertedEdges()
	 * @see BetaToAlpha#getGraph()
	 * @return An ArrayList of transformed DAGs after applying the BetaToAlpha transformation.
	 */
	public ArrayList<Dag> transform (){
		this.numberOfInsertedEdges = 0;
		for(BetaToAlpha transformDagi: this.transformers){
			transformDagi.transform();
			this.numberOfInsertedEdges += transformDagi.getNumberOfInsertedEdges();
			this.setOfOutputDags.add(transformDagi.getGraph());
		}
		return this.setOfOutputDags;
	}
	
	/**
	 * Returns the number of edges that were inserted during the transformation process.
	 * @return The total number of edges inserted across all transformed DAGs.
	 */
	public int getNumberOfInsertedEdges(){
		return this.numberOfInsertedEdges;
	}
	
	/**
	 * Returns the list of input DAGs that were transformed.
	 * @return An ArrayList of DAGs that were provided as input to the transformation.
	 */
	public ArrayList<Dag> getSetOfDags() {
		return this.setOfDags;
	}

	/**
	 * Returns the list of transformed output DAGs.
	 * This list contains the DAGs after applying the BetaToAlpha transformation.
	 * @return An ArrayList of transformed DAGs.
	 */
	public ArrayList<Dag> getSetOfOutputDags() {
		return this.setOfOutputDags;
	}

	/**
	 * Returns the alpha order used for the transformation.
	 * @return An ArrayList of nodes representing the alpha order.
	 */
	public ArrayList<Node> getAlpha() {
		return this.alpha;
	}

	/**
	 * Returns the list of BetaToAlpha transformers used for the transformation.
	 * @return An ArrayList of BetaToAlpha objects, each corresponding to a DAG in the input list.
	 */
	public ArrayList<BetaToAlpha> getTransformers() {
		return this.transformers;
	}

	/**
	 * Sets the list of BetaToAlpha transformers.
	 * This method allows for updating the transformers used in the transformation process.
	 * 
	 * @param transformers An ArrayList of BetaToAlpha objects to be set as the transformers for this TransformDags instance.
	 * Each transformer will apply the BetaToAlpha transformation to its corresponding DAG in the input list.
	 */
	public void setTransformers(ArrayList<BetaToAlpha> transformers) {
		this.transformers = transformers;
	}
	
}
