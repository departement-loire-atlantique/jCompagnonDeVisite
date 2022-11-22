package fr.digiwin.module.CompagnonDeVisite.openapi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.jalios.jcms.Category;
import com.jalios.jcms.Channel;
import com.jalios.jcms.rest.DataCollectionRestResource;
import com.jalios.util.TreeNode;

import fr.digiwin.module.CompagnonDeVisite.openapi.pojo.CategoryExpo;

/**
 * Rend les catégories enfants avec le booléen d'affichage de l'exposition
 * @author fdebiais
 *
 */
public class CategoryChildren extends DataCollectionRestResource {
  
  private static final Logger LOGGER = Logger.getLogger(CategoryChildren.class);
  private static final Channel CHANNEL = Channel.getChannel();

  /**
   * Retourne le json des catégorie enfant avec le booléen d'affichage de l'exposition
   * @param Context
   * @param Request
   * @param Response
   */
  public CategoryChildren(Context ctx, Request req, Response res) {
    super(ctx, req, res);
    String idParam = (String) req.getAttributes().get("id");
    this.data = this.channel.getData(idParam);
    
    if (this.data == null || !(this.data instanceof Category)) {
      res.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unknown param");
    } else if (!this.checkReadRight(this.data)) {
      res.setStatus(
          this.getLoggedMember() == null ? Status.CLIENT_ERROR_FORBIDDEN : Status.CLIENT_ERROR_UNAUTHORIZED);
    } else {
      HashSet<Object> result = new HashSet<>();
      Iterator<?> itCatChildren;
      if (this.data instanceof TreeNode) {
        Collection<? extends TreeNode> catChildren = ((TreeNode) this.data).getTreeChildren();
        itCatChildren = catChildren.iterator();
  
        while (itCatChildren.hasNext()) {
          TreeNode catTreeNode = (TreeNode) itCatChildren.next();
          if (catTreeNode instanceof Category) {
            result.add(new CategoryExpo((Category) new CategoryExpo((Category) catTreeNode)));
          } else {
            LOGGER.warn("Invalide instance type '" + catTreeNode == null
                ? null
                : catTreeNode.getClass().getName() + "', it should be an instance of Data");
          }
        }
      }

      this.pagerData.setCollection(result);
      this.pagerData.setItemTagName("");
    }
  }
}

