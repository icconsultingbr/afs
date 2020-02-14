package com.anoto.ash.web.validators;

import com.anoto.ash.utils.ResourceHandler;
import java.util.Iterator;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;

public class CommonValidation
{
  public static String ID_PASSWORD = "user_password";
  public static String ID_USERNAME = "user_name";

  public String getUserUsername(UIComponent component, String userPrefix)
  {
    String componentId = userPrefix + "_user_username";
    String componentValue = null;
    UIComponent c = getComponentChild(getFormRoot(component, "UserForm").getChildren().iterator(), componentId);
    if (null != c)
    {
      componentValue = (String)c.getAttributes().get("value");
    }
    return componentValue;
  }

  public int getUserId(UIComponent component, String userPrefix) {
    int userId = -1;

    Integer userSid = null;
    String componentId = userPrefix + "_user_id";
    UIComponent c = getComponentChild(getFormRoot(component, "UserForm").getChildren().iterator(), componentId);
    if (null != c)
    {
      userSid = (Integer)c.getAttributes().get("value");
      try {
        userId = userSid.intValue();
      } catch (NumberFormatException nfe) {
      }
    }
    return userId;
  }

  public String getUserPassword(UIComponent component, String userPrefix)
  {
    String componentId = userPrefix + "_user_password";
    String componentValue = null;
    UIComponent c = getComponentChild(getFormRoot(component, "UserForm").getChildren().iterator(), componentId);
    if (null != c)
    {
      componentValue = (String)c.getAttributes().get("value");
    }
    return componentValue;
  }

  private UIComponent getFormRoot(UIComponent component, String subFormId)
  {
	  UIComponent fuic = null;
      UIComponent tmpc = component;
      while(null == fuic) 
      {
          if(tmpc.getId().contains(subFormId))
          {
              fuic = tmpc;
          } else
          {
              tmpc = tmpc.getParent();
          }
      }
      return fuic;
  }

  private UIComponent getComponentChild(Iterator<UIComponent> iter, String id)
  {
    UIComponent uic = null;
    while ((iter.hasNext()) && (null == uic)) {
      UIComponent tmp = (UIComponent)iter.next();

      if ((null != tmp) && (tmp.getId().equals(id)))
      {
        uic = tmp;
        break;
      }

      uic = getComponentChild(tmp.getChildren().iterator(), id);
    }

    return uic;
  }

  protected void valididateCharacters(String matchingString) throws ValidatorException
  {
    if (!(Pattern.compile("[a-zA-Z0-9_.-]*").matcher(matchingString).matches()))
    {
      throw new ValidatorException(new FacesMessage(ResourceHandler.getResource("invalid_characters")));
    }
  }
}