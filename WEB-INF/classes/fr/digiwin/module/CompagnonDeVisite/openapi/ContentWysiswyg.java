package fr.digiwin.module.CompagnonDeVisite.openapi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Variant;

import com.jalios.jcms.Channel;
import com.jalios.jcms.rest.DataCollectionRestResource;
import com.jalios.jcms.wysiwyg.WysiwygRenderer;
import com.jalios.util.Util;

import generated.FicheArticle;

public class ContentWysiswyg extends DataCollectionRestResource {

  private static Channel CHANNEL = Channel.getChannel();
  private static final Logger LOGGER = Logger.getLogger(ContentWysiswyg.class);

  public ContentWysiswyg(Context ctx, Request req, Response res) {
    super(ctx, req, res);

    String idParam = (String) req.getAttributes().get("id");
    this.data = this.channel.getData(idParam);
    
    if (Util.isEmpty(this.data)) {
      res.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unknown param");
      return;
    }
    if (!this.data.canBeReadBy(this.getLoggedMember())) {
        res.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        return;
    }
    if (!(this.data instanceof FicheArticle)) {
        res.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return;
    }
    
    setXmlUTF8Encoding();
    this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    this.getResponse().setEntity(this.getPreferredRepresentation());
  }
  
  /**
   * Représentation en Json
   */
  @Override
  protected String getJSONRepresentation() {
    return getWysiswyg().toString();
  }
  
  /**
   * Autorise la méthode HTTP GET
   */
  @Override
  public boolean allowGet() {
    return true;
  } 
  
  private JSONObject getWysiswyg() {
    JSONObject result = new JSONObject();
    FicheArticle ficheArticle = (FicheArticle)this.data;
    if (Util.notEmpty(ficheArticle.getContenuParagraphe())) {
      String contenuJalios = ficheArticle.getContenuParagraphe()[0];
      String formatWysiswyg = WysiwygRenderer.processWysiwyg(contenuJalios, CHANNEL.getCurrentUserLocale());

      //formatWysiswyg = processWysiwygImage(formatWysiswyg);
      try {
        result.put("title", ficheArticle.getTitle());
        result.put("content", formatWysiswyg);
      } catch (JSONException e) {
        LOGGER.warn("Error parsing ", e);
      }
    }
    return result;
  }
  
  /**
   * Change link image inside Wysiswyg
   * @param contenu
   * @param pub
   * @param loggedMember
   * @return
   */
  private String processWysiwygImage(String contenu) {
    Pattern patternSrc = Pattern.compile("<img\\s+(?:[^>]*?\\s+)?src=([\"'])(.*?)\\1");
    Matcher matcherSrc = patternSrc.matcher(contenu);
    while(matcherSrc.find()) {
      LOGGER.debug("Found image tag");
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Matcher: " + matcherSrc.group(2));
      }
      contenu = Util.replace(contenu,  matcherSrc.group(2), CHANNEL.getCurrentJcmsContext().getBaseUrl() + matcherSrc.group(2));
    }
    return contenu;
  }
}
