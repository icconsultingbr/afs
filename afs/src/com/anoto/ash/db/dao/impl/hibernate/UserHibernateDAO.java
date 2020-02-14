package com.anoto.ash.db.dao.impl.hibernate;

import com.anoto.ash.database.Role;
import com.anoto.ash.database.UserData;
import com.anoto.ash.db.dao.UserDAO;
import com.anoto.ash.portal.AshUserControl;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("userDao")
@Scope(ScopeType.EVENT)
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserHibernateDAO
  implements UserDAO
{
  public int addOrUpdateUser(UserData user)
  {
    if ((user != null) && (user.getId() > 0)) {
      return AshUserControl.getInstance().updateUser(user).getUserOperationCode();
    }

    return AshUserControl.getInstance().addUser(user).getUserOperationCode();
  }

  public List<UserData> getAllUsers()
  {
    return AshUserControl.getInstance().getAllUsers();
  }

  public List<UserData> getAllUsersOrderedBy(String order) {
    return AshUserControl.getInstance().getAllUsers(order);
  }

  public List<UserData> getUsers(UserData user) {
    return AshUserControl.getInstance().getUser(user);
  }

  public int removeUser(UserData user) {
    return AshUserControl.getInstance().deleteUser(user).getUserOperationCode();
  }

  public UserData getUser(Long id) {
    return AshUserControl.getInstance().getUser(id.intValue());
  }

  public boolean isPenFree(String penId, int userId) {
    return AshUserControl.getInstance().isPenFree(penId, userId);
  }

  public Role getRole(String roleName) {
    return AshUserControl.getInstance().getRole(roleName);
  }

  public List getRoles() {
    return AshUserControl.getInstance().getRoles();
  }

  public int getNrOfUsers(String roleName) {
    return AshUserControl.getInstance().getNrOfUsers(roleName);
  }
}