package com.anoto.ash.utils;

import org.jboss.seam.core.SeamResourceBundle;

public class ResourceHandler
{
  public static String getResource(String name)
  {
    String result = name;
    try {
      result = SeamResourceBundle.getBundle().getString(name);
    }
    catch (Exception e)
    {
    }

    return result;
  }
}