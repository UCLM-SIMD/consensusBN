package es.uclm.i3a.simd.consensusBN;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Dag;

public class HierarchicalAgglomerativeClustererBNsTest {
        
    private ArrayList<Dag> inputDags;

    @BeforeEach
    public void setUp() {
        int numVariables = 4; // Number of variables in the DAGs
        int numDags = 10; // Number of DAGs to generate
        int maxEdges = 6; // Maximum number of edges in each DAG
        int maxInDegree = 2; // Maximum in-degree for each node
        int maxOutDegree = 2; // Maximum out-degree for each node
        int maxDegree = 3; // Maximum degree for each node
        boolean connected = true; // Whether the DAGs should be connected
        long seed = 42; // Seed for random number generation

        // Generate a list of random DAGs using GraphTestHelper
        inputDags = new ArrayList<>();
        inputDags.addAll(GraphTestHelper.generateRandomDagList(numVariables, numDags, maxEdges, maxInDegree, maxOutDegree, maxDegree, connected, seed));
    }


    @Test
    public void testConstructorAndGetSetOfBNs() {
        
        HierarchicalAgglomerativeClustererBNs clusterer = new HierarchicalAgglomerativeClustererBNs(inputDags, 2);

        assertEquals(inputDags.size(), clusterer.getSetOfBNs().size());
        assertEquals(inputDags, clusterer.getSetOfBNs());
    }

    @Test
    public void testClusterStopsEarlyDueToMaxSize() {

        HierarchicalAgglomerativeClustererBNs clusterer = new HierarchicalAgglomerativeClustererBNs(inputDags, 1);
        int numDagsAfterCluster = clusterer.cluster();

        // Only one fusion should occur since maxSize is 1
        assertEquals((int)inputDags.size()/2, numDagsAfterCluster);
    }

    @Test
    public void testGetClustersOutputAtLevelZero() {

        HierarchicalAgglomerativeClustererBNs clusterer = new HierarchicalAgglomerativeClustererBNs(inputDags, 2);
        clusterer.cluster();

        ArrayList<Dag> output = clusterer.getClustersOutput(0);
        assertEquals(inputDags.size(), output.size(), "En el nivel 0 debe haber tantos DAGs como en la entrada");
    }

    @Test
    public void testGetInsertedEdges() {
        HierarchicalAgglomerativeClustererBNs clusterer = new HierarchicalAgglomerativeClustererBNs(inputDags, 2);
        clusterer.cluster();

        int insertedEdges = clusterer.getInsertedEdges(1);
        assertTrue(insertedEdges >= 0, "Debe haber al menos 0 enlaces insertadas en el nivel 1");
    }

    @Test
    public void testComputeConsensusDag() {
        HierarchicalAgglomerativeClustererBNs clusterer = new HierarchicalAgglomerativeClustererBNs(inputDags, 2);
        int level = clusterer.cluster();

        Dag consensus = clusterer.computeConsensusDag(level);
        assertNotNull(consensus, "El DAG de consenso no debería ser null");
    }

    

    /* 
    @Test
    public void testFullClusteringUntilOneCluster() {
        // Sin restricciones de tamaño ni complejidad
        HierarchicalAgglomerativeClustererBNs clusterer = new HierarchicalAgglomerativeClustererBNs(inputDags, 0.0);
        int result = clusterer.cluster();

        // Debe haber n-1 fusiones si todo fue bien, por lo que al final solo debe quedar un cluster
        assertEquals(1, result, "Deberían haberse hecho n-1 fusiones");
        
        ArrayList<Dag> resultClusters = clusterer.getClustersOutput(result);
        assertEquals(1, resultClusters.size(), "Al final solo debe quedar un cluster");
    }

    @Test
    public void testClusteringStopsDueToComplexity() {
        ArrayList<Dag> dags = new ArrayList<>();
        dags.addAll(GraphTestHelper.generateRandomDagList(10, 2, 30, 10, 10, 10, true, 42));

        // Muy bajo el umbral de complejidad para forzar que no se fusionen
        HierarchicalAgglomerativeClustererBNs clusterer = new HierarchicalAgglomerativeClustererBNs(dags, 0.01);
        int result = clusterer.cluster();

        assertTrue(result <= 1, "El clustering debería detenerse porque los DAGs fusionados son muy complejos");
    }
        */

}


