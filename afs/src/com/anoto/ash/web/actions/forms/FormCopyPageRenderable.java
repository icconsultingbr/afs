package com.anoto.ash.web.actions.forms;

import com.anoto.ash.AshCommons;
import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.database.UserData;
import com.anoto.ash.services.UserService;
import com.anoto.ash.utils.FormCopyRenderer;
import java.util.Hashtable;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.jboss.seam.security.Identity;


@SuppressWarnings({"unchecked", "rawtypes"})
public class FormCopyPageRenderable
{
  private Hashtable<String, String> images;

  public FormCopyPageRenderable()
  {
    this.images = new Hashtable(); }

  public void clear() {
    this.images.clear();
  }

  public String getRenderedPage(FormCopyData formCopy, int pageNumber) {
    String image = "";

    String hashKey = formCopy.getEndPageAddress() + ":" + pageNumber;

    if (this.images.get(hashKey) != null) {
      image = (String)this.images.get(hashKey);
    }
    else {
      FormCopyRenderer renderer = new FormCopyRenderer();
      ServletContext context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
      long date = System.currentTimeMillis();
      String userName = Identity.instance().getUsername();

      UserService userService = new UserService();
      UserData user = new UserData();
      user.setUserName(userName);
      user = (UserData)userService.getUser(user).get(0);

      userName = AshCommons.getChecksum(user.getUserName() + user.getFirstName() + user.getLastName());

      String root = context.getRealPath("/temp_img/" + userName + "/" + date + "/");

      List tmpImages = renderer.createImagesForUI(formCopy, Long.valueOf(date).toString(), pageNumber, root);

      image = (String)tmpImages.get(0);

      image = image.substring(root.length(), image.length());

      if (image.startsWith("\\")) {
        image = image.substring(1, image.length());
      }

      image = "/afs/temp_img/" + userName + "/" + date + "/" + image;

      hashKey = formCopy.getEndPageAddress() + ":" + pageNumber;

      this.images.put(hashKey, image);
    }

    return image;
  }
}