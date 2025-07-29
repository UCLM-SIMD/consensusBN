package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.LinkedList;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import static es.uclm.i3a.simd.consensusBN.Utils.pdagToDag;

/**
 * The {@code HeuristicConsensusMVoting} class implements a consensus structure learning algorithm
 * based on a heuristic majority voting strategy.
 * <p>
 * Given a collection of Directed Acyclic Graphs (DAGs), this class aggregates the structures
 * by counting the frequency of directed edges across all graphs and selecting those
 * that meet or exceed a specified threshold.
 * <p>
 * The heuristic helps reduce noise by filtering out weakly supported edges, and it resolves
 * conflicts (such as bidirectional edges) by applying majority rules or tie-breaking strategies.
 * <p>
 * Typical use cases include combining results from different structure learning algorithms,
 * bootstrapping runs, or expert-curated networks.
 * <p>
 * The resulting output is a new DAG that aims to reflect the most consistent edges found
 * across the input graphs.
 *
 * <p><b>Note:</b> Input DAGs must share the same set of nodes for the consensus to be meaningful.
 *
 * Example usage:
 * <pre>{@code
 * List<Dag> inputGraphs = Arrays.asList(dag1, dag2, dag3);
 * HeuristicConsensusMVoting consensus = new HeuristicConsensusMVoting(inputGraphs, threshold);
 * Dag consensusDag = consensus.getConsensusGraph();
 * }</pre>
 *
 */
public class HeuristicConsensusMVoting {

	/**
	 * List of variables (nodes) in the consensus DAG.
	 * This list is derived from the nodes of the input DAGs.
	 */
	private ArrayList<Node> variables = null;

	/**
	 * The resulting output DAG after applying the heuristic consensus voting.
	 * This DAG contains the edges that were selected based on the majority voting strategy.
	 */
	private Dag outputDag = null;

	/**
	 * List of input DAGs used to compute the consensus.
	 * These DAGs are expected to have the same set of nodes.
	 */
	private ArrayList<Dag> setOfdags = null;

	/**
	 * Percentage threshold for edge inclusion in the consensus DAG.
	 * An edge is included if it appears in at least this percentage of the input DAGs.
	 */
	private double percentage = 1.0;

	/**
	 * Weight matrix representing the frequency of edges between pairs of nodes.
	 * The weight[i][j] indicates how many times the edge from node i to node j appears in the input DAGs.
	 */
	private double [][] weight = null;
	
	/**
	 * Constructor for HeuristicConsensusMVoting.
	 * Initializes the variables, output DAG, input DAGs, and weight matrix.
	 * @param setOfdags the list of input DAGs to be fused.
	 * @param percentage the percentage threshold for edge inclusion in the consensus DAG.
	 */
	public HeuristicConsensusMVoting(ArrayList<Dag> setOfdags, double percentage) {
			this.variables = (ArrayList<Node>) setOfdags.get(0).getNodes();
			this.outputDag = null;
			this.setOfdags = setOfdags;
			this.percentage = percentage;
			this.weight = new double[this.variables.size()][this.variables.size()];
			setup();
		}
	/**
	 * Sets up the HeuristicConsensusMVoting instance by validating the input DAGs
	 * and building the weight matrix based on the edges present in the input DAGs.
	 */
	private void setup() {
		// Ensuring that the input DAGs are not null and have the same set of nodes
		validateInputDags();
		buildWeightMatrix();
	}

	/**
	 * Validates the input DAGs to ensure they are not null and have the same set of nodes.
	 * Throws an IllegalArgumentException if any validation fails.
	 */
	private void validateInputDags() {
		if (this.setOfdags == null || this.setOfdags.isEmpty())
			throw new IllegalArgumentException("Input DAGs cannot be null or empty.");
		for(Dag dag : this.setOfdags) {
			if (dag.getNodes().size() != this.variables.size()) {
				throw new IllegalArgumentException("All input DAGs must have the same number of nodes.");
			}
			if (!dag.getNodes().containsAll(this.variables)) {
				throw new IllegalArgumentException("All input DAGs must have the same set of nodes.");
			}
		}
	}
	
	/**
	 * Builds the weight matrix based on the edges present in the input DAGs.
	 * Each entry weight[i][j] is incremented for each directed edge from node i to node j
	 * across all input DAGs, normalized by the number of input DAGs
	 */
	private void buildWeightMatrix() {
		ArrayList<Graph> pdags = new ArrayList<>();
		for(Dag g: this.setOfdags){
			Graph graph = new EdgeListGraph(new LinkedList<>(g.getNodes()));
			for(Edge e: g.getEdges()){
				graph.addEdge(e);
			}
			pdagToDag(graph);
			pdags.add(graph);
		}

		for(Graph pd: pdags){
			for(Edge e: pd.getEdges()){
				if(e.isDirected()){
					Node from = Edges.getDirectedEdgeTail(e);
					Node to = Edges.getDirectedEdgeHead(e);
					this.weight[variables.indexOf(from)][variables.indexOf(to)] += (double) (1.0/this.setOfdags.size());
					// if(e.getEndpoint1() == Endpoint.ARROW){
					// 	this.weight[variables.indexOf(n2)][variables.indexOf(n1)]+= (double) (1.0/this.setOfdags.size());
					// }else{
					// 	this.weight[variables.indexOf(n1)][variables.indexOf(n2)]+= (double) (1.0/this.setOfdags.size());
					// }
				}else{
					Node n1 = e.getNode1();
					Node n2 = e.getNode2();
					this.weight[variables.indexOf(n2)][variables.indexOf(n1)]+= (double) (1.0/this.setOfdags.size());
					this.weight[variables.indexOf(n1)][variables.indexOf(n2)]+= (double) (1.0/this.setOfdags.size());
				}
			}
		}
	}

	/**
	 * Performs the fusion of the input DAGs using a heuristic majority voting strategy.
	 * The method iteratively selects edges based on their weights and adds them to the output DAG
	 * until no more edges meet the specified percentage threshold.
	 * @return The resulting consensus DAG after applying the heuristic voting.
	 */
	public Dag fusion(){
		this.outputDag = new Dag(variables);

		while(true){
			int bestEdgei = -1; // Best edge head node index
			int bestEdgej = -1; // Best edge tail node index
			double maxW = 0.0; // Maximum weight found in the current iteration
		
			// Find the best edge based on the weight matrix
			for(int i = 0; i<this.variables.size(); i++)
				for(int j = 0; j<this.variables.size(); j++){
					if(this.weight[i][j] >= maxW){
						if((this.weight[i][j] > maxW) || ((this.weight[i][j]==maxW) && (Math.random()>0.5))){
							bestEdgei = i;
							bestEdgej = j;
							maxW = this.weight[i][j];
						}
					}
				}
			// Stop if no edge meets the threshold
			if(bestEdgei == -1 || bestEdgej == -1 || maxW < percentage || maxW == 0.0) 
				break;
			
			// Add edge if it doesn't introduce a cycle
			Node from = variables.get(bestEdgei);
			Node to = variables.get(bestEdgej);
			if(!this.outputDag.paths().existsDirectedPath(to, from)){
				this.outputDag.addEdge(new Edge(from,to,Endpoint.TAIL,Endpoint.ARROW));
			} 
			// Mark the edge as used by setting its weight to zero
			this.weight[bestEdgei][bestEdgej] = 0;
		}
		
		return this.outputDag;
	}

	/**
	 * Returns the nodes (variables) of the consensus DAG.
	 * @return A list of nodes representing the variables in the consensus DAG.
	 */
	public ArrayList<Node> getVariables() {
		return variables;
	}

	/**
	 * Returns the resulting consensus DAG after applying the heuristic voting.
	 * @return The output DAG containing the selected edges based on the majority voting strategy.
	 */
	public Dag getOutputDag() {
		return outputDag;
	}

	/**
	 * Returns the list of input DAGs used to compute the consensus.
	 * @return A list of DAGs that were used as input for the consensus voting.
	 */
	public ArrayList<Dag> getSetOfdags() {
		return setOfdags;
	}

	/**
	 * Returns the percentage threshold used for edge inclusion in the consensus DAG.
	 * @return The percentage threshold for edge inclusion.
	 */
	public double getPercentage() {
		return percentage;
	}
	/**
	 * Returns the weight matrix representing the frequency of edges between pairs of nodes.
	 * Each entry weight[i][j] indicates the weight that the edge from node i to node j has in the input DAGs.
	 * @return The weight matrix used in the consensus voting process.
	 * @see #fusion()
	 */
	public double[][] getWeight() {
		return weight;
	}

		
}
