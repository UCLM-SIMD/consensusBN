package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.util.RandomUtil;

public class GraphTestHelper {
    

    private GraphTestHelper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates a list of random DAGs sharing the same set of nodes.
     *
     * @param numVariables Number of variables (nodes) in each DAG.
     * @param numDags Number of random DAGs to generate.
     * @param maxEdges Maximum number of edges in each DAG.
     * @param maxInDegree Maximum in-degree for each node.
     * @param maxOutDegree Maximum out-degree for each node.
     * @param maxDegree Maximum degree for each node.
     * @param connected Whether the generated DAGs should be connected.
     * @param seed Seed for random number generation.
     * @return List of randomly generated DAGs
     */
    public static List<Dag> generateRandomDagList(int numVariables, int numDags, int maxEdges, int maxInDegree, int maxOutDegree, int maxDegree, boolean connected, long seed) {
        List<Dag> randomDagsList = new ArrayList<>();

        // Create shared nodes
        List<Node> sharedNodes = new ArrayList<>();
        for (int i = 0; i < numVariables; i++) {
            sharedNodes.add(new GraphNode("Node" + i));
        }

        // Set seed
        RandomUtil.getInstance().setSeed(seed);

        // Generate DAGs
        for (int i = 0; i < numDags; i++) {
            Dag randomDag = RandomGraph.randomDag(
                    sharedNodes,
                    0,      // Latent variables (0 by default)
                    maxEdges,     
                    maxDegree,     
                    maxInDegree,     
                    maxOutDegree,     
                    connected    
            );
            randomDagsList.add(randomDag);
        }

        return randomDagsList;
    }
}
