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
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.utils.GraphSearchUtils;
import edu.cmu.tetrad.search.utils.MeekRules;
import static es.uclm.i3a.simd.consensusBN.Utils.pdagToDag;

public class BackwardEquivalenceSearchDSep {

    private final Graph graph;
    private final ArrayList<Dag> transformedDags;
    private final ArrayList<Dag> initialDags;
    private Dag outputDag;
    private final Map<String, Double> localScore = new HashMap<>();
    private int numberOfInsertedEdges = 0;


    public BackwardEquivalenceSearchDSep(Dag union, ArrayList<Dag>initialDags, ArrayList<Dag> transformedDags) {
        this.graph = new EdgeListGraph(new LinkedList<>(union.getNodes()));
        for (Edge edge : union.getEdges()) {
            graph.addEdge(edge);
        }
        this.initialDags = initialDags;
        this.transformedDags = transformedDags;
    }

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
					SubSet hSubset=hSubsets.nextElement();

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


    private void rebuildPattern(Graph graph) {
        GraphSearchUtils.basicCpdag(graph);
        pdag(graph);
    }

	
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

	private EdgeCandidate calculateBestCandidateEdge(List<Edge> edges, double score){
		double bestScore = score;
		EdgeCandidate bestCandidate = null;
		for(Edge edge : edges){
			// Getting candidate edge to delete			
			Node candidateTail = Edges.getDirectedEdgeTail(edge);
			Node candidateHead = Edges.getDirectedEdgeHead(edge);

			List<Node> hNeighbors = getHNeighbors(candidateTail, candidateHead, graph);
			PowerSet hSubsets= PowerSetFabric.getPowerSet(candidateTail,candidateHead,hNeighbors);

			while(hSubsets.hasMoreElements()) {
				// Getting a subset of hNeighbors
				SubSet hSubset=hSubsets.nextElement();
				
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

				// Updating best candidate edge
				bestCandidate = new EdgeCandidate(candidateTail, candidateHead, hSubset);
				bestCandidate.score = evalScore;

				// Updating score for the best edge deletion
				bestScore = evalScore;
			}
		}
		return bestCandidate;
	}

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
		System.out.println(" ");
		System.out.println("DELETE " + graph.getEdge(bestTail, bestHead) + bestSetParents.toString() + " (" +bestScore + ")");
		System.out.println(" ");
		delete(bestTail, bestHead, bestSetParents, graph);
		
		// Rebuilding the pattern after deleting the edge
		rebuildPattern(graph);
		
		// Updating the number of inserted edges
		int deletedEdges = 0;
		for(int g = 0; g <this.transformedDags.size(); g++){
			if(this.transformedDags.get(g).getEdge(bestTail, bestHead) != null || this.transformedDags.get(g).getEdge(bestHead, bestTail) != null) deletedEdges++;
		}
		this.numberOfInsertedEdges-= deletedEdges;

		// Updating the initial score of the iteration
		score = bestScore;
		return score;
	}



    /**
     * Fully direct a graph with background knowledge. I am not sure how to
     * adapt Chickering's suggested algorithm above (dagToPdag) to incorporate
     * background knowledge, so I am also implementing this algorithm based on
     * Meek's 1995 UAI paper. Notice it is the same implemented in PcSearch.
     * </p> *IMPORTANT!* *It assumes all colliders are oriented, as well as
     * arrows dictated by time order.*
     * 
     * ELIMINADO BACKGROUND KNOWLEDGE
     */
    private void pdag(Graph graph) {
        MeekRules rules = new MeekRules();
        rules.setMeekPreventCycles(true);
        rules.orientImplied(graph);
    }

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

    private static void delete(Node x, Node y, Set<Node> subset, Graph graph) {
        graph.removeEdges(x, y);

        for (Node aSubset : subset) {
            if (!graph.isParentOf(aSubset, x) && !graph.isParentOf(x, aSubset)) {
                graph.removeEdge(x, aSubset);
                graph.addDirectedEdge(x, aSubset);
            }
            graph.removeEdge(y, aSubset);
            graph.addDirectedEdge(y, aSubset);
        }
    }

    private double deleteEval(Node x, Node y, SubSet h, Graph graph){
		
		 Set<Node> set1 = new HashSet<Node>(findNaYX(x, y, graph));
	        set1.removeAll(h);
	        set1.addAll(graph.getParents(y));
	        set1.remove(x);
	        return scoreGraphChangeDelete(y, x, set1); // calcular si y esta d-separado de x dado el set1 en cada grafo.
		
	}

    private static List<Node> findNaYX(Node x, Node y, Graph graph) {
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

    private double scoreGraphChangeDelete(Node y, Node x, Set<Node> set){
		
		String key = y.getName()+x.getName()+set.toString();
		Double val = this.localScore.get(key);
		if(val == null){
			double eval = 0.0;
			LinkedList<Node> conditioning = new LinkedList<>();
			conditioning.addAll(set);
			for(Dag g: this.initialDags){
				if(!dSeparated(g,y, x, conditioning)) return 0.0;
			}
			eval = 1.0; //eval / (double) this.setOfdags.size();
			val = eval;
			this.localScore.put(key, val);
			return eval;
		}else{
			return val.doubleValue();
		}
	}

	boolean dSeparated(Dag g, Node x, Node y, LinkedList<Node> cond){
		
		LinkedList<Node> open = new LinkedList<Node>();
		HashMap<String,Node> close = new HashMap<String,Node>();
		open.add(x);
		open.add(y);
		open.addAll(cond);
		while (open.size() != 0){
			Node a = open.getFirst();
			open.remove(a);
			close.put(a.toString(),a);
			List<Node> pa =g.getParents(a);
			for(Node p : pa){
				if(close.get(p.toString()) == null){
					if(!open.contains(p)) open.addLast(p);
				}
			}
        }

        Graph aux = new EdgeListGraph();
		
		for (Node node : g.getNodes()) aux.addNode(node);
		Node nodeT, nodeH;
		for (Edge e : g.getEdges()){
			if(!e.isDirected()) continue;
			nodeT = e.getNode1();
			nodeH = e.getNode2();
			if((close.get(nodeH.toString())!=null)&&(close.get(nodeT.toString())!=null)){
				Edge newEdge = new Edge(e.getNode1(),e.getNode2(),e.getEndpoint1(),e.getEndpoint2());
				aux.addEdge(newEdge);
			}
		}
		
		close = new HashMap<String,Node>();
		for(Edge e: aux.getEdges()){
			if(e.isDirected()){
				Node h;
				if(e.getEndpoint1()==Endpoint.ARROW){
					h = e.getNode1();
				}else h = e.getNode2();
				if(close.get(h.toString())==null){
					close.put(h.toString(),h);
					List<Node> pa = aux.getParents(h);
					if(pa.size()>1){
						for(int i = 0 ; i< pa.size() - 1; i++)
							for(int j = i+1; j < pa.size(); j++){
								Node p1 = pa.get(i);
								Node p2 = pa.get(j);
								boolean found = false;
								for(Edge edge : aux.getEdges()){
									if(edge.getNode1().equals(p1)&&(edge.getNode2().equals(p2))){
										found = true;
										break;
									}
									if(edge.getNode2().equals(p1)&&(edge.getNode1().equals(p2))){
										found = true;
										break;
									}
								}
								if(!found) aux.addUndirectedEdge(p1, p2);
							}
					}
					
				}
			}
		}
		
		for(Edge e: aux.getEdges()){
			if(e.isDirected()){
				e.setEndpoint1(Endpoint.TAIL);
				e.setEndpoint2(Endpoint.TAIL);
			}
		}
		
		aux.removeNodes(cond);

		open = new LinkedList<Node>();
		close = new HashMap<String,Node>();
		open.add(x);
		while (open.size() != 0){
			Node a = open.getFirst();
			if(a.equals(y)) return false;
			open.remove(a);
			close.put(a.toString(),a);
			List<Node> pa =aux.getAdjacentNodes(a);
			for(Node p : pa){
				if(p == null) continue;
				if(close.get(p.toString()) == null){
					if(!open.contains(p)) open.addLast(p);
				}
			}
		}
		
		return true;
	}
       
    
    	
	private static boolean isClique(List<Node> set, Graph graph) {
		List<Node> setv = new LinkedList<Node>(set);
		for (int i = 0; i < setv.size() - 1; i++) {
			for (int j = i + 1; j < setv.size(); j++) {
				if (!graph.isAdjacentTo(setv.get(i), setv.get(j))) {
					return false;
				}
			}
		}
		return true;
	}

	public int getNumberOfInsertedEdges() {
		return this.numberOfInsertedEdges;
	}

	private class EdgeCandidate {
		public final Node tail;
		public final Node head;
		public final Set<Node> conditioningSet;
		public double score;

		public EdgeCandidate(Node tail, Node head, Set<Node> conditioningSet) {
			this.tail = tail;
			this.head = head;
			this.conditioningSet = conditioningSet;
		}
    
	}

}
