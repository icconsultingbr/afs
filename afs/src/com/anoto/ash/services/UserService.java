package com.anoto.ash.services;

import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.database.Role;
import com.anoto.ash.database.UserData;
import com.anoto.ash.db.dao.UserDAO;
import com.anoto.ash.db.dao.impl.hibernate.UserHibernateDAO;
import com.anoto.ash.portal.AshUserControl;
import com.anoto.ash.portal.result.UserResult;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;


@Name("userService")
@Scope(ScopeType.EVENT)
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserService
{
  private UserDAO userDao = new UserHibernateDAO();

  public UserData getUser(String id)
  {
    return this.userDao.getUser(Long.valueOf(id));
  }

  public List<UserData> getUser(UserData user)
  {
    return this.userDao.getUsers(user);
  }

  public List<UserData> getAllUsers()
  {
    return this.userDao.getAllUsers();
  }

  public List<UserData> getAllUsersOrderedByUserName() {
    return this.userDao.getAllUsersOrderedBy("userName");
  }

  public int getNrOfUsers(String roleName) {
    return this.userDao.getNrOfUsers(roleName);
  }

  public boolean canChangePenId(UserData user)
  {
    boolean result = false;
    FormCopyData fc = new FormCopyData();
    fc.setOwner(user);

    FormCopyService formCopyService = new FormCopyService();

    if (formCopyService.getFormCopies(fc).size() > 0) {
      result = false;
    }
    else {
      result = true;
    }

    return result;
  }

  public int removeUser(UserData user)
  {
    return this.userDao.removeUser(user);
  }

  public UserResult retreivePassword(String userName) {
    UserData user = new UserData();
    user.setUserName(userName);

    UserResult userResult = AshUserControl.getInstance().createNewUserPassword(user);

    return userResult;
  }

  public int saveUser(UserData user)
  {
    int result = this.userDao.addOrUpdateUser(user);

    if ((0 < result) && (user.getUserName().equals(Identity.instance().getUsername()))) {
      Identity.instance().setUsername(user.getUserName());
      Identity.instance().removeRole("Admin");
      Identity.instance().removeRole("User");
      Identity.instance().removeRole("User");

      Identity.instance().addRole(user.getRole().getRoleName());
    }

    return result;
  }

  public boolean createNewPassword(UserData user)
  {
    AshUserControl.getInstance().createNewUserPassword(user);
    return true;
  }

  public Role getRole(String roleName) {
    Role result = null;

    result = this.userDao.getRole(roleName);

    return result;
  }

  public List<Role> getRoles() {
    List result = new ArrayList();

    result = this.userDao.getRoles();

    return result;
  }

  public boolean isPenFree(String penId, int userId) {
    return this.userDao.isPenFree(penId, userId);
  }

  public boolean canChangeRole(String username)
  {
    return ((Identity.instance().hasRole("Admin")) && (!(Identity.instance().getUsername().equals(username))));
  }
}