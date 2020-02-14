package com.anoto.ash.web.actions.users;

import com.anoto.ash.database.UserData;
import com.anoto.ash.services.UserService;
import com.anoto.ash.web.validators.PasswordValidator;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;

@Name("myProfileAction")
@Scope(ScopeType.PAGE)
@SuppressWarnings("unused")
public class MyProfileAction
{

  @In
  FacesMessages facesMessages;

  @In(create=true)
  private UserService userService;

  @In(required=false, create=true)
  @Out(value="loggedInUser", required=false)
  private UserData loggedInUser;

  @In(required=false, create=true)
  @Out(value="password", required=false)
  private String password;

  @In(required=false, create=true)
  @Out(value="confirmPassword", required=false)
  private String confirmPassword;

  @In(create=true)
  private ViewUserAction viewUserAction;

  public MyProfileAction()
  {
    this.password = "";

    this.confirmPassword = "";
  }

  public void populateUser()
  {
    this.loggedInUser = new UserData();
    this.loggedInUser.setUserName(Identity.instance().getUsername());
    this.loggedInUser = ((UserData)this.userService.getUser(this.loggedInUser).get(0));
    this.password = PasswordValidator.fakePassword;
    this.confirmPassword = this.password;
  }

  public boolean canChangePenId() {
    return this.userService.canChangePenId(this.loggedInUser);
  }

  public String saveUser()
  {
	  loggedInUser.setPassword(password);
      if(password.equals(PasswordValidator.fakePassword))
      {
          loggedInUser.setPassword("");
      }
      
      if(0 > userService.saveUser(loggedInUser))
      {
          facesMessages.add("#{messages.save_failed}", new Object[0]);
      }

      return "/main.xhtml";
  }

  public String clear() {
    return "/main.xhtml";
  }
}