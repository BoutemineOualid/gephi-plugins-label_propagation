package boutemineoualid.gephi.plugins.clustering.label_propagation;

import org.gephi.clustering.spi.ClustererUI;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.gephi.clustering.spi.Clusterer;

/**
 *
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 */
public class LabelPropagationClustererUI implements ClustererUI {
    LabelPropagationSettingsPanel panel = null;
    LabelPropagationClusterer clusterer = null;

    public LabelPropagationClustererUI() {
      initComponents();
    }

    @Override
    public JPanel getPanel() {
      return panel;
    }

    @Override
    public void setup(Clusterer clstr) {
      this.clusterer = (LabelPropagationClusterer)clstr;
    }

    public boolean isAnimationEnabled() {
        return this.panel.chkAnimateLabelPropagation.isSelected();
    }
    
    public long getAnimationPauseMilliseconds() {
        try{
            String value = this.panel.txtAnimationTimeSpan.getText();
            Long result = Long.parseLong(value);
            return Math.abs(result);
        }
        catch(Exception ex){
            return 0;
        }
    }
    
    @Override
    public void unsetup() {
        if (this.clusterer == null){
            return;
        }
        this.clusterer.setIsAnimationEnabled(this.isAnimationEnabled());
        this.clusterer.setAnimationPauseMilliseconds(this.getAnimationPauseMilliseconds());
    }

    private void initComponents() {
      panel = new LabelPropagationSettingsPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }
}
