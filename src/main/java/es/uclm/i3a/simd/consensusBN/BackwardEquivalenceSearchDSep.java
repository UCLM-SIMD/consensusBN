package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.utils.GraphSearchUtils;
import edu.cmu.tetrad.search.utils.MeekRules;
import static es.uclm.i3a.simd.consensusBN.Utils.pdagToDag;

/**
 * This class implements the Backward Equivalence Search with D-Separation
 * algorithm for consensus Bayesian networks. It uses an implementation of
 * second phase of the Greedy Equivalence Search (GES) algorithm, the Backward
 * Equivalence Search (BES), to refine a consensus DAG by removing edges while
 * ensuring that the resulting graph remains a Directed Acyclic Graph (DAG).
 * Since no data is available, the algorithm relies on D-separation to
 * determine whether two nodes are conditionally independent given a set of
 * other nodes. For this, the algorithm uses the set of input DAGs to check
 * whether the deletion of an edge maintains the d-separation condition.
 */
public class BackwardEquivalenceSearchDSep {
	/**
	 * The graph representing the consensus DAG after applying the Backward
	 * Equivalence Search with D-separation.
	 * This graph is built from the union of the transformed input DAGs and is
	 * refined by removing edges based on d-separation checks.
	 * 
	 * @see ConsensusUnion
	 * @see TransformDags
	 */
    private final Graph graph;

	/**
	 * List of initial DAGs used to check how many edges are deleted.
	 */
    private final ArrayList<Dag> transformedDags;

	/**
	 * List of initial DAGs used to check the d-separation condition.
	 * This list is used to verify whether the deletion of an edge maintains the
	 * d-separation condition across all input DAGs.
	 * 
	 * @see Utils#dSeparated(Dag, Node, Node, List)
	*/
    private final ArrayList<Dag> initialDags;

	/**
	 * The output DAG after applying the Backward Equivalence Search with D-separation.
	 * This DAG is the final result after removing edges from the consensus DAG
	 * while ensuring that the d-separation condition is maintained using the input DAGs.
	 * 
	 * @see Utils#dSeparated(Dag, Node, Node, List)
	 */
    private Dag outputDag;

	/**
	 * A map to store the local scores for edge deletions.
	 * This map is used to cache the scores of edge deletions to avoid redundant calculations.
	 * The key is a string representation of the edge and its conditioning set, and the value is the score.
	 */
    private final Map<DSeparationKey, Double> localScore = new HashMap<>();

	/**
	 * Number of edges removed during the backward equivalence search process.
	 * This variable keeps track of the total number of edges that are inserted (deleted) during the 
	 * Backward Equivalence Search with D-separation process.
	 * 
	 * @see ConsensusUnion#getNumberOfInsertedEdges()
	 * @see BackwardEquivalenceSearchDSep#applyBackwardEliminationWithDSeparation()
	 */
    private int numberOfRemovedEdges = 0;

	/**
	 * Percentage threshold for edge deletion. By default, it is set to 1.0, set to another value for an heuristic search
	 */
	private double percentage = 1.0;

	/**
	 * Maximum size of the conditioning set for edge deletion. Set to Integer.MAX_VALUE by default. Another value can be set for an heuristic search.
	 */
	private int maxSize = Integer.MAX_VALUE;

	/**
	 * Constructor for BackwardEquivalenceSearchDSep that initializes the properties for the search with a union DAG and lists of initial and transformed DAGs.
	 * 
	 * @param union The resulting union DAG from the ConsensusUnion process.
	 * @param initialDags List of initial DAGs used to check the d-separation condition.
	 * @param transformedDags List of transformed DAGs after applying the alpha order.
	 */
    public BackwardEquivalenceSearchDSep(Dag union, ArrayList<Dag>initialDags, ArrayList<Dag> transformedDags) {
        this.graph = new EdgeListGraph(new LinkedList<>(union.getNodes()));
        for (Edge edge : union.getEdges()) {
            graph.addEdge(edge);
        }
        this.initialDags = initialDags;
        this.transformedDags = transformedDags;
    }

	/**
	 * Applies the Backward Equivalence Search with D-separation to the consensus DAG.
	 * This method iteratively removes edges from the consensus DAG while ensuring that the d-separation condition is maintained across all input DAGs.
	 * It returns the final output DAG after all possible edge deletions.
	 * @return The output DAG after applying the Backward Equivalence Search with D-separation.
	 */
    public Dag applyBackwardEliminationWithDSeparation(){
		double score = 0;
        EdgeCandidate bestCandidate;
        
		// Creating a pdag from the graph
		rebuildPattern(graph);

		// While there are edges to delete, search for the best edge to delete
		do {
			// Make sure that any undirected edge is transformed into two directed edges
			List<Edge> edges = cleanUndirectedEdges();

			// Find the best edge to delete
			bestCandidate = calculateBestCandidateEdge(edges, score);
			/* for (Edge edge : edges) {
				// Getting candidate edge to delete			
				Node candidateTail = Edges.getDirectedEdgeTail(edge);
				Node candidateHead = Edges.getDirectedEdgeHead(edge);

				List<Node> hNeighbors = getHNeighbors(candidateTail, candidateHead, graph);
				PowerSet hSubsets= PowerSetFabric.getPowerSet(candidateTail,candidateHead,hNeighbors);

				while(hSubsets.hasMoreElements()) {
					HashSet<Node> hSubset=hSubsets.nextElement();

					// Checking if {naYXH} \ {hSubset} is a clique
					List<Node> naYXH = findNaYX(candidateTail, candidateHead, graph);
					naYXH.removeAll(hSubset);
					if (!isClique(naYXH, graph)) {
		                   continue;
					}

					// Calculating the score of the candidate edge deletion
					double deleteEval = deleteEval(candidateTail, candidateHead, hSubset, graph);
					
					// Setting limit for deleteEval
					if (!(deleteEval >= 1.0)) deleteEval = 0.0;

					// If the score is not better than the best score, continue
					double evalScore = score + deleteEval;
					if (!(evalScore > bestScore)) {
						continue;
					}

					// Updating variables for the best edge deletion
					bestScore = evalScore;
					bestTail = candidateTail;
					bestHead = candidateHead;
					bestSetParents = hSubset;
				}

			} */
			// 
			if (bestCandidate != null) {
				score = executeEdgeDeletion(bestCandidate);
			}
		} while (bestCandidate != null);

		// Rebuild the pattern to ensure the final graph is a DAG		
		createOutputDag();

        return outputDag;
    }

	/**
	 * Rebuilds the input graph to ensure it is a valid pattern.
	 * This method applies the Meek rules to orient the edges and ensure that the graph is a valid pattern.
	 * It also converts the graph to a PDAG (Partially Directed Acyclic Graph)
	 * @param graph The graph to validate and rebuild as a PDAG.
	 */
    private void rebuildPattern(Graph graph) {
        GraphSearchUtils.basicCpdag(graph);
        pdag(graph);
    }


	/**
	 * Cleans the undirected edges in the graph by converting them to directed edges.
	 * This method iterates through the edges of the graph and transforms undirected edges into two directed edges,
	 * ensuring that the resulting graph maintains only directed edges.
	 * @return
	 */
	private List<Edge> cleanUndirectedEdges() {
		Set<Edge> edges1 = graph.getEdges();
		List<Edge> edges = new ArrayList<>();

		for (Edge edge : edges1) {
			Node _x = edge.getNode1();
			Node _y = edge.getNode2();

			if (Edges.isUndirectedEdge(edge)) {
				edges.add(Edges.directedEdge(_x, _y));
				edges.add(Edges.directedEdge(_y, _x));
			} else {
				edges.add(edge);
			}
		}
		return edges;
	}

	/**
	 * Calculates the best candidate edge for deletion based on the current score and the edges available.
	 * This method evaluates each edge and its possible conditioning sets to find the edge that, when deleted,
	 * results in the highest score improvement while maintaining the d-separation condition.
	 * @param edges List of edges to consider for deletion.
	 * @param score The current score before any edge deletion.
	 * @return An EdgeCandidate object representing the best edge to delete, or null if no suitable edge is found.
	 */
	private EdgeCandidate calculateBestCandidateEdge(List<Edge> edges, double score){
		double bestScore = score;
		EdgeCandidate bestCandidate = null;
		for(Edge edge : edges){
			// Getting candidate edge to delete			
			Node candidateTail = Edges.getDirectedEdgeTail(edge);
			Node candidateHead = Edges.getDirectedEdgeHead(edge);

			List<Node> hNeighbors = getHNeighbors(candidateTail, candidateHead, graph);
			PowerSet hSubsets= new PowerSet(hNeighbors);//PowerSetFabric.getPowerSet(candidateTail,candidateHead,hNeighbors);

			while(hSubsets.hasMoreElements()) {
				// Getting a HashSet<Node> of hNeighbors
				Set<Node> hSubset=hSubsets.nextElement();
				
				// Checking size of hSubset
				if (hSubset.size() > maxSize) {
					break; // Skip to next edge if the size exceeds the maximum allowed size
				}

				// Checking if {naYXH} \ {hSubset} is a clique
				List<Node> naYXH = Utils.findNaYX(candidateTail, candidateHead, graph);
				naYXH.removeAll(hSubset);
				if (!GraphUtils.isClique(naYXH, graph)) {
					continue;
				}

				// Calculating the score of the candidate edge deletion
				double deleteEval = deleteEval(candidateTail, candidateHead, hSubset, graph);
				
				// Setting limit for deleteEval
				if (deleteEval < percentage) deleteEval = 0.0;

				// If the score is not better than the best score, continue
				double evalScore = score + deleteEval;
				if (!(evalScore > bestScore)) {
					continue;
				}

				// Updating best candidate edge
				bestCandidate = new EdgeCandidate(candidateTail, candidateHead, hSubset);
				bestCandidate.score = evalScore;

				// Updating score for the best edge deletion
				bestScore = evalScore;
			}
		}
		return bestCandidate;
	}

	/**
	 * Executes the deletion of the best candidate edge from the graph.
	 * This method removes the edge from the graph and updates the local score map.
	 * It also rebuilds the pattern after the deletion and updates the number of inserted edges.
	 * @param bestCandidate The best candidate edge to delete, containing the tail, head, conditioning set, and score.
	 * @return The score after the edge deletion is executed.
	 * This score reflects the new state of the graph after the edge has been removed.
	 */
	private double executeEdgeDeletion(EdgeCandidate bestCandidate) {
		Node bestTail;
		Node bestHead;
		Set<Node> bestSetParents;
		double score;
		double bestScore;
		bestTail = bestCandidate.tail;
		bestHead = bestCandidate.head;
		bestSetParents = bestCandidate.conditioningSet;
		bestScore = bestCandidate.score;

		// Applying delete
		//System.out.println(" ");
		//System.out.println("DELETE " + graph.getEdge(bestTail, bestHead) + bestSetParents.toString() + " (" +bestScore + ")");
		//System.out.println(" ");
		delete(bestTail, bestHead, bestSetParents, graph);
		
		// Rebuilding the pattern after deleting the edge
		rebuildPattern(graph);
		
		// Updating the number of inserted edges
		int deletedEdges = 0;
		for(int g = 0; g <this.transformedDags.size(); g++){
			if(this.transformedDags.get(g).getEdge(bestTail, bestHead) != null || this.transformedDags.get(g).getEdge(bestHead, bestTail) != null) deletedEdges++;
		}
		this.numberOfRemovedEdges+= deletedEdges;

		// Updating the initial score of the iteration
		score = bestScore;
		return score;
	}

	/**
	 * Creates the output DAG from the final graph after applying the Backward Equivalence Search.
	 * This method ensures that the final graph is a valid DAG by removing any cycles and undirected edges.
	 * It converts the graph from a PDAG to a DAG and rebuilds the output DAG from the final graph.
	 * The output DAG contains all nodes and directed edges, ensuring that it is acyclic.
	 * 
	 * @see Utils#pdagToDag(Graph)
	 * @see Dag
	 */
	private void createOutputDag() {
		// Rebuild the pattern to ensure the final graph is a DAG
		pdagToDag(graph);

		// Rebuild the output DAG from the final graph
		this.outputDag = new Dag();
		for (Node node : graph.getNodes()) this.outputDag.addNode(node);
		Node nodeT, nodeH;
		for (Edge e : graph.getEdges()){
			if(!e.isDirected()) continue;
			Endpoint endpoint1 = e.getEndpoint1();
			if (endpoint1.equals(Endpoint.ARROW)){
				nodeT = e.getNode1(); 
				nodeH = e.getNode2();
			}else{
				nodeT = e.getNode2();
				nodeH = e.getNode1();
			}
			if(!this.outputDag.paths().existsDirectedPath(nodeT, nodeH)) this.outputDag.addEdge(e);
		}
	}



    /**
	 * Transforms a dag into a pdag assuming that all colliders are oriented, as well as
     * arrows dictated by time order.*
     * @param graph The graph to transform into a PDAG.
	 * @see MeekRules
	 * @see GraphSearchUtils#basicCpdag(Graph)
	 * 
     */
    private void pdag(Graph graph) {
        MeekRules rules = new MeekRules();
        rules.setMeekPreventCycles(true);
        rules.orientImplied(graph);
    }


	/**
	 * Finds all neighbors of node x that are adjacent to node y in the graph.
	 * This method retrieves the neighbors of node y that are also adjacent to node x,
	 * ensuring that the edges between them are undirected.
	 * It filters out undirected edges to ensure that only neighbors from directed edges are considered.
	 * @param x Node x to find neighbors for.
	 * @param y Node y to find neighbors for.
	 * @param graph The graph in which to find the neighbors.
	 * @return A list of nodes that are neighbors of x and y, filtered to include only neighbors from directed edges.
	 */
    private static List<Node> getHNeighbors(Node x, Node y, Graph graph) {
		List<Node> hNeighbors = new LinkedList<>(graph.getAdjacentNodes(y));
		hNeighbors.retainAll(graph.getAdjacentNodes(x));

		for (int i = hNeighbors.size() - 1; i >= 0; i--) {
			Node z = hNeighbors.get(i);
			Edge edge = graph.getEdge(y, z);
			if (!Edges.isUndirectedEdge(edge)) {
				hNeighbors.remove(z);
			}
		}

		return hNeighbors;
	}

	/**
	 * Applies the delete operation from Chickering 2002 for the edge x->y in the graph, and updates the edges
	 * connecting x and y to the nodes in the provided HashSet<Node>. This is done to ensure that the same dependency structure is maintained
	 * while removing the edge between x and y.
	 * @param tailNode The tail node of the edge to be deleted.
	 * @param headNode The head node of the edge to be deleted.
	 * @param subset The set of nodes that will be connected to the tail and head nodes after the deletion.
	 * @param graph The graph from which the edge is deleted and the connections are updated.
	 */
    private static void delete(Node tailNode, Node headNode, Set<Node> subset, Graph graph) {
        graph.removeEdges(tailNode, headNode);

        for (Node aSubset : subset) {
            if (!graph.isParentOf(aSubset, tailNode) && !graph.isParentOf(tailNode, aSubset)) {
                graph.removeEdge(tailNode, aSubset);
                graph.addDirectedEdge(tailNode, aSubset);
            }
            graph.removeEdge(headNode, aSubset);
            graph.addDirectedEdge(headNode, aSubset);
        }
    }

	/**
	 * Evaluates the impact of deleting an edge from the graph based on d-separation.
	 *
	 * This method computes a score for deleting the edge from {@code x} to {@code y},
	 * taking into account a conditioning set of nodes {@code conditioningSet}. It uses
	 * structural information from the graph to assess whether {@code y} is d-separated
	 * from {@code x} given the constructed conditioning set.
	 *
	 * @param x The source node of the edge to be deleted.
	 * @param y The target node of the edge to be deleted.
	 * @param conditioningSet The set of nodes used as conditioning variables (Z) for d-separation.
	 * @param graph The graph in which the change is being evaluated.
	 * @return The score resulting from deleting the edge, based on the given context.
	 */
    private double deleteEval(Node x, Node y, Set<Node> conditioningSet, Graph graph){
		// Setup the conditioning set for d-separation by removing the conditioning nodes from the naYX set, adding the parents of y and removing x.
		Set<Node> finalConditioningSet = new HashSet<>(Utils.findNaYX(x, y, graph));
		finalConditioningSet.removeAll(conditioningSet);
		finalConditioningSet.addAll(graph.getParents(y));
		finalConditioningSet.remove(x);
		
		// Check if y is d-separated from x given the final conditioning set in each graph. 
		return scoreGraphChangeDelete(y, x, finalConditioningSet);		
	}

	/**
	 * Checks if the deletion of an edge from {@code x} to {@code y} maintains the d-separation condition
	 * across all initial DAGs. If the edge deletion maintains d-separation, it returns a score of 1.0,
	 * otherwise it returns 0.0.
	 * 
	 * This method uses a local score map to cache results for efficiency, avoiding redundant calculations
	 * for the same edge and conditioning set.
	 * @param x The tail node of the edge to be deleted.
	 * @param y The head node of the edge to be deleted.
	 * @param conditioningSet The set of nodes used as conditioning variables (Z) for d-separation.
	 * @return A score of 1.0 if the edge deletion maintains d-separation, otherwise 0.0.
	 * 
	 * @see Utils#dSeparated(Dag, Node, Node, List)
	 * @see DSeparationKey
	 * 
	 * This method is crucial for ensuring that the edge deletion does not violate the d-separation condition,
	 * which is essential for maintaining the integrity of the Bayesian network structure.
	 */
	private double scoreGraphChangeDelete(Node x, Node y, Set<Node> conditioningSet) {
		// Check if the edge deletion has already been evaluated and cached
		DSeparationKey key = new DSeparationKey(y, x, conditioningSet);
		Double cached = localScore.get(key);
		if (cached != null) {
			return cached;
		}

		// Evaluating the d-separation condition across all initial DAGs
		double eval = 0.0;
		for (Dag g : this.initialDags) {
			if (Utils.dSeparated(g, x, y, new ArrayList<>(conditioningSet))) {
				eval++;
			}
		}
		eval = eval / (double) this.initialDags.size();

		localScore.put(key, eval);
		return eval;
	}
	/**
	 * Returns the number of edges that were inserted during the consensus union and backward equivalence search process.
	 * @return
	 */
	public int getNumberOfRemovedEdges() {
		return this.numberOfRemovedEdges;
	}

	public void setPercentage(double percentage) {
		if(percentage < 0.0 || percentage > 1.0) {
			throw new IllegalArgumentException("Percentage must be between 0.0 and 1.0");
		}
		this.percentage = percentage;
	}
	public void setMaxSize(int maxSize) {
		if(maxSize < 0) {
			throw new IllegalArgumentException("Max size must be a non-negative integer");
		}
		this.maxSize = maxSize;
	}
	public double getPercentage() {
		return this.percentage;
	}
	public int getMaxSize() {
		return this.maxSize;
	}	

	/**
	 * Class representing a candidate edge for deletion in the Backward Equivalence Search.
	 * This class encapsulates the tail and head nodes of the edge, the conditioning set used for d-separation,
	 * and the score associated with the edge deletion.
	 * 
	 * @see BackwardEquivalenceSearchDSep#applyBackwardEliminationWithDSeparation()
	 * @see Utils#dSeparated(Dag, Node, Node, List)
	 */
	private class EdgeCandidate {
		/**
		 * The tail node of the edge candidate.
		 */
		public final Node tail;

		/**
		 * The head node of the edge candidate.
		 */
		public final Node head;

		/**
		 * The conditioning set used for d-separation in the edge candidate.
		 */
		public final Set<Node> conditioningSet;

		/**
		 * The score associated with the edge candidate deletion.
		 */
		public double score;

		public EdgeCandidate(Node tail, Node head, Set<Node> conditioningSet) {
			this.tail = tail;
			this.head = head;
			this.conditioningSet = conditioningSet;
		}
    
	}

}
