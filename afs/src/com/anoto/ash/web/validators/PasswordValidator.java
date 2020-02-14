package com.anoto.ash.web.validators;

import com.anoto.ash.portal.utils.ASHValidatorUtility;
import com.anoto.ash.utils.ResourceHandler;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class PasswordValidator extends CommonValidation
  implements Validator
{
  protected static String ID_PASSWORD_CONFIRM = "user_confirm_password";
  public static String fakePassword = "£@$=/¤";

  public void validate(FacesContext context, UIComponent component, Object object)
    throws ValidatorException
  {
    String psw = (String)object;
    if ((context == null) || (component == null))
      throw new NullPointerException();

    if (!(component instanceof UIInput)) {
      return;
    }

    if (!(ASHValidatorUtility.validMinLength(psw, 6)))
      throw new ValidatorException(new FacesMessage(ResourceHandler.getResource("invalid_password_too_short")));

    if (!(ASHValidatorUtility.validMaxLength(psw, 32))) {
      throw new ValidatorException(new FacesMessage(ResourceHandler.getResource("invalid_password_too_long")));
    }

    if ((component.getId() != null) && (component.getId().endsWith(ID_PASSWORD))) {
      context.getExternalContext().getRequestMap().put(ID_PASSWORD, object);
      if (!(((String)object).equals(fakePassword)))
      {
        valididateCharacters((String)object);
      }
      String userName = (String)context.getExternalContext().getRequestMap().get(ID_USERNAME);
      if ((null != userName) && (psw.equals(userName)))
        throw new ValidatorException(new FacesMessage(ResourceHandler.getResource("invalid_password_equals_username")));
    }

    if ((component.getId() != null) && (component.getId().endsWith(ID_PASSWORD_CONFIRM))) {
      String password = (String)context.getExternalContext().getRequestMap().get(ID_PASSWORD);
      if ((password == null) || (!(password.equals(psw))))
        throw new ValidatorException(new FacesMessage(ResourceHandler.getResource("password_confirm_failed")));
    }
  }
}