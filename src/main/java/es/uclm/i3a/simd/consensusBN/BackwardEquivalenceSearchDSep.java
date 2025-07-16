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
        // Implement the BESd algorithm logic here
        // This is a placeholder for the actual BESd algorithm implementation
        // The algorithm should modify the graph based on the BESd logic
        rebuildPattern(graph);
		Node x, y;
		Set<Node> t = new HashSet<>();
        double score = 0;
        double bestScore = score;
		do {
			x = y = null;
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
			for (Edge edge : edges) {
				
				Node _x = Edges.getDirectedEdgeTail(edge);
				Node _y = Edges.getDirectedEdgeHead(edge);

				List<Node> hNeighbors = getHNeighbors(_x, _y, graph);
//		                List<Set<Node>> hSubsets = powerSet(hNeighbors);
				PowerSet hSubsets= PowerSetFabric.getPowerSet(_x,_y,hNeighbors);

				while(hSubsets.hasMoreElements()) {
					SubSet hSubset=hSubsets.nextElement();
					double deleteEval = deleteEval(_x, _y, hSubset, graph);
					if (!(deleteEval >= 1.0)) deleteEval = 0.0;
					double evalScore = score + deleteEval;

                    //System.out.println("Attempt removing " + _x + "-->" + _y + "(" +evalScore + ") "+ hSubset.toString());

					if (!(evalScore > bestScore)) {
						continue;
					}

					// INICIO TEST 1
					List<Node> naYXH = findNaYX(_x, _y, graph);
					naYXH.removeAll(hSubset);
					if (!isClique(naYXH, graph)) {
//		                    	hSubsets.firstTest(true); // Si pasa para H entonces pasa para cualquier H' | H' contiene H
						continue;
					}
					// FIN TEST 1

					bestScore = evalScore;
					x = _x;
					y = _y;
					t = hSubset;
				}

			}
			if (x != null) {
				System.out.println(" ");
				System.out.println("DELETE " + graph.getEdge(x, y) + t.toString() + " (" +bestScore + ")");
				System.out.println(" ");
				delete(x, y, t, graph);
				rebuildPattern(graph);
				int deletedEdges = 0;
				for(int g = 0; g <this.transformedDags.size(); g++){
					if(this.transformedDags.get(g).getEdge(x, y) != null || this.transformedDags.get(g).getEdge(y, x) != null) deletedEdges++;
				}
				this.numberOfInsertedEdges-= deletedEdges;
//				if(graph.existsDirectedCycle()){

//					System.out.println("Hay un ciclo: "+x.toString()+"  "+y.toString());
//					System.out.println("Grafo: "+graph.toString());
//					System.exit(0);
//				}
				score = bestScore;
			}
		} while (x != null);
		
//		System.out.println("Pdag: "+ graph.toString());
                pdagToDag(graph);
//		System.out.println("PdagToDag"+graph.toString());
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
//		System.out.println("DAG: "+this.outputDag.toString());
	




        return outputDag;
    }

    private void rebuildPattern(Graph graph) {
        GraphSearchUtils.basicCpdag(graph);
        pdag(graph);
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
				if(close.get(p.toString()) == null){
					if(!open.contains(p)) open.addLast(p);
				}
			}
		}
		
		return true;
	}
       
	    
    public Dag getFusion(){
    	
    	return this.outputDag;
    }
    
    public List<Node> getOrderFusion(){
    	return  this.getFusion().paths().getValidOrder(this.getFusion().getNodes(),true);
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


    
}
