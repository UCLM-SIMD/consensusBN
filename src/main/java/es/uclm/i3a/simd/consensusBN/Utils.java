package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;

/**
 * Utility class providing static methods for graph operations, particularly for transforming PDAGs to DAGs and checking d-separation.
 * This class includes methods for converting a PDAG to a DAG, checking if two nodes are d-separated in a DAG, and finding neighbors of nodes.
 */
public final class Utils {
    

	/**
	 * Transforms a PDAG (Partially Directed Acyclic Graph) into a DAG (Directed Acyclic Graph)
	 * using the algorithm proposed by Dor and Tarsi (1992), as presented in Chickering (2002).
	 *
	 * <p>The algorithm proceeds as follows:
	 * <ol>
	 *   <li>Let NX be the set of neighbors of node X in the PDAG P.</li>
	 *   <li>Create a new DAG G containing all the directed edges from P (and no others).</li>
	 *   <li>Iteratively repeat the following steps:
	 *     <ol type="a">
	 *       <li>Select a node X such that:
	 *         <ul>
	 *           <li>(1) X has no outgoing directed edges in P, and</li>
	 *           <li>(2) if NX is non-empty, then NX ∪ Pa(X) forms a clique.</li>
	 *         </ul>
	 *         Such a node is guaranteed to exist if P admits a consistent extension.</li>
	 *       <li>For each undirected edge Y—X incident to X in P, orient it as Y → X in G.</li>
	 *       <li>Remove node X and all its incident edges from P.</li>
	 *     </ol>
	 *   </li>
	 *   <li>The algorithm terminates when all nodes have been removed from P.</li>
	 * </ol>
	 *
	 * @param graph The input PDAG to be converted into a DAG.
	 */
    public static void pdagToDag(Graph graph){
        // First create a DAG G that contains all of the directed edges from the PDAG, and no other edges.
        Graph graphAux = new EdgeListGraph(graph);
        List<Edge> undirectedEdges = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if(!edge.isDirected())
                undirectedEdges.add(edge);
            }
        graph.removeEdges(undirectedEdges);

        // We now repeat the following procedure: for each node in the pdag, check if it has no outgoing edges and if its neighbors form a clique.
        // If it does, we add the directed edges from the undirected edges to the graph. Y->X to G.
        List<Node> nodes = graphAux.getNodes();
        do{
            Node x = null; 
            for(Node node : nodes){
                x = node;
                //Checking if the node has no outgoing edges
                if(!graphAux.getChildren(node).isEmpty())
                    continue;
                //Checking if the neighbors form a clique
                if(!GraphUtils.isClique(graphAux.getAdjacentNodes(x), graphAux))
                    continue;
                // If these conditions are met, we add directed edges Y->X to G, where Y are nodes that are neighbors of X in the PDAG.
                for(Node neighbor : graphAux.getAdjacentNodes(x)){
                    if(!neighbor.equals(x)){
                        // Adding the directed edge from neighbor to node
                        graph.addDirectedEdge(neighbor, x);
                    }
                }
                // Finally, we remove the node and all incident edges from the PDAG and continue with the next node.
                graphAux.removeNode(node);
                break; // We break the loop to start again with the new graphAux without the removed node.
            }
            nodes.remove(x);
        }while(!nodes.isEmpty());
    }

	/**
	 * Checks if two nodes in a DAG are d-separated given an empty set of conditioning nodes.
	 * @param g The DAG to check for d-separation.
	 * @param x The first node.
	 * @param y The second node.
	 * @return True if the nodes are d-separated, false otherwise.
	 */
	public static boolean dSeparated(Dag g, Node x, Node y) {
		return dSeparated(g, x, y, new ArrayList<>());
	}

	/**
	 * Checks if two nodes in a DAG are d-separated given a set of conditioning nodes.
	 * This method uses a defensive copy of the conditioning set to ensure immutability.
	 * It builds an induced subgraph of the DAG containing only the relevant nodes and checks if there is a path between the two nodes that does not pass through the conditioning nodes.
	 * The method first finds the relevant nodes in the DAG, builds an induced subgraph, moralizes it,
	 * converts it to an undirected graph, and finally checks if the two nodes are reachable from each other in the undirected graph after removing the conditioning nodes.
	 * @param g The DAG to check for d-separation.
	 * @param x The first node.
	 * @param y The second node.
	 * @param cond The list of conditioning nodes.
	 * @return True if the nodes are d-separated, false otherwise.
	 */
	public static boolean dSeparated(Dag g, Node x, Node y, List<Node> cond) {

		Set<Node> relevantNodes = findRelevantNodes(g, x, y, cond);
		Graph aux = buildInducedSubgraph(g, relevantNodes);
		moralize(aux);
		convertToUndirected(aux);
		aux.removeNodes(cond);
		return !isReachable(aux, x, y);
	}

	/**
	 * Finds the relevant nodes in the DAG that are needed to check d-separation between two nodes x and y, given a set of conditioning nodes.
	 * This method performs a depth-first search starting from nodes x and y, and includes all nodes that are reachable from either x or y, as well as the conditioning nodes.
	 * The method uses a stack to explore the graph and a set to keep track of visited nodes, ensuring that
	 * all relevant nodes are included in the final set.
	 * @param g The DAG in which to find the relevant nodes.
	 * @param x The first node.
	 * @param y The second node.
	 * @param cond The list of conditioning nodes.
	 * @return A set of nodes that are relevant for checking d-separation between x and y, including the conditioning nodes.
	 */
	private static Set<Node> findRelevantNodes(Dag g, Node x, Node y, List<Node> cond) {
		Set<Node> visited = new HashSet<>();
		Deque<Node> stack = new ArrayDeque<>();

		stack.push(x);
		stack.push(y);
		for (Node c : cond) stack.push(c);

		while (!stack.isEmpty()) {
			Node current = stack.pop();
			if (visited.add(current)) {
				for (Node parent : g.getParents(current)) {
					stack.push(parent);
				}
			}
		}

		return visited;
	}

	/**
	 * Builds an induced subgraph from the given DAG containing only the nodes specified in the set {@code nodesToKeep}.
	 * The method creates a new graph that includes all nodes in {@code nodesToKeep} and all directed edges between them that exist in the original graph.
	 * It ensures that only directed edges are included, and undirected edges are ignored.
	 * @param g The original DAG from which to build the induced subgraph.
	 * @param nodesToKeep The set of nodes to include in the induced subgraph.
	 * This set should contain the nodes that are relevant for the d-separation check.
	 * @return A new graph representing the induced subgraph containing only the specified nodes and their directed edges.
	 * This graph is a subgraph of the original DAG, containing only the nodes in {@code nodesToKeep} and the directed edges between them that exist in the original graph.
	 */
	private static Graph buildInducedSubgraph(Dag g, Set<Node> nodesToKeep) {
		Graph subgraph = new EdgeListGraph();

		for (Node node : g.getNodes()) {
			if (nodesToKeep.contains(node)) {
				subgraph.addNode(node);
			}
		}

		for (Edge e : g.getEdges()) {
			if (!e.isDirected()) continue;

			Node tail = e.getNode1();
			Node head = e.getNode2();

			if (nodesToKeep.contains(tail) && nodesToKeep.contains(head)) {
				subgraph.addEdge(new Edge(tail, head, e.getEndpoint1(), e.getEndpoint2()));
			}
		}

		return subgraph;
	}

	/**
	 * Moralizes the given graph by adding undirected edges between all pairs of parents of each child node.
	 * This process ensures that the resulting graph is undirected and that all parents of each child are connected.
	 * The moralization is done by iterating over each child node, retrieving its parents, and adding undirected edges between every pair of parents.
	 * This is a crucial step in preparing the graph for d-separation checks, as it ensures that the graph structure reflects the necessary connections between parents of child nodes.
	 * @param graph The graph to moralize.
	 */
	private static void moralize(Graph graph) {
		for (Node child : graph.getNodes()) {
			List<Node> parents = graph.getParents(child);
			int n = parents.size();
			if (n <= 1) continue;

			for (int i = 0; i < n - 1; i++) {
				for (int j = i + 1; j < n; j++) {
					Node p1 = parents.get(i);
					Node p2 = parents.get(j);
					if (!graph.isAdjacentTo(p1, p2)) {
						graph.addUndirectedEdge(p1, p2);
					}
				}
			}
		}
	}

	/**
	 * Converts all directed edges in the graph to undirected edges.
	 * This method iterates through all edges in the graph and changes their endpoints to TAIL,
	 * effectively removing the directionality of the edges. This is useful for certain graph operations
	 * where the direction of edges is not relevant, such as when checking connectivity or performing undirected graph algorithms.
	 * @param graph The graph to convert.
	 */
	private static void convertToUndirected(Graph graph) {
		for (Edge e : new ArrayList<>(graph.getEdges())) {
			if (e.isDirected()) {
				e.setEndpoint1(Endpoint.TAIL);
				e.setEndpoint2(Endpoint.TAIL);
			}
		}
	}

	/**
	 * Checks if there is a path between two nodes in the graph.
	 * This method performs a depth-first search starting from the {@code start} node and checks
	 * if it can reach the {@code target} node. It uses a stack to explore the graph and a set to keep track of visited nodes,
	 * ensuring that it does not revisit nodes.
	 * If the target node is found during the search, it returns true; otherwise,
	 * it returns false after exhausting all possible paths.
	 * @param g The graph to search.
	 * @param start The starting node.
	 * @param target The target node.
	 * @return True if there is a path from start to target, false otherwise.
	 */
	private static boolean isReachable(Graph g, Node start, Node target) {
		Set<Node> visited = new HashSet<>();
		Deque<Node> stack = new ArrayDeque<>();
		stack.push(start);

		while (!stack.isEmpty()) {
			Node current = stack.pop();
			if (current.equals(target)) return true;
			if (visited.add(current)) {
				for (Node neighbor : g.getAdjacentNodes(current)) {
					if (!visited.contains(neighbor)) {
						stack.push(neighbor);
					}
				}
			}
		}

		return false;
	}


    /**
	 * Finds the nodes that are neighbors of node y and x in the graph.
	 * This method retrieves the neighbors of node y that are also adjacent to node x,
	 * ensuring that the edges between them are directed.
	 * It filters out undirected edges to ensure that only neighbors from directed edges are considered.
	 * @param x Node x to find neighbors for.
	 * @param y Node y to find neighbors for.
	 * @param graph The graph in which to find the neighbors.
	 * @return A list of nodes that are neighbors of x and y, filtered to include only neighbors from directed edges.
	 */
    public static List<Node> findNaYX(Node x, Node y, Graph graph) {
        List<Node> naYX = new LinkedList<>(graph.getAdjacentNodes(y));
        naYX.retainAll(graph.getAdjacentNodes(x));

        for (int i = naYX.size()-1; i >= 0; i--) {
            Node z = naYX.get(i);
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                naYX.remove(z);
            }
        }

        return naYX;
    }
}
