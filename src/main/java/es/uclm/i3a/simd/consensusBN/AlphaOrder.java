package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Node;

/**
 * This class implements a heuristic to compute an ancestral order of nodes for a set of DAGs.
 *  The heuristic is based on finding the best sink node in each iteration for the set of DAGs,
 *  removing it from the DAGs, and repeating the process until all nodes are ordered.
 */
public class AlphaOrder {
	/**
	 * The set of DAGs to compute the ancestral order from.
	 */
	private final ArrayList<Dag> setOfDags;
	/**
	 * The computed ancestral order of nodes.
	 */
	private ArrayList<Node> alpha;
	/**
	 * A set of auxiliary DAGs used during the computation.
	 */
	private final ArrayList<Dag> setOfauxG;
	
	/**
	 * Constructor for the AlphaOrder class.
	 * Initializes the set of DAGs and creates a copy of each DAG to work with.
	 * @param dags the list of DAGs from which to compute the ancestral order.
	 * This constructor creates a deep copy of each DAG to avoid modifying the original DAGs during
	 * the computation of the ancestral order.
	 */
	public AlphaOrder(ArrayList<Dag> dags){
		// Check if the dags are valid
		checkExceptions(dags);

		// Initialize the class variables
		this.setOfDags = dags;
		this.alpha = new ArrayList<>();
		this.setOfauxG = new ArrayList<>();
		for (Dag i : setOfDags)	{
			Dag aux_G = new Dag(i);
			setOfauxG.add(aux_G);
		}
	}

	/**
	 * Checks for exceptions in the input set of DAGs.
	 * Throws an IllegalArgumentException if the set is null, empty, or contains DAGs with different nodes.
	 * Also checks that the size of the set is greater than 1.
	 * @param setOfDags the set of DAGs to check for exceptions.
	 */
	private void checkExceptions(ArrayList<Dag> setOfDags) {
		// Check if setOfDags is null
		if(setOfDags == null) {
			throw new IllegalArgumentException("The set of DAGs is null.");
		}

		// Check if all DAGs have the same nodes
		if (setOfDags.isEmpty()) {
			throw new IllegalArgumentException("The set of DAGs is empty.");
		}
		// Check that the size is greater than 1
		if(setOfDags.size() <= 1) {
			throw new IllegalArgumentException("The set of DAGs has only one DAG.");
		}
		
		// Check that all DAGs have the same nodes
		List<Node> firstDagNodes = setOfDags.get(0).getNodes();
		for (Dag dag : setOfDags) {
			if (!dag.getNodes().equals(firstDagNodes)) {
				throw new IllegalArgumentException("All DAGs must have the same nodes. Dag " + dag + " has different nodes than the rest of DAGs.");
			}
		}
		
	}

	
	/**
	 * Returns the nodes of the first DAG in the set, since all DAGs are assumed to have the same nodes.
	 * @return
	 */
	public List<Node> getNodes(){
		return(setOfDags.get(0).getNodes());
	}
	
	/**
	 * This method computes the heuristic to find an ancestral order of nodes of consensus. It is based on the number of edges that would be added on a sequence created from the sink nodes upwards.
	 * It iteratively finds the node with the minimum number of changes (inversions and additions of edges) and adds it to the beginning of the order.
	 * */ 
	public void computeAlpha(){
		
		// Get nodes and initialize the alpha list
		List<Node> nodes = setOfDags.get(0).getNodes();
		LinkedList<Node> alpha_aux = new LinkedList<>();
		
		while(!nodes.isEmpty()){
			int index_alpha = computeNextSink(nodes);
			Node nodeAlpha = nodes.get(index_alpha);
			alpha_aux.addFirst(nodeAlpha);
			for(Dag g: this.setOfauxG){
				removeNode(g,nodeAlpha);
			}
			nodes.remove(nodeAlpha);
		}
		this.alpha = new ArrayList<>(alpha_aux);
	}
	
	
	/**
	 * Gets the following node in the order based on the minimum number of changes (inversions and additions of edges) that would be required to create a sequence from the sink nodes upwards.
	 * @param nodes Remaining nodes to be ordered.
	 * @return index of the node that should be added next to the order.
	 */
	private int computeNextSink(List<Node> nodes){
		
		// Setting up variables to count changes
		int changes;
		int inversion = 0;
		int addition = 0;
		int indexNode = 0;
		int min = Integer.MAX_VALUE;

		// Iterate through each node to find the one with the minimum changes for the list of DAGs.	
		for(int i=0; i<nodes.size(); i++){
			Node nodei = nodes.get(i);
			for(Dag g: this.setOfauxG){
				// Checking total amount of inversions. We add -1 to give relevance to nodes that are already sinks.
				List<Node> children = g.getChildren(nodei);
				inversion += (children.size()-1);

				// Checking edge additions from parents of each child to nodei and from parents of nodei to children.
				ArrayList<Edge> inserted = new ArrayList<>();
				List<Node> paX = g.getParents(nodei);
				for(Node child: children){
					List<Node> paY = g.getParents(child);
					// For each parent of nodei, check if it has an edge to the child
					for(Node nodep: paX){
						if(g.getEdge(nodep, child)==null){
							addition++;
						}
					}
					// For each parent of the child, check if it has an edge to nodei
					for(Node nodec: paY){
						if(!nodec.equals(nodei)){
							// If there is no edge between nodec and nodei, we consider adding it
							if((g.getEdge(nodec,nodei)==null) && (g.getEdge(nodei,nodec)==null)){
								Edge toBeInserted = new Edge(nodec,nodei,Endpoint.CIRCLE,Endpoint.CIRCLE);
								boolean contains = false;
								// Checking if we have already added this edge to the list of inserted edges
								// to avoid counting it multiple times.
								for(Edge e: inserted){
									if((e.getNode1().equals(nodec) && (e.getNode2().equals(nodei))) || 
									  ((e.getNode1().equals(nodei) && (e.getNode2().equals(nodec))))){
										contains = true;
										break;
									}
								}
								// Checkin if there is a new edge addition, we update the counter and the list of inserted edges if so.
								if(!contains){
									addition++;
									inserted.add(toBeInserted);
								}
							}
						}
					}
				}
			}
			// Calculate total changes for the current node
			changes = inversion + addition;
			// If the current node has less changes than the minimum found so far, we update the minimum and the index of the node
			// to be added to the order.
			if(changes < min){
				min = changes;
				indexNode = i;
			}
			// Resetting changes for the next iteration
			inversion = 0;
			addition = 0;
		}
		return indexNode;
	}
	
	/**
	 * Removes a node from the DAG and updates the edges according to a new node added to the alpha order.
	 * It removes a sink node and updates the edges to maintain the directed paths in the DAG.
	 * This is done each iteration of the heuristic to compute the alpha order.
	 * @param g the DAG from which the node is to be removed.
	 * @param nodeAlpha the node to be removed from the DAG.
	 */
	private void removeNode(Dag g, Node nodeAlpha){
		
		List<Node> children = g.getChildren(nodeAlpha);
		
		while(!children.isEmpty()){
			// 1. Select a child that prevents  a cycle when nodeAlpha <- child is added.
			Node child = selectChild(g, nodeAlpha, children);

			// 2. Cover the edge nodeAlpha -> child by adding edges from parents of nodeAlpha to child and from parents of child to nodeAlpha. Last of all we revert the edge nodeAlpha -> child.
			// This is done to maintain the directed paths in the DAG.
			coverEdge(g, nodeAlpha, child);

			// 3. Delete the child from the list of children of nodeAlpha, as it has been processed.
			children.remove(child);
		}
		// Finally, remove the nodeAlpha from the DAG.
		g.removeNode(nodeAlpha);
	}

	/**
	 * Selects a child node from the list of children of nodeAlpha that does not create a cycle when an edge from nodeAlpha to the child is added (nodeAlpha <- child).
	 * @param g the DAG from which the child is to be selected.
	 * @param nodeAlpha the node from the alpha order heuristic.
	 * @param children the remaining children of nodeAlpha in the DAG.
	 * @return the selected child node that does not create a cycle when an edge from nodeAlpha to the child is added.
	 */
	private Node selectChild(Dag g, Node nodeAlpha, List<Node> children) {
		int i=0;
		Node child;
		boolean endCondition;
		do{
			child = children.get(i++);
			g.removeEdge(nodeAlpha, child);
			endCondition=false;
			if(g.paths().existsDirectedPath(nodeAlpha,child)){
				endCondition=true;
				g.addEdge(new Edge(nodeAlpha,child,Endpoint.TAIL, Endpoint.ARROW));
			}
		}while(endCondition);
		return child;
	}

	/**
	 * Covers the edge from nodeAlpha to child by adding edges from parents of nodeAlpha to child and from parents of child to nodeAlpha.
	 * This is done to maintain the directed paths in the DAG after removing nodeAlpha.
	 * @param g the DAG where the edge is to be covered.
	 * @param nodeAlpha the node from the alpha order heuristic.
	 * @param child the child node selected from the list of children of nodeAlpha.
	 */
	private void coverEdge(Dag g, Node nodeAlpha, Node child) {
		// Getting the parents of nodeAlpha and child.
		List<Node> paX = g.getParents(nodeAlpha);
		List<Node> paY = g.getParents(child);
		paY.remove(nodeAlpha);
		
		// Adding edges from parents of nodeAlpha to child and from parents of child to nodeAlpha.
		for(Node nodep: paX){
			Edge pay = g.getEdge(nodep, child);
			if(pay == null)
				g.addEdge(new Edge(nodep,child,Endpoint.TAIL,Endpoint.ARROW));

		}
		
		// Adding edges from parents of child to nodeAlpha.
		for(Node nodep : paY){
			Edge paz = g.getEdge(nodep,nodeAlpha);
			if(paz == null) 
				g.addEdge(new Edge(nodep,nodeAlpha,Endpoint.TAIL,Endpoint.ARROW));
		}

		// Reverting the edge nodeAlpha -> child.
		g.addEdge(new Edge(child,nodeAlpha,Endpoint.TAIL, Endpoint.ARROW));
		
	}


	
	/**
	 * Returns the computed ancestral order of nodes.
	 * @return an ArrayList of nodes representing the ancestral order of the DAGs after applying the alpha order heuristic.
	 */
	public ArrayList<Node> getOrder(){	
		return this.alpha;
	}
}
