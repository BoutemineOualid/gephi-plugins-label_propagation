package boutemineoualid.gephi.plugins.clustering.label_propagation.helpers;

import boutemineoualid.gephi.plugins.clustering.label_propagation.LabelPropagationCluster;
import java.util.Map;
import org.gephi.graph.api.Node;


public class GraphColorizer {

    public void colorizeGraph(LabelPropagationCluster[] graphClusters) {
        if(graphClusters == null) {
            return;
        }


        for (LabelPropagationCluster cluster : graphClusters) {
            for (Node node : cluster.getNodes()) {
                Color color = cluster.getColor();
                colorizeNode(node, color);
            }
        }
    }
    
    public void colorizeNodes(Map<Node, Color> nodeColors){
        if(nodeColors == null) {
            return;
        }

        for (Map.Entry<Node, Color> nodeColor : nodeColors.entrySet()) {
            colorizeNode(nodeColor.getKey(), nodeColor.getValue());
        }
    }
    

    public void colorizeNode(Node node, Color color) {
        node.getNodeData().setColor(color.getR(), color.getG(), color.getB());
    }
}
