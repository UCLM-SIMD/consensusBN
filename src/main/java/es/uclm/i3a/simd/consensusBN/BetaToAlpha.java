package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Node;

/**
 * BetaToAlpha is a class that transforms a directed acyclic graph (DAG) into an I-map minimal with respect to a specified alpha order.
 * It constructs a compatible beta order and modifies the graph accordingly.
 * The transformation respects the alpha order, ensuring that the resulting graph is consistent with it.
 */
public class BetaToAlpha {

	/**
	 * The directed acyclic graph (DAG) to be transformed.
	 */
	private final Dag dag;

	/**
	 * The beta order derived from the alpha order.
	 */
	private List<Node> beta;

	/**
	 * The alpha order that the graph should respect. In consensusBN, this alpha order has been created using the AlphaOrder class.
	 * If null, a random order will be created.
	 */
	private List<Node> alpha;
	
	/**
	 * A hash map to store the index of each node in the alpha order for quick access.
	 */
	private final HashMap<Node,Integer> alphaHash= new HashMap<>();
	
	/**
	 * The auxiliary graph used during the transformation process.
	 */
	private Dag G_aux = null;

	/**
	 * The number of edges inserted during the transformation process.
	 */
	int numberOfInsertedEdges = 0;
	
	/**
	 * Constructor for BetaToAlpha that initializes the graph and alpha order.
	 * @param dag the directed acyclic graph (DAG) to be transformed.
	 * @param alpha the alpha order that the graph should respect.
	 */
	public BetaToAlpha(Dag dag, ArrayList<Node> alpha){
		this.alpha = alpha;
		this.dag = dag;
		this.beta = null;
		for(int i= 0; i< alpha.size(); i++){
			Node n = alpha.get(i);
			alphaHash.put(n, i);
		}
		
	}
	
	/**
	 * Constructor for BetaToAlpha that initializes the graph without a specified alpha order.
	 * A random alpha order will be created instead.
	 * @param dag the directed acyclic graph (DAG) to be transformed.
	 */
	public BetaToAlpha(Dag dag){
		this.alpha = null;
		this.dag = dag;
		this.beta = null;
	}

	/**
	 * Computes the alpha hash map if it is not already computed.
	 * This method populates the alphaHash with the index of each node in the alpha order.
	 * It is called before any transformation to ensure that the alpha order is respected.
	 * If the alpha order is null, it will not compute the hash.
	 */
	public void computeAlphaHash(){
		
		if(this.alpha !=null){
			if(alphaHash.isEmpty()){
				for(int i= 0; i< alpha.size(); i++){
					Node n = alpha.get(i);
					alphaHash.put(n, i);
				}
			}
		}
		
	}
	
	// Only to test the methods, to build a random order.
	
	
	/**
	 * Builds a random alpha order from the nodes of the graph. This is used for test purposes to ensure that the transformation can handle different orders.
	 * @param randomGenerator the random number generator to use for shuffling the nodes.
	 * @return a list of nodes representing a random alpha order.
	 */
	public List<Node> randomAlpha (Random randomGenerator){

		List<Node> nodes = this.dag.getNodes();
		this.alpha = new ArrayList<>();

		int[] index = new int[nodes.size()];

		for(int i = 0; i< nodes.size() ; i++){
			index[i]=i;
		}

		for (int j = 0; j < nodes.size(); j++){
			int indi = randomGenerator.nextInt(nodes.size());
			int indj = randomGenerator.nextInt(nodes.size());
			int sw = index[indi];
			index[indi] = index[indj];
			index[indj] = sw;
		}

		for (int i = 0; i< nodes.size(); i++){
			this.alpha.add(i, nodes.get(index[i]));
		}
		this.computeAlphaHash();
		return this.alpha;
	}
	
	/**
	 * Transforms the graph G into an I-map minimal with respect to the alpha order.
	 */
	public void transform(){
		
		// 1. Create a compatible beta order with the alfa order for the DAG G.
		buildBetaOrder();
		
		// 2. Transform graph G into an I-map minimal with alpha order
		transformWithBeta();
		
	}

	/**
	 * Builds the beta order that best respects the alpha order for the given graph G.
	 * This method constructs a beta order by identifying sink nodes and arranging them in a way that minimizes the number of edges that violate the alpha order.
	 * It uses a greedy approach to select the next node based on its position in the alpha order.
	 * The beta order is constructed such that it is as close as possible to the alpha order while ensuring that the resulting graph is still a DAG.
	 * 
	 * This method modifies the G_aux graph to reflect the current state of the transformation.
	 * It also initializes the beta list with the first sink node and iteratively adds nodes to the beta order based on their relationships in the graph.
	 */
	private void buildBetaOrder() {
		this.G_aux = new Dag(this.dag);
		this.beta = new ArrayList<>();
		List<Node> parents;

		// Compute the sink nodes and add the first one to beta.
		ArrayList<Node> sinkNodes = getSinkNodes(this.G_aux);
		this.beta.add(sinkNodes.get(0)); 
		parents = G_aux.getParents(sinkNodes.get(0));
		this.G_aux.removeNode(sinkNodes.get(0));
		sinkNodes.remove(0); 

		// Compute the new sink nodes
		updateSinkNodes(sinkNodes, parents);

		// Construct beta order as close as possible to alpha.
		while (this.G_aux.getNumNodes()>0){
			// Select fist sink node
			Node sink = sinkNodes.get(0);
			parents = G_aux.getParents(sink);
			this.G_aux.removeNode(sink);
			sinkNodes.remove(0);
			// Compute the new sink nodes
			updateSinkNodes(sinkNodes, parents);

			// Compute the index to insert the sink node in beta.
			int insertIndex = 0;
			for (; insertIndex < beta.size(); insertIndex++) {
				Node current = beta.get(insertIndex);
				if (alphaHash.get(current) > alphaHash.get(sink)) break;
				if (dag.getParents(current).contains(sink)) break;
			}
			beta.add(insertIndex, sink);
		}
	}
/* FUTURE IDEA: SELECT BEST SINK NODE FROM ALPHA ORDER.
	private Node selectBestSinkNode(List<Node> sinkNodes) {
		return sinkNodes.stream()
			.min(Comparator.comparingInt(alfaHash::get))
			.orElse(sinkNodes.get(0));
	}
*/
	/**
	 * Updates the sink nodes list based on the current list of candidates.
	 * This method checks each candidate node to see if it has any children in the auxiliary graph G_aux.
	 * If a candidate node has no children, it is added to the sink nodes list.
	 * This is used to maintain the integrity of the beta order during the transformation process.
	 * 
	 * @param sinkNodes the list of current sink nodes to be updated.
	 * @param candidates the list of candidate nodes to check for children.
	 */
	private void updateSinkNodes(ArrayList<Node> sinkNodes, List<Node> candidates) {
		// Compute the new sink nodes
		for(Node node: candidates){
			List<Node> chld = G_aux.getChildren(node);
			if (chld.isEmpty())
				sinkNodes.add(node);
		}
	}
	
	/**
	 * Transforms the graph G into an I-map minimal with respect to the alpha order.
	 * This method rearranges the edges in the graph based on the beta order derived from the alpha order.
	 * It ensures that the resulting graph respects the alpha order by checking the relationships between nodes and adjusting edges accordingly.
	 * The transformation modifies the graph in place and updates the beta list to reflect the new order of nodes.
	 */
	private void transformWithBeta() {
		ArrayList<Node> orderedNodes = new ArrayList<>();
		// Setting the first node in the orderedNodes list.
		orderedNodes.add(this.beta.remove(0));
		
		while(!this.beta.isEmpty()){ 
			// Setting the next node in the orderedNodes list.
			orderedNodes.add(this.beta.get(0));
			this.beta.remove(0);
			int i = orderedNodes.size();
			boolean changed = true;
			
			while (changed){
				if(i==1) break;
				changed = false;
				// Getting the last two nodes in the ordered list
				Node nodeY = orderedNodes.get(i-1);
				Node nodeZ = orderedNodes.get(i-2);

				// Check if there is an edge from nodeZ to nodeY, if so, cover it.
				if ((nodeZ != null) && (this.alphaHash.get(nodeZ) > this.alphaHash.get(nodeY))){
					if(this.dag.getEdge(nodeZ, nodeY) != null){
						List<Node> paZ = this.dag.getParents(nodeZ);
						List<Node> paY = this.dag.getParents(nodeY);
						paY.remove(nodeZ);
						this.dag.removeEdge(nodeZ, nodeY);
						this.dag.addEdge(new Edge(nodeY,nodeZ,Endpoint.TAIL, Endpoint.ARROW));
						for(Node nodep: paZ){
							Edge pay = this.dag.getEdge(nodep, nodeY);
							if(pay == null){
								this.dag.addEdge(new Edge(nodep,nodeY,Endpoint.TAIL,Endpoint.ARROW));
								this.numberOfInsertedEdges++;
							}
						}
						for(Node nodep : paY){
							Edge paz = this.dag.getEdge(nodep,nodeZ);
							if(paz == null){
								this.dag.addEdge(new Edge(nodep,nodeZ,Endpoint.TAIL,Endpoint.ARROW));
								this.numberOfInsertedEdges++;
							}
						}
					}
					changed = true;
					orderedNodes.remove(nodeY);
					orderedNodes.add(i-2,nodeY);
					i--;	
				}
			}
		}
		this.beta = orderedNodes;
	}
	/**
	 * Returns the number of edges that were inserted during the transformation process.
	 * This method is useful for understanding how many modifications were made to the original graph to achieve the desired alpha order.
	 * @return the number of edges that were inserted during the transformation process.
	 * @see BetaToAlpha#transform()
	 */
	public int getNumberOfInsertedEdges(){
		return this.numberOfInsertedEdges;
	}
	
	/**
	 * Retrieves the sink nodes from the given directed acyclic graph (DAG).
	 * A sink node is defined as a node that does not have any children in the graph.
	 * This method iterates through all nodes in the graph and checks their children to determine if they are sink nodes.
	 * 
	 * @param dagGraph the directed acyclic graph (DAG) from which to retrieve sink nodes.
	 * @return an ArrayList of sink nodes that do not have any children in the graph.
	 */
	private ArrayList<Node> getSinkNodes(Dag dagGraph){
		// Get nodes from DAG
		ArrayList<Node> sinkNodes = new ArrayList<>();
		List<Node> nodes = dagGraph.getNodes();
		// Check which nodes don't have children and add them to sinkNodes
		for (Node node : nodes){
			if(dagGraph.getChildren(node).isEmpty()){
				sinkNodes.add(node);
			}
		}
		return sinkNodes;		
	}

	/**
	 * Returns the alpha hash map that contains the index of each node in the alpha order.
	 * This map is used to quickly access the position of nodes in the alpha order during the transformation process.
	 * It is particularly useful for ensuring that the resulting graph respects the specified alpha order.
	 * @return the alpha hash map where keys are nodes and values are their indices in the alpha order.
	 */
	public HashMap<Node, Integer> getAlphaHash() {
		return alphaHash;
	}

	/**
	 * Sets the alpha order for the transformation.
	 * This method allows the user to specify a new alpha order for the graph transformation.
	 * It updates the alpha field and recomputes the alpha hash map to reflect the new order.
	 * @param alpha the new alpha order to be set for the transformation.
	 */
	public void setAlphaOrder(List<Node> alpha) {
		this.alpha = alpha;
		this.computeAlphaHash();
	}

	/**
	 * Returns the alpha order that the graph should respect.
	 * @return the alpha order as a list of nodes, or null if no alpha order has been set.
	 */
	public List<Node> getAlphaOrder() {
		return alpha;
	}

	/**
	 * Returns the directed acyclic graph (DAG) that has been transformed.
	 * This method provides access to the modified graph after the transformation has been applied.
	 * The graph will be an I-map minimal with respect to the specified alpha order.
	 * 
	 * @see BetaToAlpha#transform()
	 * @return the transformed directed acyclic graph (DAG) as a Dag object.
	 */
	public Dag getGraph() {
		return dag;
	}

			
}

		
