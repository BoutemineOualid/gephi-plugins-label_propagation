package boutemineoualid.gephi.plugins.clustering.label_propagation;

import boutemineoualid.gephi.plugins.clustering.label_propagation.PropagationRules.LPAPropagationRule;
import boutemineoualid.gephi.plugins.clustering.label_propagation.PropagationRules.LPAmPropagationRule;
import boutemineoualid.gephi.plugins.clustering.label_propagation.PropagationRules.LPArPropagationRule;
import boutemineoualid.gephi.plugins.clustering.label_propagation.PropagationRules.PropagationRuleBase;
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
            this.panel.txtAnimationTimeSpan.setText("1000");
            return 1000;
        }
    }
    
    public PropagationRuleBase getPropagationRule(){
        if (this.panel.btnLPARule.isSelected())
            return new LPAPropagationRule(this.clusterer);
        else if (this.panel.btnLPArRule.isSelected())
            return new LPArPropagationRule(this.clusterer);
        else 
        {
            double resolutionParameter = 1;
            try{
                String value = this.panel.txtResolutionParameter.getText();
                Long result = Long.parseLong(value);
                resolutionParameter = result;
            }
            catch(Exception ex){
                this.panel.txtResolutionParameter.setText("1");
            }
            return new LPAmPropagationRule(this.clusterer, resolutionParameter);
        }
    }

    @Override
    public void unsetup() {
        if (this.clusterer == null){
            return;
        }
        this.clusterer.setIsAnimationEnabled(this.isAnimationEnabled());
        this.clusterer.setAnimationPauseMilliseconds(this.getAnimationPauseMilliseconds());
        this.clusterer.setPropagationRule(this.getPropagationRule());
    }

    private void initComponents() {
      panel = new LabelPropagationSettingsPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }
}
