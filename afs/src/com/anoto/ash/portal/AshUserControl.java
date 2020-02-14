package com.anoto.ash.portal;

import com.anoto.ash.AshCommons;
import com.anoto.ash.AshLogger;
import com.anoto.ash.AshProperties;
import com.anoto.ash.MailControl;
import com.anoto.ash.Randomizer;
import com.anoto.ash.database.Role;
import com.anoto.ash.database.UserDBHandler;
import com.anoto.ash.database.UserData;
import com.anoto.ash.portal.result.LoginResult;
import com.anoto.ash.portal.result.UserResult;
import com.anoto.ash.portal.utils.ASHValidatorUtility;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.HibernateException;

@SuppressWarnings("rawtypes")
public class AshUserControl
{
  private static AshUserControl instance = null;

  public static AshUserControl getInstance()
  {
    if (instance == null) {
      instance = new AshUserControl();
    }

    return new AshUserControl();
  }

  public LoginResult authorizeUser(UserData userData, boolean justAuthorize)
  {
    LoginResult result = new LoginResult();

    if (!(ASHValidatorUtility.validUserName(userData.getUserName()))) {
      AshLogger.logFine("The user name contained invalid characters: " + userData.getUserName());
      result.setLoginOperationSuccessful(false);
      result.setLoginOperationMessage("Wrong user name or password.");

      return result;
    }

    String hashedPassword = AshCommons.getChecksum(userData.getPassword());
    userData.setPassword(hashedPassword);
    
    System.out.println(hashedPassword);

    AshLogger.logFine("Authorizing user: " + userData.getUserName());
    try
    {
      int loginResult = -1;

      if (!(justAuthorize))
        loginResult = UserDBHandler.authorizeUser(userData);
      else {
        loginResult = UserDBHandler.plainAuthorization(userData);
      }

      if (loginResult == -6) {
        AshLogger.logFine("Number of failed logins has exceeded for user: " + userData.getUserName());
        result.setLoginOperationSuccessful(false);
        result.setLoginOperationMessage("Maximum number of failed logins exceeded for this user. Account has been locked, please contact system administrator.");
      } else if (loginResult == -5) {
        AshLogger.logFine("Wrong password or user name for user: " + userData.getUserName());
        result.setLoginOperationSuccessful(false);
        result.setLoginOperationMessage("Wrong user name or password.");
      } else if (loginResult > 0) {
        AshLogger.logFine("Successfully authorized user: " + userData.getUserName());
        result.setLoginOperationSuccessful(true);
        userData.setId(loginResult);
      }
      else
      {
        AshLogger.logFine("Login failed for user: " + userData.getUserName());
        result.setLoginOperationSuccessful(false);
        result.setLoginOperationMessage("Wrong user name or password.");
      }
    } catch (HibernateException he) {
      AshLogger.logSevere("Error when authorizing user: " + he.getMessage());
      result.setLoginOperationSuccessful(false);
      result.setLoginOperationMessage("Severe Error. Please contact system administrator.");
    }

    result.setUserData(userData);

    return result;
  }

  public UserResult createNewUserPassword(UserData userData)
  {
    UserResult result = new UserResult();

    AshLogger.logFine("Creating new password for user: " + userData.getUserName());

    List userList = getUser(userData.getUserName());
    if (userList.size() == 0)
    {
      AshLogger.logFine("No such user: " + userData.getUserName());
      result.setUserOperationSuccessful(false);
      result.setUserOperationMessage("No Such User..");
      return result;
    }

    UserData dbUser = (UserData)userList.get(0);

    if (dbUser.getLockedAsBoolean()) {
      AshLogger.logFine("The user is locked and can't retreive an new password: " + dbUser.getUserName());
      result.setUserOperationSuccessful(false);
      result.setUserOperationMessage("This account has been locked, please contact system administrator.");
      return result;
    }

    String userEmail = dbUser.getEmail();
    if (userEmail.length() == 0) {
      AshLogger.logFine("No email address was found for that user: " + dbUser.getUserName());
      result.setUserOperationSuccessful(false);
      result.setUserOperationMessage("No email address has been set for the user with this userName. New password can not be sent.");
      return result;
    }

    String newPassword = Randomizer.generateNewPassword();

    dbUser.setPassword(newPassword);

    result = updateUser(dbUser);

    if (!(result.isUserOperationSuccessful()))
    {
      AshLogger.logFine("Failed to update the user ercord with the new password, user name: " + userData.toString());
      return result;
    }

    return result;
  }

  public UserResult addUser(UserData userData)
  {
    UserResult result = new UserResult();

    String password = userData.getPassword();
    String hashedPassword = AshCommons.getChecksum(password);

    userData.setPassword(hashedPassword);

    userData.setNbrOfFailedLogins(0);
    userData.setUserStatus(AshProperties.USER_STATUS_ALIVE_AND_WELL);
    AshLogger.logFine("Adding a new user: " + userData);
    try
    {
      int operationResult = UserDBHandler.addUser(userData);
      result.setUserOperationCode(operationResult);
      if (operationResult == -3)
      {
        AshLogger.logFine("A user with that user name already exists: " + userData.getUserName());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage("User name not unique.");
      } else if (operationResult == -7)
      {
        AshLogger.logFine("A user with the same pen id already exists, can't create more than one user with the same pen id, existing Pen Id:" + userData.getPenId());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage("The submitted pen serial already belongs to another user.");
      } else if (operationResult == -1)
      {
        AshLogger.logFine("There was an error when adding the new user: " + userData.toString());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage("Operation failed.");
      }
      else {
        userData.setId(operationResult);
        result.setUserOperationSuccessful(true);
        AshLogger.logFine("Succesfully added a new user: " + userData.toString());

        userData.setPassword(password);
        if (userData.getLocked() == 0) {
          boolean userMailSent = MailControl.getInstance().sendNewUserMail(userData);

          if (!(userMailSent))
            AshLogger.logSevere("Failed to send mail to user with username " + userData.getUserName());
        }

        userData.setPassword(hashedPassword);
      }
    }
    catch (HibernateException he) {
      AshLogger.logSevere("There was an error when adding a new user, reason: " + he.getMessage());
      result.setUserOperationSuccessful(false);
      result.setUserOperationMessage("Severe Error. Please contact system administrator.");
    }

    result.setUserData(userData);

    return result;
  }

  public UserResult updateUser(UserData userData)
  {
    AshLogger.logFine("Updating Form portal user: " + userData.toString());
    UserResult result = new UserResult();

    String password = userData.getPassword();

    UserData oldUserData = getUser(userData.getId());

    boolean userNameChanged = false;
    boolean passwordChanged = false;

    if (!(userData.getUserName().equals(oldUserData.getUserName()))) {
      userNameChanged = true;
    }

    if ((oldUserData.getLockedAsBoolean()) && (false == userData.getLockedAsBoolean()))
    {
      password = Long.toString(System.currentTimeMillis(), 36);
    }

    if ((password != null) && (!(password.equals(""))) && (!(password.equals(oldUserData.getPassword())))) {
      passwordChanged = true;
    }

    if (passwordChanged) {
      String hashedPassword = AshCommons.getChecksum(password);

      userData.setPassword(hashedPassword);

      userData.setNbrOfFailedLogins(0);

      AshLogger.logFine("Updated the user Password, user name: " + userData.getUserName());
    }
    else
    {
      userData.setPassword(oldUserData.getPassword());
    }
    try
    {
      int operationResult = UserDBHandler.updateUser(userData);
      result.setUserOperationCode(operationResult);
      if (operationResult == -4)
      {
        AshLogger.logFine("Error when updating user, no such user name: " + userData.getUserName());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage("No Such User..");
      } else if (operationResult == AshProperties.ONE_ADMIN_MUST_EXIST)
      {
        AshLogger.logFine("Couldn't delete the user, one admin must exist: " + userData.getUserName());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage(AshProperties.ONE_ADMIN_MUST_EXIST_MESSAGE);
      }
      else if (operationResult == -3)
      {
        AshLogger.logFine("Error when updating user, and user with that user name already exists: " + userData.getUserName());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage("User name not unique.");
      } else if (operationResult == -7)
      {
        AshLogger.logFine("A user with the same pen id already exists, can't create more than one user with the same pen id, existing Pen Id:" + userData.getPenId());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage("The submitted pen serial already belongs to another user.");
      } else if (operationResult == -8)
      {
        AshLogger.logFine("Tried to change the Pen ID, but that Pen ID has been used to submit form copies, Pen ID: " + userData.getPenId());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage("The pen serial may not be changed since form data has been submitted by the pen.");
      } else {
        result.setUserOperationSuccessful(true);

        if ((userNameChanged) || (passwordChanged)) {
          if (passwordChanged)
          {
            userData.setPassword(password);
          }
          else { userData.setPassword("");
          }

          if (userData.getLocked() == 0) {
            boolean userMailSent = MailControl.getInstance().sendNewUserMail(userData);

            if (!(userMailSent))
              AshLogger.logSevere("Failed to send mail to user with username " + userData.getUserName());
          }
        }
      }

    }
    catch (HibernateException he)
    {
      AshLogger.logSevere("Error when updating user, reason: " + he.getMessage());
      result.setUserOperationSuccessful(false);
      result.setUserOperationMessage("Severe Error. Please contact system administrator.");
    }

    result.setUserData(userData);
    return result;
  }

  public UserResult deleteUser(UserData userData)
  {
    UserResult result = new UserResult();

    AshLogger.logFine("Deleting user with user name: " + userData.toString());
    try
    {
      int operationResult = UserDBHandler.deleteUser(userData);
      result.setUserOperationCode(operationResult);
      if (operationResult == -4)
      {
        AshLogger.logFine("Couldn't delete the user, no such user name: " + userData.getUserName());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage("No Such User..");
      } else if (operationResult == AshProperties.ONE_ADMIN_MUST_EXIST)
      {
        AshLogger.logFine("Couldn't delete the user, one admin must exist: " + userData.getUserName());
        result.setUserOperationSuccessful(false);
        result.setUserOperationMessage(AshProperties.ONE_ADMIN_MUST_EXIST_MESSAGE);
      } else {
        AshLogger.logFine("Succesfully deleted the user with user name: " + userData.getUserName());
        result.setUserOperationSuccessful(true);
      }
    }
    catch (HibernateException he)
    {
      AshLogger.logSevere("Error when deleting user, reason: " + he.getMessage());
      result.setUserOperationSuccessful(false);
      result.setUserOperationMessage("The user could not be removed.");
    }

    result.setUserData(userData);

    return result;
  }

  public List getAllUsers() throws IllegalStateException {
    ArrayList cpUsers = new ArrayList();

    AshLogger.logFine("Retrieving all users");
    try
    {
      cpUsers = UserDBHandler.getAllUsers();
    } catch (HibernateException he) {
      AshLogger.logSevere("Error when trying to retrieve all users, reason: " + he.getMessage());
      throw new IllegalStateException("Severe Error. Please contact system administrator.");
    }

    return cpUsers;
  }

  public List getAllUsers(String sortOrder)
    throws IllegalStateException
  {
    ArrayList cpUsers = new ArrayList();

    AshLogger.logFine("Retrieving all users, with sort order: " + sortOrder);
    try
    {
      String orderSyntax = "order by " + sortOrder;
      cpUsers = UserDBHandler.getAllUsers(orderSyntax);
    } catch (HibernateException he) {
      AshLogger.logSevere("Error when trying to retrieve all Form portal users, reason: " + he.getMessage());
      throw new IllegalStateException("Severe Error. Please contact system administrator.");
    }

    return cpUsers;
  }

  public int getNrOfUsers(String roleName) {
    int count = 0;
    AshLogger.logFine("Retrieving count of users with role: " + roleName);
    try {
      count = UserDBHandler.getNrOfUsers(roleName);
    } catch (HibernateException he) {
      AshLogger.logSevere("Error when retrieving user count, reason: " + he.getMessage());
      throw new IllegalStateException("Severe Error. Please contact system administrator.");
    }
    return count;
  }

  public UserData getUser(int userId) throws IllegalStateException {
    UserData userData = null;

    AshLogger.logFine("Retrieving user with user id = " + userId);
    try
    {
      userData = UserDBHandler.getUser(userId);

      if ((userData == null) || (AshProperties.USER_STATUS_REMOVED == userData.getUserStatus())) {
        return null;
      }

    }
    catch (HibernateException he)
    {
      AshLogger.logSevere("Error when retrieving user, reason: " + he.getMessage());
      throw new IllegalStateException("Severe Error. Please contact system administrator.");
    }

    return userData;
  }

  private List getUser(String userName) throws IllegalStateException
  {
    List userList = new ArrayList();

    AshLogger.logFine("Retrieving user with user name: " + userName);
    try
    {
      userList = UserDBHandler.getUser(userName);
      AshLogger.logFine("Retrieved " + userList.size() + " number of users with that user name: " + userName);
    } catch (HibernateException he) {
      AshLogger.logSevere("Error when retreiving user, reason: " + he.getMessage());
      throw new IllegalStateException("Severe Error. Please contact system administrator.");
    }

    return userList;
  }

  public UserData getUserByPenId(String penId) throws IllegalStateException {
    List userList = new ArrayList();
    UserData userData = null;

    AshLogger.logFine("Retrieving users with Pen ID: " + penId);
    try
    {
      userList = UserDBHandler.getUserByPenId(penId);
      if (userList.size() == 1)
      {
        userData = (UserData)userList.get(0);
        AshLogger.logFine("Succesfully retrieved a users: " + userData.toString());
      } else if (userList.size() > 1) {
        throw new IllegalStateException("Severe error. Please contact System Administrator.");
      }
    } catch (HibernateException he) {
      AshLogger.logSevere("Error when retrieving user, reason: " + he.getMessage());
      throw new IllegalStateException("Severe Error. Please contact system administrator.");
    }

    return userData;
  }

  public List getUser(UserData user) {
    List result = new ArrayList();

    AshLogger.logFine("Retrieving user with search critera based on: " + user.toString());
    try
    {
      result = UserDBHandler.searchUser(user);
    } catch (HibernateException he) {
      AshLogger.logSevere("Error when retrieving Form portal users, reason: " + he.getMessage());
      throw new IllegalStateException("Severe Error. Please contact system administrator.");
    }

    return result;
  }

  public Role getRole(String roleName) {
    return UserDBHandler.getRole(roleName);
  }

  public List<?> getRoles() {
    return UserDBHandler.getRoles();
  }

  public boolean isPenFree(String penId, int userId)
  {
    return UserDBHandler.isPenFree(penId, userId);
  }
}