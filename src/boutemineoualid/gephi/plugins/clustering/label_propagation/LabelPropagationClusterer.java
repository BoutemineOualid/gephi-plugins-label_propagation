package boutemineoualid.gephi.plugins.clustering.label_propagation;

import boutemineoualid.gephi.plugins.clustering.label_propagation.helpers.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.gephi.clustering.api.Cluster;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.NodeIterator;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;
/**
 *
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 */
public class LabelPropagationClusterer implements Clusterer, LongTask {

    public LabelPropagationClusterer()
    {}
    
    public LabelPropagationClusterer(boolean isAnimationEnabled, long animationPauseMilliseconds)
    {
        this.isAnimationEnabled = isAnimationEnabled;
        this.animationPauseMilliseconds = animationPauseMilliseconds;
    }
    
    boolean isAnimationEnabled = false;
    long animationPauseMilliseconds = 0;
    
    private List<Cluster> result = new ArrayList<Cluster>();
    public static final String PLUGIN_NAME = "Label Propagation";
    public static final String PLUGIN_DESCRIPTION = "Label Propagation Clustering Algorithm";
    ProgressTicket progress = null;
    boolean isCancelled = false;
    private GraphModel graphModel = null;
    
    // Colors values are the clusters.
    Map<Node, Color> nodeClusterMappings = new HashMap<Node, Color>();
    Random randomizer = new Random(System.currentTimeMillis());
    GraphColorizer graphColorizer = new GraphColorizer();

    @Override
    public void execute(GraphModel gm) {
        this.graphModel = gm;
        Graph graph = gm.getGraphVisible();
        graph.readLock();
        this.isCancelled = false;
        try
        {
            if (progress != null) {
                this.progress.progress(NbBundle.getMessage(LabelPropagationClusterer.class, "LabelPropagationClusterer.setup"));
                this.progress.start();
            }

            // Label Propagation clustering logic.
            NodeIterator iterator = graph.getNodes().iterator();

            List<Node> nodes = new ArrayList<Node>();
            // assigning each node to its proper cluster.
            while (iterator.hasNext()){
                Node currentNode = iterator.next();
                nodes.add(currentNode);
            }

            // assigning each node to its own cluster
            for(Node node: nodes){
                Color colorRandomizer = new Color();
                Color cluster = colorRandomizer.randomize();
                nodeClusterMappings.put(node, cluster);
            }
            if (isAnimationEnabled)
                graphColorizer.colorizeNodes(nodeClusterMappings);
            
            if (progress != null) {
                this.progress.progress(NbBundle.getMessage(LabelPropagationClusterer.class, "LabelPropagationClusterer.buildingClusters"));
            }

            // Start the clustering
            while(!allNodesAssignedToPrevailingClusterInNeighborhood(graph)&& !isCancelled){

                // shuffeling the nodes list
                Collections.shuffle(nodes); 

                for(Node node:nodes){
                    if (isCancelled)
                        break;
    
                    if (this.isAnimationEnabled)
                    {
                        // highlighting the node.
                        node.getNodeData().setSize(node.getNodeData().getSize() * 1.5f);
                        Thread.sleep(this.animationPauseMilliseconds / 2);
                    }
                    
                    
                    nodeClusterMappings.put(node, getPrevailingClusterInNeighbourhood(node, graph));
                    if (this.isAnimationEnabled)
                    {
                        Color cluster = nodeClusterMappings.get(node);
                        graphColorizer.colorizeNode(node, cluster);
                        Thread.sleep(this.animationPauseMilliseconds / 2);
                        NodeData nodeData = node.getNodeData();
                        nodeData.setSize(node.getNodeData().getSize() / 1.5f);
                    }
                }
            }

            PrepareResults();

            if (progress != null) {
                this.progress.finish(NbBundle.getMessage(LabelPropagationClusterer.class, "LabelPropagationClusterer.finished"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally{
            graph.readUnlockAll();
        }
    }
    
    private void PrepareResults()
    {
        // Preparing the results.
        if (progress != null) {
            this.progress.progress(NbBundle.getMessage(LabelPropagationClusterer.class, "LabelPropagationClusterer.preparingResults"));
        }
        
        result = getResultingClusters(nodeClusterMappings);
        if (result != null && result.size() > 0 && !isAnimationEnabled) {
            graphColorizer.colorizeGraph(result.toArray(new LabelPropagationCluster[0]));
        }
    }
      
    private boolean allNodesAssignedToPrevailingClusterInNeighborhood(Graph graph){
        boolean result = true;
        
        NodeIterator graphNodesIterator = this.graphModel.getGraph().getNodes().iterator();
        
        while (graphNodesIterator.hasNext() && result && !isCancelled)
        {
            Node currentNode = graphNodesIterator.next();
            result = nodeClusterMappings.get(currentNode) == getPrevailingClusterInNeighbourhood(currentNode, graph);
        }
        return result;
    }
    
    private Color getPrevailingClusterInNeighbourhood(Node node, Graph graph){
        NodeIterator neighborNodes = graph.getNeighbors(node).iterator();
        Map<Color, Integer> neighborClusterWeights = new HashMap<Color, Integer>();
        
        // Calculating the weights of clusters in the neighbourhood of the node.
        while(neighborNodes.hasNext() && !isCancelled){
            Node currentNeighbor = neighborNodes.next();
            Color neighborsCluster = this.nodeClusterMappings.get(currentNeighbor);
            Integer clusterWeight = 1;
            if (neighborClusterWeights.containsKey(neighborsCluster)) {
                clusterWeight += neighborClusterWeights.get(neighborsCluster);
            }
            neighborClusterWeights.put(neighborsCluster, clusterWeight);
        }
        
        if (neighborClusterWeights.isEmpty())
            return this.nodeClusterMappings.get(node); // no clusters in neighbourhood.
        

        // picking the cluster with the heighest weight, if the clusters have the same weight, pick one randomly.
        // Shuffling the clusters list and picking the cluster with the highest weight, this will help if more than two clusters share the same weight.
        neighborClusterWeights = MapUtils.shuffle(neighborClusterWeights, randomizer);
        Integer maxWeight = Collections.max(neighborClusterWeights.values());
        return MapUtils.getKeyByValue(neighborClusterWeights, maxWeight);
    }    
    
    private List<Cluster> getResultingClusters(Map<Node, Color> nodeClusterMappings)
    {
        Map<Color,List<Node>> clusters = new HashMap<Color, List<Node>>();
        for(Map.Entry<Node, Color> nodesCluster : nodeClusterMappings.entrySet()) {
            List<Node> clusterNodes = new ArrayList<Node>();
            Color cluster = nodesCluster.getValue();
            if (clusters.containsKey(cluster)) {
                clusterNodes = clusters.get(cluster);
            }
            
            clusterNodes.add(nodesCluster.getKey());
            clusters.put(cluster, clusterNodes);// update the list.
        }
        
        ArrayList<Cluster> clustersResult = new ArrayList<Cluster>();
        for(Map.Entry<Color, List<Node>> clusterNodes:clusters.entrySet()) {
            String clusterName = clusterNodes.getKey().getColorAsInt().toString();
            LabelPropagationCluster cluster = new LabelPropagationCluster(clusterNodes.getValue(), clusterName, clusterNodes.getKey());
            clustersResult.add(cluster);
        }
        return clustersResult;
    }

    @Override
    public Cluster[] getClusters() {
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.toArray(new Cluster[0]);
    }

    @Override
    public boolean cancel() {
        this.progress.finish(NbBundle.getMessage(LabelPropagationClusterer.class, "LabelPropagationClusterer.cancelled"));
        return this.isCancelled = true;
    }

    
    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progress = pt;
    }

    void setIsAnimationEnabled(boolean animationEnabled) {
        this.isAnimationEnabled = animationEnabled;
    }

    void setAnimationPauseMilliseconds(long animationPauseMilliseconds) {
        this.animationPauseMilliseconds = animationPauseMilliseconds;
    }
}