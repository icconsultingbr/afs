package com.anoto.ash.web.actions.forms;

import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.database.UserData;
import com.anoto.ash.services.FormCopyService;
import com.anoto.ash.services.UserService;
import com.anoto.ash.utils.ResourceHandler;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;


@Name("inboxAction")
@Scope(ScopeType.PAGE)
@SuppressWarnings({"unchecked", "rawtypes"})
public class InboxAction
{

  @In
  FacesMessages facesMessages;

  @In(create=true)
  private FormCopyService formCopyService;

  @In(value="userService", create=true)
  UserService userService;

  @DataModel("inbox")
  private List<FormCopyData> formCopies;

  @DataModelSelection
  @In(value="selectedFormCopy", required=false, create=true)
  @Out(value="selectedFormCopy", required=false)
  private FormCopyData selectedFormCopy;

  @Factory("inbox")
  public void getInbox()
  {
    if (this.formCopies == null) {
      FormCopyData fc = new FormCopyData();

      fc.setOwner(getUserCriteria());

      this.formCopies = new ArrayList();
      this.formCopies = this.formCopyService.getInboxFormCopies(fc, -1);
    }
  }

  private UserData getUserCriteria()
  {
    if ((Identity.instance().hasRole("Admin")) || (Identity.instance().hasRole("Verifier"))) {
      return null;
    }

    UserData user = new UserData();
    user.setUserName(Identity.instance().getUsername());
    user = (UserData)this.userService.getUser(user).get(0);

    return user;
  }

  public String convertDate(Date date)
  {
    Format formatter = new SimpleDateFormat(ResourceHandler.getResource("date_Format"));

    return formatter.format(date);
  }

  public String getLockedStatus(FormCopyData formCopy)
  {
    if (formCopy.getLocked() == 1) {
      return ", locked.";
    }

    return "";
  }

  public String editFormCopy()
  {
    String page = "";

    if (this.selectedFormCopy.getFormStatus() == 1)
    {
      page = "/forms/MissingMandatoryFields.html";
    }
    else if (this.selectedFormCopy.getFormStatus() == 3)
    {
      page = "/forms/Completed.html";
    }
    else if (this.selectedFormCopy.getFormStatus() == 0)
    {
      page = "/forms/Incompleted.html";
    }
    else if (this.selectedFormCopy.getFormStatus() == 2)
    {
      page = "/forms/NotVerified.html";
    }

    return page;
  }
}