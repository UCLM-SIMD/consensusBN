package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
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
    
}
