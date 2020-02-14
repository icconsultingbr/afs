package com.anoto.ash.database;

import com.anoto.ash.AshLogger;
import com.anoto.ash.AshProperties;
import com.anoto.ash.portal.AshSettingsControl;
import com.anoto.ash.portal.ThresholdControl;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class UserDBHandler extends DBHandler
{
  public static synchronized int plainAuthorization(UserData userData)
  {
    List<?> userList = getUser(userData.getUserName());
    int userId = 0;
    AshLogger.log("Authorizing user: " + userData.getUserName());

    if (userList.size() == 0)
    {
      AshLogger.log("No user with that user name was found.");
      return -5; }
    if (userList.size() == 1)
    {
      UserData storedUserData = (UserData)userList.get(0);

      if (storedUserData.getNbrOfFailedLogins() >= AshSettingsControl.getInstance().getSettings().getMaxFailedPortalLogins()) {
        AshLogger.log("The maximum number of failed logins has been reached.");
        return -6;
      }

      if (storedUserData.getLockedAsBoolean()) {
        AshLogger.log("The user is locked.");
        return -13;
      }

      String submittedPassword = userData.getPassword().trim();
      String storedPassword = storedUserData.getPassword().trim();

      if (!(submittedPassword.equals(storedPassword)))
      {
        if (storedUserData.getNbrOfFailedLogins() >= AshSettingsControl.getInstance().getSettings().getMaxFailedPortalLogins())
        {
          return -6;
        }
        return -5;
      }

      if (storedUserData.getNbrOfFailedLogins() > 0) {
        AshLogger.logFine(storedPassword);
        storedUserData.setNbrOfFailedLogins(0);
        updateUser(storedUserData);
        AshLogger.logFine("Reset of number of failed logins for user: " + storedUserData.getUserName());
      }

      userId = storedUserData.getId();
    }
    else
    {
      AshLogger.logSevere("An error when authorizing a user: " + userData.toString());
      throw new IllegalStateException("Severe Database failure. Please contact System Administrator.");
    }

    return userId;
  }

  public static synchronized void handleFailedLogin(UserData userData) {
    int nbrOfFailedLogins = userData.getNbrOfFailedLogins();
    ++nbrOfFailedLogins;
    userData.setNbrOfFailedLogins(nbrOfFailedLogins);

    updateUser(userData);
  }

  public static synchronized int authorizeUser(UserData userData)
  {
    List<?> userList = getUser(userData.getUserName());
    int userId = 0;
    AshLogger.log("Authorizing user: " + userData.getUserName());

    if (userList.size() == 0)
    {
      AshLogger.log("No user with that user name was found.");
      return -5; }
    if (userList.size() == 1)
    {
      UserData storedUserData = (UserData)userList.get(0);

      if (storedUserData.getNbrOfFailedLogins() >= AshSettingsControl.getInstance().getSettings().getMaxFailedPortalLogins()) {
        AshLogger.log("The maximum number of failed logins has been reached.");
        return -6;
      }

      if (storedUserData.getLockedAsBoolean()) {
        AshLogger.log("The user is locked.");
        return -13;
      }

      String submittedPassword = userData.getPassword().trim();
      String storedPassword = storedUserData.getPassword().trim();

      if (!(submittedPassword.equals(storedPassword)))
      {
        storedUserData.setNbrOfFailedLogins(storedUserData.getNbrOfFailedLogins() + 1);

        AshLogger.log("The user entered wrong password.");
        int retVal = -5;
        if (storedUserData.getNbrOfFailedLogins() >= AshSettingsControl.getInstance().getSettings().getMaxFailedPortalLogins()) {
          storedUserData.setLocked(1);

          retVal = -6;
        }
        updateUser(storedUserData);
        return retVal;
      }

      if (storedUserData.getNbrOfFailedLogins() > 0) {
        AshLogger.logFine(storedPassword);
        storedUserData.setNbrOfFailedLogins(0);
        updateUser(storedUserData);
        AshLogger.logFine("Reset of number of failed logins for user: " + storedUserData.getUserName());
      }

      userId = storedUserData.getId();
    }
    else
    {
      AshLogger.logSevere("An error when authorizing a user: " + userData.toString());
      throw new IllegalStateException("Severe Database failure. Please contact System Administrator.");
    }

    return userId;
  }

  public static synchronized int deleteUser(UserData userData) throws HibernateException
  {
    AshLogger.logFine("Deleting user: " + userData.toString());

    if (getUser(userData.getId()) == null) {
      AshLogger.log("Error when trying to delete a user: " + getUser(userData.getId()).toString());
      return -4;
    }
    if (getUser(userData.getId()).getRole().getRoleName().equalsIgnoreCase("Admin"))
    {
      UserData tempUser = new UserData();

      tempUser.setRole(getRole("Admin"));

      List<?> admins = searchUser(tempUser);
      int numberOfAdmins = admins.size();

      if (numberOfAdmins < 2) {
        AshLogger.log("Error when trying to delete a user: " + getUser(userData.getId()).toString());
        return AshProperties.ONE_ADMIN_MUST_EXIST;
      }
    }

    ThresholdData threshold = new ThresholdData();
    threshold.setUser(getUser(userData.getId()));

    List<ThresholdData> thresholds = ThresholdControl.getInstance().getAllThresholds();

    for (ThresholdData currentThreshold : thresholds) {
      if ((currentThreshold.getUser() != null) && (currentThreshold.getUser().getId() == getUser(userData.getId()).getId())) {
        ThresholdControl.getInstance().deleteThreshold(currentThreshold);
      }

    }

    int nrOfFormCopies = 0;
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);
    Long formCopies = (Long)session.createQuery("select count(f.formCopyId) from FormCopyData f where f.owner=" + getUser(userData.getId()).getId()).uniqueResult();

    nrOfFormCopies = formCopies.intValue();

    if (1 > nrOfFormCopies)
    {
      session.delete(getUser(userData.getId()));
    }

    commitTransaction(session);
    closeSession();

    if (1 <= nrOfFormCopies)
    {
      userData.setUserStatus(AshProperties.USER_STATUS_REMOVED);
      String tmpUserName = userData.getUserName() + "_removed";
      int i = 1;
      while (getUser(tmpUserName + i).size() > 0)
        ++i;

      tmpUserName = tmpUserName + i;
      userData.setPenId("");
      userData.setRole(getRole("User"));
      userData.setEmail("");
      userData.setUserName(tmpUserName);

      openSession();
      session = getCurrentSession();
      beginTransaction(session);
      session.update(userData);
      commitTransaction(session);
      closeSession();
    }
    return 0;
  }

  public static synchronized ArrayList<?> getAllUsers(String orderSyntax)
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);
    List<?> result = session.createQuery("from UserData where userStatus > 0 " + orderSyntax).list();
    AshLogger.logFine("Retrieved all users, number of users returned = " + result.size());
    commitTransaction(session);
    closeSession();

    return new ArrayList<Object>(result);
  }

  public static synchronized ArrayList<?> getAllUsers()
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);
    List<?> result = session.createQuery("from UserData where userStatus > 0").list();
    AshLogger.logFine("Retrieved all users, number of users returned = " + result.size());
    commitTransaction(session);
    closeSession();

    return new ArrayList<Object>(result);
  }

  public static synchronized int getNrOfUsers(String roleName)
    throws HibernateException
  {
    int nrOfUsers = 0;
    Role role = getRole(roleName);
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);
    Long users = (Long)session.createQuery("select count(users.id) from UserData users where users.userStatus > 0 and users.role=" + role.getId()).uniqueResult();

    if (null != users)
    {
      nrOfUsers = users.intValue();
    }

    AshLogger.logFine("Retrieved count of users with role: " + roleName + " count: " + nrOfUsers);
    commitTransaction(session);
    closeSession();
    return nrOfUsers;
  }

  public static synchronized List<?> searchUser(UserData user) throws HibernateException {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Searching for the user: " + user.toString());
    Criteria searchCriteria = SearchUserFactory.createCriteria(session, user);
    List<?> result = searchCriteria.list();

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized int addUser(UserData userData) throws HibernateException
  {
    int receivedUserId = -1;

    if (getUser(userData.getUserName()).size() > 0) {
      AshLogger.log("Error when adding a new user, the user name was already taken: " + userData.getUserName());

      return -3;
    }

    if ((userData.getPenId() != null) && (!(userData.getPenId().equals(""))) && (getUserByPenId(userData.getPenId()).size() > 0)) {
      AshLogger.log("Tried to add a user using an existing pen id: " + userData.toString());

      return -7;
    }
    userData.setUserStatus(AshProperties.USER_STATUS_ALIVE_AND_WELL);
    openSession();

    Session session = getCurrentSession();

    beginTransaction(session);

    Integer Id = (Integer)session.save(userData);
    try
    {
      receivedUserId = Id.intValue();
      AshLogger.logFine("Added a new user: " + userData.toString());
    } catch (Exception e) {
      AshLogger.logSevere("Error when trying to add a user: " + e.getMessage());
      receivedUserId = -1;
    }

    commitTransaction(session);

    closeSession();

    return receivedUserId;
  }

  public static synchronized int updateUser(UserData userData)
    throws HibernateException
  {
	/*
    UserData storedUserData = getUser(userData.getId());
    
    if (storedUserData == null) {
      AshLogger.log("Error when trying to update user, no such user: " + userData.toString());
      return -4;
    }

    if ((storedUserData.getRole().getRoleName().equalsIgnoreCase("Admin")) && 
      (!(storedUserData.getRole().getRoleName().equalsIgnoreCase(userData.getRole().getRoleName()))))
    {
      int numberOfAdmins = getNrOfUsers("Admin");
      if (numberOfAdmins < 2)
      {
        AshLogger.log("Error when trying to update a user: " + userData.toString());
        return AshProperties.ONE_ADMIN_MUST_EXIST;
      }

    }

    if ((!(storedUserData.getUserName().equals(userData.getUserName()))) && 
      (getUser(userData.getUserName()).size() > 0))
    {
      AshLogger.log("Error when trying to change the user name, the new user name already exists: " + userData.toString());
      return -3;
    }

    if ((storedUserData.getPenId() == null) || ((userData.getPenId() != null) && (!(storedUserData.getPenId().equals(userData.getPenId())))))
    {
      if ((userData.getPenId() != null) && (!(userData.getPenId().equals(""))) && (getUserByPenId(userData.getPenId()).size() > 0)) {
        AshLogger.log("Tried to add a user using an existing pen id: " + userData.toString());

        return -7;
      }

      FormCopyData formCopyData = new FormCopyData();
      formCopyData.setOwner(userData);

      if (FormDBHandler.searchFormCopy(formCopyData, -1, "", 3).size() > 0) {
        AshLogger.log("Error when trying to change the pen id, the pen id already belongs to another user: " + userData.getPenId());

        return -8;
      }
    }
    */

    openSession();

    Session session = getCurrentSession();

    beginTransaction(session);

    session.saveOrUpdate(userData);

    AshLogger.logFine("Updated a user: " + userData.toString());

    commitTransaction(session);

    closeSession();

    return 0;
  }

  public static synchronized UserData getUser(int userId) throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    UserData userData = (UserData)session.get(UserData.class, new Integer(userId));

    if (userData != null)
      AshLogger.logFine("Retrieved a user by id: " + userData.toString());
    else {
      AshLogger.logFine("No user found with id: " + userId);
    }

    commitTransaction(session);
    closeSession();

    return userData;
  }

  /*
  private static synchronized List getUser(String userName, String password) throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    Query query = session.createQuery("from UserData where userName like:userName and password like:password");
    query.setString("userName", userName);
    query.setString("password", password);
    List result = query.list();
    AshLogger.logFine("Retrieved users by user name and password, number of users retrieved = " + result.size());

    commitTransaction(session);
    closeSession();

    return result;
  }
  */

  public static synchronized List<?> getUser(String userName) throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    Query query = session.createQuery("from UserData where userName like:userName");
    query.setString("userName", userName);
    List<?> result = query.list();
    AshLogger.logFine("Retrieved users by user name, number of users retrieved = " + result.size());

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized List<?> getUserByPenId(String penId) throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    Query query = session.createQuery("from UserData where penId like:penId");
    query.setString("penId", penId);
    List<?> result = query.list();

    AshLogger.logFine("Retrieved Portal Users by Pen ID, number of users retrieved = " + result.size());

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized Role getRole(String roleName)
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    Query query = session.createQuery("from Role where roleName like:roleName");
    query.setString("roleName", roleName);
    Role result = (Role)query.uniqueResult();

    AshLogger.logFine("Retreived a role with role name = " + result.getRoleName());

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized List<?> getRoles() {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    Query query = session.createQuery("from Role role");
    List<?> result = query.list();

    AshLogger.logFine("Retreived a all role objects from databse, number of records = " + result.size());

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized boolean isPenFree(String penId, int userId)
  {
    boolean penIsFree = true;
    List<?> udl = getUserByPenId(penId);
    for (int i = 0; i < udl.size(); ++i) {
      UserData ud = (UserData)udl.get(i);
      if (ud.getId() != userId) {
        penIsFree = false;
        break;
      }
    }
    return penIsFree;
  }

  public static class SearchUserFactory
  {
    public static Criteria createCriteria(Session session, UserData user)
    {
      if ((session != null) && (user != null)) {
        Criteria crit = session.createCriteria(UserData.class);

        AshLogger.logFine("Searching for a user, using criteria:");

        if ((user.getRole() != null) && (user.getRole().getRoleName().length() > 0)) {
          crit.add(Restrictions.eq("role", user.getRole()));
          AshLogger.logFine("Role = " + user.getRole().getRoleName());
        }

        if ((user.getEmail() != null) && (user.getEmail().length() > 0)) {
          crit.add(Restrictions.eq("email", user.getEmail()));
          AshLogger.logFine("email = " + user.getEmail());
        }

        if ((user.getFirstName() != null) && (user.getFirstName().length() > 0)) {
          crit.add(Restrictions.eq("firstName", user.getFirstName()));
          AshLogger.logFine("firstName = " + user.getFirstName());
        }

        if ((user.getLastName() != null) && (user.getLastName().length() > 0)) {
          crit.add(Restrictions.eq("lastName", user.getLastName()));
          AshLogger.logFine("lastName = " + user.getLastName());
        }

        if (user.getNbrOfFailedLogins() > -1) {
          crit.add(Restrictions.eq("nbrOfFailedLogins", Integer.valueOf(user.getNbrOfFailedLogins())));
          AshLogger.logFine("nbrOfFailedLogins = " + user.getNbrOfFailedLogins());
        }

        if ((user.getPassword() != null) && (user.getPassword().length() > 0)) {
          crit.add(Restrictions.eq("password", user.getPassword()));
          AshLogger.logFine("password = " + user.getPassword());
        }

        if ((user.getPenId() != null) && (user.getPenId().length() > 0)) {
          crit.add(Restrictions.eq("penId", user.getPenId()));
          AshLogger.logFine("penId = " + user.getPenId());
        }

        if ((user.getUserName() != null) && (user.getUserName().length() > 0)) {
          crit.add(Restrictions.eq("userName", user.getUserName()));
          AshLogger.logFine("userName = " + user.getUserName());
        }

        return crit;
      }

      return null;
    }
  }
}