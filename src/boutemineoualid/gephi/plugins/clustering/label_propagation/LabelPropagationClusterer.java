package boutemineoualid.gephi.plugins.clustering.label_propagation;

import boutemineoualid.gephi.plugins.clustering.label_propagation.PropagationRules.LPAPropagationRule;
import boutemineoualid.gephi.plugins.clustering.label_propagation.PropagationRules.PropagationRuleBase;
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
 * Provides a the shared logic for LPA-based clustering algorithms. The user has to specify which propagation rule he's willing to use.
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 */
public class LabelPropagationClusterer implements Clusterer, LongTask {

    boolean isAnimationEnabled = false;
    long animationPauseMilliseconds = 0;
    PropagationRuleBase propagationRule = new LPAPropagationRule(this);

    public LabelPropagationClusterer(){this.randomizer = new Random(System.currentTimeMillis());
}
    
    public LabelPropagationClusterer(boolean isAnimationEnabled, long animationPauseMilliseconds, PropagationRuleBase propagationRule){
        this.randomizer = new Random(System.currentTimeMillis());
        this.isAnimationEnabled = isAnimationEnabled;
        this.animationPauseMilliseconds = animationPauseMilliseconds;
        this.propagationRule = propagationRule;
    }
    
    
    public static final String PLUGIN_NAME = "Label Propagation";
    public static final String PLUGIN_DESCRIPTION = "A plugin that provides an implementation of 3 label propagation-based clustering algorithms.";
    ProgressTicket progress = null;

    
    private boolean isCancelled = false;
    public boolean getIsCancelled(){
        return isCancelled;
    }
    
    private Graph graph;
    public Graph getGraph(){
        return graph;
    }

    private Random randomizer;
    public Random getRandomizer(){
        return randomizer;
    }
    
    // Each cluster is represented through a unique color object.
    private List<Cluster> result = new ArrayList<Cluster>();
    private Map<Node, Color> nodeClusterMapping = new HashMap<Node, Color>();
    public Map<Node, Color> getNodeClusterMapping(){
        return nodeClusterMapping;
    }
    
    GraphColorizer graphColorizer = new GraphColorizer();
        
    @Override
    public void execute(GraphModel gm) {
        this.graph = gm.getGraphVisible();
        this.graph.readLock();
        this.isCancelled = false;
        try
        {
            if (progress != null) {
                this.progress.progress(NbBundle.getMessage(LabelPropagationClusterer.class, "LabelPropagationClusterer.setup"));
                this.progress.start();
            }

            // 1. assigning each node to its own cluster.
            NodeIterator iterator = graph.getNodes().iterator();
            List<Node> nodes = new ArrayList<Node>();
            // extracting the nodes list.
            while (iterator.hasNext()){
                Node currentNode = iterator.next();
                nodes.add(currentNode);
            }
            // creating singleton clusters.
            for(Node node: nodes){
                Color cluster = new Color();
                nodeClusterMapping.put(node, cluster);
            }
            if (isAnimationEnabled)
                graphColorizer.colorizeNodes(nodeClusterMapping);
            
            if (progress != null) {
                this.progress.progress(NbBundle.getMessage(LabelPropagationClusterer.class, "LabelPropagationClusterer.buildingClusters"));
            }
            
            long waitPause = this.animationPauseMilliseconds / 2;

            // Start the clustering process
            while(!this.propagationRule.allNodesAssignedToDominantClusterInNeighbourhood() && !isCancelled){

                // shuffeling the nodes list
                Collections.shuffle(nodes); 

                // assigning the nodes to the most dominant clusters in their neighborhood according to the selected propagation rule.
                for(Node node:nodes){
                    if (isCancelled)
                        break;
    
                    if (this.isAnimationEnabled)
                    {
                        // highlighting the node.
                        node.getNodeData().setSize(node.getNodeData().getSize() * 1.5f);
                        Thread.sleep(waitPause);
                    }
                    
                    // updating the current membership of the node according to the selected propagation rule. 
                    nodeClusterMapping.put(node, this.propagationRule.getDominantClusterInNeighbourhood(node));
                    if (this.isAnimationEnabled)
                    {
                        Color cluster = nodeClusterMapping.get(node);
                        graphColorizer.colorizeNode(node, cluster);
                        Thread.sleep(waitPause);
                        NodeData nodeData = node.getNodeData();
                        nodeData.setSize(node.getNodeData().getSize() / 1.5f);
                    }
                }
            }

            prepareResults();

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
    
    private void prepareResults()
    {
        // Preparing the results.
        if (progress != null) {
            this.progress.progress(NbBundle.getMessage(LabelPropagationClusterer.class, "LabelPropagationClusterer.preparingResults"));
        }
        
        // regroup nodes into separate groups based on their labels
        result = getResultingClusters(nodeClusterMapping);
        if (result != null && result.size() > 0 && !isAnimationEnabled) {
            graphColorizer.colorizeGraph(result.toArray(new LabelPropagationCluster[0]));
        }
    }
    
    private List<Cluster> getResultingClusters(Map<Node, Color> nodeClusterMapping)
    {
        Map<Color,List<Node>> clusters = new HashMap<Color, List<Node>>();
        for(Map.Entry<Node, Color> nodesCluster : nodeClusterMapping.entrySet()) {
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
            LabelPropagationCluster cluster = new LabelPropagationCluster(clusterNodes.getValue(),
                                                                                    clusterName, clusterNodes.getKey());
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

    void setPropagationRule(PropagationRuleBase propagationRule) {
        this.propagationRule = propagationRule;
    }
}