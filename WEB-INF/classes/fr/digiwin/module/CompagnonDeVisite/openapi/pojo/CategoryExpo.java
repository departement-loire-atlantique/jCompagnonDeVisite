package fr.digiwin.module.CompagnonDeVisite.openapi.pojo;

import com.jalios.jcms.Category;

/**
 * Categorie avec l'extradata d'affichage des exposition
 * @author fdebiais
 *
 */
public class CategoryExpo extends Category {
  
  private String afficheExpo;
  
  public CategoryExpo() {
    super();
  }

  public CategoryExpo(Category var1) {
    super(var1);
    this.afficheExpo = var1.getExtraData("extra.Category.compagnonDeVisite.exposition.display");
  }

  public String getAfficheExpo() {
    return afficheExpo;
  }
 
}
