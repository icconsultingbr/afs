package com.anoto.ash.web.validators;

import com.anoto.ash.AshCommons;
import com.anoto.ash.database.UserData;
import com.anoto.ash.portal.utils.ASHValidatorUtility;
import com.anoto.ash.services.UserService;
import com.anoto.ash.utils.ResourceHandler;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;


@SuppressWarnings("rawtypes")
public class UserNameValidator extends CommonValidation
  implements Validator
{
  private UserService userService;

  public void validate(FacesContext context, UIComponent component, Object object)
    throws ValidatorException
  {
    String userName = (String)object;
    UserData user = null;
    if ((context == null) || (component == null))
      throw new NullPointerException();

    if (!(component instanceof UIInput)) {
      return;
    }

    context.getExternalContext().getRequestMap().put(ID_USERNAME, object);
    if (!(ASHValidatorUtility.validMinLength(userName, 2)))
      throw new ValidatorException(new FacesMessage(ResourceHandler.getResource("invalid_user_name_too_short")));

    if (!(ASHValidatorUtility.validMaxLength(userName, 15)))
      throw new ValidatorException(new FacesMessage(ResourceHandler.getResource("invalid_user_name_too_long")));

    valididateCharacters(userName);

    String hashedPassword = null;
    this.userService = new UserService();
    String userPrefix = component.getId().substring(0, component.getId().indexOf(95));
    int id = -1;
    if (!(userPrefix.contains("add"))) {
      id = getUserId(component, userPrefix);
      user = this.userService.getUser(String.valueOf(id));
      hashedPassword = user.getPassword();
    }
    UserData userNameUser = new UserData();
    userNameUser.setUserName(userName);
    List users = this.userService.getUser(userNameUser);
    if (((user == null) && (0 < users.size())) || ((user != null) && (0 < users.size()) && (((UserData)users.get(0)).getId() != user.getId()))) {
      throw new ValidatorException(new FacesMessage(ResourceHandler.getResource("invalid_username_already_exists")));
    }

    String password = (String)context.getExternalContext().getRequestMap().get(ID_PASSWORD);
    if (null == password)
    {
      password = getUserPassword(component, userPrefix);
    }

    if ((null != hashedPassword) && (null != password) && (password.equals(PasswordValidator.fakePassword)))
    {
      String hashedUsername = AshCommons.getChecksum(userName);
      if (hashedUsername.equals(hashedPassword))
        throw new ValidatorException(new FacesMessage(ResourceHandler.getResource("invalid_username_equals_password")));
    }
  }
}