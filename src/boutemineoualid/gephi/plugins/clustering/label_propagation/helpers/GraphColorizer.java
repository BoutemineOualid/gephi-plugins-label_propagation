package boutemineoualid.gephi.plugins.clustering.label_propagation.helpers;

import boutemineoualid.gephi.plugins.clustering.label_propagation.LabelPropagationCluster;
import org.gephi.graph.api.Node;


public class GraphColorizer {

    public void colorizeGraph(LabelPropagationCluster[] result) {
        if(result == null) {
            return;
        }


        for (LabelPropagationCluster cluster : result) {
            for (Node n : cluster.getNodes()) {
                Color color = cluster.getColor();
                n.getNodeData().setColor(color.getR(), color.getG(), color.getB());
            }
        }
    }
}
