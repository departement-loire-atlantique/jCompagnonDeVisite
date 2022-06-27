package fr.digiwin.module.CompagnonDeVisite.dataControllers;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.jalios.jcms.BasicDataController;
import com.jalios.jcms.Channel;
import com.jalios.jcms.ControllerStatus;
import com.jalios.jcms.Data;
import com.jalios.jcms.FieldStatus;
import com.jalios.jcms.JcmsUtil;
import com.jalios.jcms.Member;
import com.jalios.jcms.plugin.Plugin;
import com.jalios.jcms.plugin.PluginComponent;
import com.jalios.jcms.WorkflowConstants;

import generated.Parcours;

/**
 * If JExplore is tick, some fields are hidden 
 * @author fdebiais
 *
 */
public class JExploreDataController extends BasicDataController implements PluginComponent {
  
  private static final Logger LOGGER = Logger.getLogger(JExploreDataController.class);
  private static final Channel CHANNEL = Channel.getChannel();
  
  public boolean init(Plugin plugin) {
    return true;
  }
  
  /*
   * Modify fields / boolean jexplore
   */
  @Override
  public void processFieldStatusMap(Data data, Member mbr, Map map) {
    LOGGER.debug(map);
    Parcours pub = (Parcours)data;
    updateFieldStatusMap(map, "etapes", setStatus(pub));
    updateFieldStatusMap(map, "thematique", setStatus(pub));
    updateFieldStatusMap(map, "duree", setStatus(pub));
    updateFieldStatusMap(map, "public1", setStatus(pub));
  }
  
  private FieldStatus setStatus(Parcours pub) {
    if (pub.getJexplore()) {
      return FieldStatus.HIDDEN;
    }
   return FieldStatus.REQUIRED;    
  }
  
  /*
   * Check fields before validation
   */
  @Override
  public ControllerStatus checkIntegrity(Data data) {

    if (!(data instanceof Parcours)) {
      return ControllerStatus.OK;
    }
    Parcours pub = (Parcours)data;
      
    if (pub.getJexplore()) {
      Set<Parcours> parcoursSet = CHANNEL.getPublicationSet(Parcours.class, CHANNEL.getDefaultAdmin());
      for (Parcours itParcours : parcoursSet) {
        if (!JcmsUtil.isSameId(pub, itParcours)
            && itParcours.getPstatus() == WorkflowConstants.PUBLISHED_PSTATUS 
            && itParcours.getJexplore() ) {
          return new ControllerStatus("Un parcours \"j'explore\" existe déjà à l'état publié : " + itParcours.getId());
        }
      }
    } else {
      if (pub.getFirstThematique(CHANNEL.getCurrentLoggedMember()) == null) {
        return new ControllerStatus(CHANNEL.getCurrentJcmsContext().glp("msg.edit.empty-field", "Thématique"));
      } else if (pub.getDuree() == 0L ) {
        return new ControllerStatus(CHANNEL.getCurrentJcmsContext().glp("msg.edit.lesser-number", "Durée", "0"));
      } else if (pub.getPublic1().length() == 0 ) {
        return new ControllerStatus(CHANNEL.getCurrentJcmsContext().glp("msg.edit.empty-field", "Public"));
      } else if (pub.getEtapes() == null ) {
        return new ControllerStatus(CHANNEL.getCurrentJcmsContext().glp("msg.edit.empty-field", "Etapes"));
      }
        
    }
    
    return ControllerStatus.OK;

  }

}
