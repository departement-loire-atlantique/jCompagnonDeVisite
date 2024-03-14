package fr.digiwin.module.CompagnonDeVisite.openapi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Variant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jalios.jcms.json.ObjectMapperBuilder;
import com.jalios.jcms.rest.JcmsRestResource;
import com.jalios.util.Util;
import com.jalios.util.XmlUtil;

import fr.digiwin.module.CompagnonDeVisite.openapi.pojo.PropertyPOJO;

/**
 * Modification de PropertyResource
 */
public class PropApi extends JcmsRestResource {

  private static final Logger LOGGER = Logger.getLogger(PropApi.class);

  private String propName;

  public PropApi(Context ctx, Request req, Response rep) {
    super(ctx, req, rep);
    this.setXmlUTF8Encoding();
    this.getVariants().add(new Variant(MediaType.TEXT_XML));
    this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));

    // if (Util.isEmpty(this.getLoggedMember())) {
    //  rep.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    // }

    this.propName = (String) req.getAttributes().get("prop");
    if (Util.isEmpty(this.propName)) {
      rep.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    
    String[] propsPublic = channel.getStringArrayProperty("jcmsplugin.compagnonDeVisite.prop.public", null);
    if(Util.isEmpty(propsPublic) || !Arrays.asList(propsPublic).contains(propName)) {
      rep.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }
  }

  @Override
  protected String getItemXmlRepresentation(Object obj) {
    String prop = this.channel.getProperty(this.propName, (String) null);
    if (Util.notEmpty(prop)) {
      PropertyPOJO var3 = new PropertyPOJO(this.propName, prop);
      return var3.getXmlRepresentation();
    } else {
      this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      return "";
    }
  }

  @Override
  protected String getJSONRepresentation() {
    String prop = this.channel.getProperty(this.propName, (String) null);
    if (prop == null) {
      this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      return "";
    }

    PropertyPOJO var2 = new PropertyPOJO(this.propName, prop);

    try {
      ObjectMapperBuilder var3 = new ObjectMapperBuilder();
      return var3.getMapper().writeValueAsString(var2);
    } catch (JsonProcessingException var5) {
      String var4 = "Cannot convert ressource to JSON data ";
      LOGGER.warn(var4, var5);
      this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, var4);
      return "";
    }
  }

  private String getxmlProp(String name, String val) {
    String var1 = "<property name='" + XmlUtil.normalize(name) + "' ";
    var1 = var1 + ">" + (Util.isEmpty(val) ? "" : XmlUtil.normalize(val)) + "</property>\n";

    return var1;
  }

}
