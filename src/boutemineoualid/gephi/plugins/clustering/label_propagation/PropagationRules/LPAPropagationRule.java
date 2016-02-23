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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Node;

/**
 * Provides an implementation of the basic LPA propagation rule as described in 
 * Raghavan, Usha Nandini, Réka Albert, and Soundar Kumara. "Near linear time algorithm to detect community structures in large-scale networks." Physical Review E 76.3 (2007): 036106.
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 */
public class LPAPropagationRule extends PropagationRuleBase {

    public LPAPropagationRule(LabelPropagationClusterer clusterer) {
        super(clusterer);
    }
   
    /**
     * This rule selects the most frequent cluster label amongst the neighbors of the processed node as its new cluster. 
     * The frequency is calculated based on the total weight of links connecting the neighbors bearing the same cluster label.
     * In case two or more labels are equally frequent, the rule selects a random one from the dominant group provided that the processed node 
     * is not already bearing a dominant one. In such a case, the rule keeps the current label of the processed node.
     */
    @Override
    public Color getDominantClusterInNeighbourhood(Node node) {
        
        Set<Node> neighbors = IteratorUtils.toSet(this.LPAClusteringEngine.getGraph().getNeighbors(node).iterator());

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
        
        if (neighborClusterWeights.isEmpty())
            return this.LPAClusteringEngine.getNodeClusterMapping().get(node); // no clusters in neighbourhood, a disconnected node.
        

        // picking the cluster with the heighest weight. In case two or more clusters exhibit the same weight, the LPA rule must pick a random one from the dominant group.
        // Shuffling the clusters list and picking the cluster with the highest weight.
        neighborClusterWeights = MapUtils.shuffle(neighborClusterWeights, this.LPAClusteringEngine.getRandomizer());
        double maxWeight = Collections.max(neighborClusterWeights.values());
        Color prevailingClusterColor = MapUtils.getKeyByValue(neighborClusterWeights, maxWeight);

        // Check whether the current node's cluster is already a dominant one.
        Color currentNodeCluster = this.LPAClusteringEngine.getNodeClusterMapping().get(node);
        if (neighborClusterWeights.containsKey(currentNodeCluster)) {
            // if it is the case, then keep it.
            if (neighborClusterWeights.get(currentNodeCluster) == maxWeight)
                prevailingClusterColor = currentNodeCluster;
        }
        
        return prevailingClusterColor;
    }    
}
