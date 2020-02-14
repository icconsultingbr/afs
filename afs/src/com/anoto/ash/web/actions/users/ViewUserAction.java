package com.anoto.ash.web.actions.users;

import com.anoto.ash.database.UserData;
import com.anoto.ash.services.UserService;
import com.anoto.ash.web.logger.AshWebUILogger;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;

@Name("viewUserAction")
@Scope(ScopeType.PAGE)
@SuppressWarnings("all")
public class ViewUserAction
{

  @In
  FacesMessages facesMessages;

  @In(create=true)
  private UserService userService;

  @DataModel
  private List<UserData> users;

  @DataModelSelection
  @In(value="users", required=false, scope=ScopeType.PAGE)
  @Out(value="users", required=false, scope=ScopeType.PAGE)
  private UserData selectedUser;

  @Factory("users")
  public void findUsers()
  {
    AshWebUILogger.info(super.getClass(), "Retreiving users...");
    this.users = this.userService.getAllUsersOrderedByUserName();
    AshWebUILogger.info(super.getClass(), "...found " + this.users.size());
  }

  public String viewUsers() {
    findUsers();
    return "/users/ViewUsers.xhtml";
  }

  public void saveUser() {
    AshWebUILogger.info(super.getClass(), "Saving new user data, for user: " + this.selectedUser.getUserName());
    int result = this.userService.saveUser(this.selectedUser);

    if (0 < result)
    {
      this.selectedUser = ((UserData)this.userService.getUser(this.selectedUser).get(0));
    }
    else
      this.facesMessages.add("#{save_failed}", new Object[0]);
  }

  private UserData getUser(String username)
  {
    UserData user = new UserData();
    user.setUserName(username);

    List userList = this.userService.getUser(user);

    if (userList.size() > 0) {
      return ((UserData)userList.get(0));
    }

    this.facesMessages.add("Failed to retreive the user, " + username, new Object[0]);
    return user;
  }

  public ArrayList getRoleOptions()
  {
    List listOptions = new ArrayList();

    listOptions.add(new SelectItem("Admin", "Admin"));
    listOptions.add(new SelectItem("Verifier", "Verifier"));
    listOptions.add(new SelectItem("User", "User"));

    return ((ArrayList)listOptions);
  }

  public String clear() {
    this.selectedUser = new UserData();
    return "/main.xhtml";
  }
}