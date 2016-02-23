/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boutemineoualid.gephi.plugins.clustering.label_propagation.PropagationRules;

import boutemineoualid.gephi.plugins.clustering.label_propagation.LabelPropagationClusterer;
import boutemineoualid.gephi.plugins.clustering.label_propagation.helpers.Color;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterator;

/**
 * An abstract class that represents a propagation rule specific to the LPA variant being implemented.
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 */
public abstract class PropagationRuleBase {
    public LabelPropagationClusterer LPAClusteringEngine;
    
    public PropagationRuleBase (LabelPropagationClusterer clusterer) {
        this.LPAClusteringEngine = clusterer;
    }
    /**
     * Returns the dominant cluster in the neighborhood of a node according to a propagation rule specific to the LPA variant being implemented.
     */
    public abstract Color getDominantClusterInNeighbourhood(Node node);
    
    /**
     * Checks whether all nodes adopt the dominant label amongst their neighbors.
     */
    public boolean allNodesAssignedToDominantClusterInNeighbourhood(){
        boolean result = true;
        
        NodeIterator graphNodesIterator = this.LPAClusteringEngine.getGraph().getNodes().iterator();
        
        while (graphNodesIterator.hasNext() && result && !this.LPAClusteringEngine.getIsCancelled()){
            Node currentNode = graphNodesIterator.next();
            result = this.LPAClusteringEngine.getNodeClusterMapping().get(currentNode) == this.getDominantClusterInNeighbourhood(currentNode);
        }
        return result;
    }
}
