/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boutemineoualid.gephi.plugins.clustering.label_propagation.PropagationRules;

import boutemineoualid.gephi.plugins.clustering.label_propagation.LabelPropagationClusterer;
import boutemineoualid.gephi.plugins.clustering.label_propagation.helpers.Color;
import boutemineoualid.gephi.plugins.clustering.label_propagation.helpers.IteratorUtils;
import boutemineoualid.gephi.plugins.clustering.label_propagation.helpers.MapUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterator;

/**
 * Provides an implementation of LPAr's propagation rule as described in 
 * Barber, Michael J., and John W. Clark. "Detecting network communities by propagating labels under constraints." Physical Review E 80.2 (2009): 026129.
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 */
public class LPArPropagationRule extends PropagationRuleBase {

    public LPArPropagationRule(LabelPropagationClusterer clusterer) {
        super(clusterer);
    }

    /**
     * This rule selects the most frequent cluster label amongst the neighbors of the processed node as its new cluster. 
     * The frequency is calculated based on the total weight of links connecting the neighbors bearing the same cluster label.
     * In case two or more labels are equally frequent, the rule selects a random one from the dominant group regardless of the current label of the processed node.
     */
    @Override
    public Color getDominantClusterInNeighbourhood(Node node) {
        Set<Node> neighbors = IteratorUtils.toSet(this.LPAClusteringEngine.getGraph().getNeighbors(node).iterator());
        if (neighbors.isEmpty())
            return this.LPAClusteringEngine.getNodeClusterMapping().get(node); // no clusters in neighbourhood, a disconnected node.

        Map<Color, Double> neighborClusterWeights = new HashMap<Color, Double>();
        
        // Calculating the clusters' weights in the neighbourhood of the processed node.
        for(Node currentNeighbor:neighbors){
            if (this.LPAClusteringEngine.getIsCancelled())
                break;
            
            Color neighboringCluster = this.LPAClusteringEngine.getNodeClusterMapping().get(currentNeighbor);
            // Although the original LPA algorithm doesn't consider the weight on edges, Barber and Clark 
            // in (Detecting network communities by propagating labels under constraints. Physical Review E,80(2),026129.)
            // indicate that the discrete nature of the objective function being optimized by LPA makes it possible to support weighted networks too.
            double clusterWeight = this.LPAClusteringEngine.getGraph().getEdge(node, currentNeighbor).getWeight();
            
            if (neighborClusterWeights.containsKey(neighboringCluster)) {
                clusterWeight += neighborClusterWeights.get(neighboringCluster);
            }
            neighborClusterWeights.put(neighboringCluster, clusterWeight);
        }

        // picking the cluster with the heighest weight. In case two or more clusters exhibit the same weight, the LPA rule must pick a random one from the dominant group.
        // Shuffling the clusters list and picking the cluster with the highest weight.
        // This rule selects a random dominant cluster regardless the current cluster of the processed node.
        neighborClusterWeights = MapUtils.shuffle(neighborClusterWeights, this.LPAClusteringEngine.getRandomizer());
        double maxWeight = Collections.max(neighborClusterWeights.values());
        Color prevailingClusterColor = MapUtils.getKeyByValue(neighborClusterWeights, maxWeight);
        return prevailingClusterColor;
    }
    
     /**
     * Returns a list containing the dominant cluster labels in the neighborhood of a node.
     * Used to check whether each node is already bearing a dominant cluster label among its neighborhood.
     */
    private List<Color> getDominantClustersInNeighbourhood(Node node) {
        NodeIterator neighborNodes = this.LPAClusteringEngine.getGraph().getNeighbors(node).iterator();
        Map<Color, Double> neighborClusterWeights = new HashMap<Color, Double>();
        
        // Calculating clusters weights in the processed node's neighbourhood.
        while(neighborNodes.hasNext() && !this.LPAClusteringEngine.getIsCancelled()) {
            Node currentNeighbor = neighborNodes.next();
            Color neighborCluster = this.LPAClusteringEngine.getNodeClusterMapping().get(currentNeighbor);
            
            double clusterWeight = this.LPAClusteringEngine.getGraph().getEdge(node, currentNeighbor).getWeight();
            
            if (neighborClusterWeights.containsKey(neighborCluster)) {
                clusterWeight += neighborClusterWeights.get(neighborCluster);
            }
            neighborClusterWeights.put(neighborCluster, clusterWeight);
        }
        
        // returning the clusters
        ArrayList<Color> result = new ArrayList<Color>();
        
        if (neighborClusterWeights.isEmpty()){ // disconnected node, must be assigned to its own cluster.
            Color prevailingCluster = this.LPAClusteringEngine.getNodeClusterMapping().get(node);
            result.add(prevailingCluster);
            return result;
        }
        
        // selecting the dominant clusters based on the combined weight of the edges linking their members to the processed node.
        double maxWeight = Collections.max(neighborClusterWeights.values());
        for (Map.Entry<Color, Double> neighborClusterWeight:neighborClusterWeights.entrySet()) {
            double currentWeight = neighborClusterWeight.getValue();
            if (currentWeight == maxWeight) {
                result.add(neighborClusterWeight.getKey());
            }
        }
        return result;
    }
    
    @Override
    public boolean allNodesAssignedToDominantClusterInNeighbourhood(){
        boolean result = true;
        
        NodeIterator graphNodesIterator = this.LPAClusteringEngine.getGraph().getNodes().iterator();
        
        while (graphNodesIterator.hasNext() && result && !this.LPAClusteringEngine.getIsCancelled()) {
            Node currentNode = graphNodesIterator.next();
            List<Color> prevailingClusters = this.getDominantClustersInNeighbourhood(currentNode);
            result = prevailingClusters.contains(this.LPAClusteringEngine.getNodeClusterMapping().get(currentNode));
        }
        return result;
    }
}
