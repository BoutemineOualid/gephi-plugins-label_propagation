package boutemineoualid.gephi.plugins.clustering.label_propagation;

import boutemineoualid.gephi.plugins.clustering.label_propagation.helpers.Color;
import java.util.ArrayList;
import java.util.List;
import org.gephi.clustering.api.Cluster;
import org.gephi.graph.api.Node;

/**
 *
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 */
public class LabelPropagationCluster implements Cluster {

  private List<Node> nodes = new ArrayList<Node>();
  private String name = "untitled";
  private Color color;
  
  private Node metaNode = null;

    public LabelPropagationCluster(List<Node> nodes, String name, Color color)
    {
        this.nodes = nodes;
        this.name = name;
        this.color = color;
    }
    
    public Color getColor(){
        return this.color;
    }
    
    @Override
    public Node[] getNodes() {
        return nodes.toArray(new Node[0]);
    }

    @Override
    public int getNodesCount() {
        return this.nodes.size();
    }

    @Override
    public String getName() {
        return this.name;
    }
    
    public void setName(String name){
        this.name = name;
    }

    @Override
    public Node getMetaNode() {
        return this.metaNode;
    }

    @Override
    public void setMetaNode(Node node) {
        this.metaNode = node;
    }
    
    public void addNode(Node node){
        this.nodes.add(node);
    }
}