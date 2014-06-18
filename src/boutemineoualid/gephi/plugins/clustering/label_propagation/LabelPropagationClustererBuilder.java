package boutemineoualid.gephi.plugins.clustering.label_propagation;

import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererBuilder;
import org.gephi.clustering.spi.ClustererUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 */
@ServiceProvider(service = ClustererBuilder.class)
public class LabelPropagationClustererBuilder implements ClustererBuilder {

    LabelPropagationClustererUI settingsUI = new LabelPropagationClustererUI();
    
    @Override
    public Clusterer getClusterer() {
      return new LabelPropagationClusterer();
    }

    @Override
    public String getName() {
      return LabelPropagationClusterer.PLUGIN_NAME;
    }

    @Override
    public String getDescription() {
      return LabelPropagationClusterer.PLUGIN_DESCRIPTION;
    }

    @Override
    public Class getClustererClass() {
      return LabelPropagationClusterer.class;
    }

    @Override
    public ClustererUI getUI() {
      return settingsUI;
    }
}
