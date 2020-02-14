package com.anoto.ash.db.dao;

import com.anoto.ash.database.Role;
import com.anoto.ash.database.UserData;
import java.util.List;

public abstract interface UserDAO
{
  public abstract int addOrUpdateUser(UserData paramUserData);

  public abstract List<UserData> getAllUsers();

  public abstract List<UserData> getAllUsersOrderedBy(String paramString);

  public abstract int getNrOfUsers(String paramString);

  public abstract Role getRole(String paramString);

  public abstract List<Role> getRoles();

  public abstract List<UserData> getUsers(UserData paramUserData);

  public abstract int removeUser(UserData paramUserData);

  public abstract UserData getUser(Long paramLong);

  public abstract boolean isPenFree(String paramString, int paramInt);
}