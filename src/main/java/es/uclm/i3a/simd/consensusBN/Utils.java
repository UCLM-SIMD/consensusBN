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

public class Utils {
    

    /**
     * pdagToDag Algorithm From Chickering 2002: 
     * "We first consider a simple implementation of PDAG-to-DAG due to Dor and Tarsi (1992).
     *  Let NX denote the neighbors of node X in a PDAG P.
     *  We first create a DAG G that contains all of the directed edges from P, and no other edges.
     *  We then repeat the following procedure: 
     *  First, select a node X in P such that: 
     *      (1) X has no out-going edges and 
     *      (2) if NX is non-empty, then NX PaX is a clique.
     *  If P admits a consistent extension, the node X is guaranteed to exist.
     * Next, for each undirected edge Y X incident to X in P, insert a directed edge Y X to G.
     * Finally, remove X and all incident edges from the P and continue with the next node.
     * The algorithm terminates when all nodes have been deleted from P." 
     * @param graph The graph to be transformed from PDAG to DAG.
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
                if(graphAux.getChildren(node).size() != 0)
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
        }while(nodes.size() > 0);
    }


	public static boolean dSeparated(Dag g, Node x, Node y) {
		return dSeparated(g, x, y, new ArrayList<>());
	}

	public static boolean dSeparated(Dag g, Node x, Node y, List<Node> cond) {

		Set<Node> relevantNodes = findRelevantNodes(g, x, y, cond);
		Graph aux = buildInducedSubgraph(g, relevantNodes);
		moralize(aux);
		convertToUndirected(aux);
		aux.removeNodes(cond);
		return !isReachable(aux, x, y);
	}

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

	private static void convertToUndirected(Graph graph) {
		for (Edge e : new ArrayList<>(graph.getEdges())) {
			if (e.isDirected()) {
				e.setEndpoint1(Endpoint.TAIL);
				e.setEndpoint2(Endpoint.TAIL);
			}
		}
	}

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
