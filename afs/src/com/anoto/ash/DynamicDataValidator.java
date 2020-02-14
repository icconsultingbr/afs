package com.anoto.ash;

import com.anoto.afs.dynamicdata.DynamicDataDecorator;
import com.anoto.afs.dynamicdata.xml.Dynamicdata;
import com.anoto.api.PenHome;
import com.anoto.api.util.PadFileUtility;
import com.anoto.ash.portal.result.DynamicDataValidationResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


@SuppressWarnings({"unchecked", "rawtypes"})
public class DynamicDataValidator
{
  private static List<String> fonts = new ArrayList();

  public static synchronized DynamicDataValidationResult validate(byte[] padBytes, InputStream xmlStream, boolean mustLoadPad)
    throws IOException
  {
    DynamicDataDecorator dynDataDecorator = new DynamicDataDecorator();
    dynDataDecorator.setInputStream(xmlStream);
    Dynamicdata dynData = dynDataDecorator.getDynamicData();

    ByteArrayInputStream padByteStream = new ByteArrayInputStream(padBytes);

    return validate(padByteStream, dynData, mustLoadPad);
  }

  public static synchronized DynamicDataValidationResult validate(InputStream padStream, Dynamicdata dynData, boolean mustLoadPad) throws IOException {
    AshLogger.logFine("About to validate dynamic data");
    List xmlFieldNames = getFieldNamesFromXml(dynData);
    List padFieldNames = getFieldNamesFromPad(padStream, mustLoadPad);
    DynamicDataValidationResult result = new DynamicDataValidationResult();
    String validationMessage = "";

    if (!(xmlFieldNames.equals(padFieldNames))) {
      AshLogger.logSevere("Validation failed!!");
      result.setValid(false);
      if (xmlFieldNames.size() > padFieldNames.size())
        validationMessage = "There are fields in the dynamic data definition (xml) not present in the formtype PAD file!";
      else if (padFieldNames.size() > xmlFieldNames.size())
        validationMessage = "There are dynamic data areas in the formtype PAD file not present in the dynamic data definition (xml)!";
      else {
        validationMessage = "The fields in the formtype PAD file and the fields in the dynamic data definition (xml) are not equal!";
      }

      AshLogger.logSevere(validationMessage);
      result.setValidationMessage(validationMessage);
    }
    else
    {
      String illegalFonts = getIllegalFonts();
      if ((illegalFonts != null) && (illegalFonts.length() > 0))
      {
        validationMessage = "Font(s) not supported by the AFS found in dynamic data xml: (" + illegalFonts + ")";

        AshLogger.logSevere(validationMessage);
        result.setValidationMessage(validationMessage);
      }
      else
      {
        result.setValid(true);
        AshLogger.logFine("Validation was successful!");
      }
    }

    return result;
  }

  private static List<String> getFieldNamesFromXml(Dynamicdata dynData) {
    AshLogger.logFine("Getting field names from the dynamic data definition (xml)");

    List fieldNames = new ArrayList();
    List fields = new ArrayList();

    if ((dynData != null) && (dynData.getSettings() != null) && (dynData.getSettings().getFields() != null) && (dynData.getSettings().getFields().getField() != null))
    {
      fields = dynData.getSettings().getFields().getField();
    }

    for (Iterator i$ = fields.iterator(); i$.hasNext(); ) { Dynamicdata.Settings.Fields.Field field = (Dynamicdata.Settings.Fields.Field)i$.next();
      String fieldName = field.getName();
      String font = field.getText().getFont();
      fieldNames.add(fieldName);
      fonts.add(font);
    }

    Collections.sort(fieldNames);

    return fieldNames;
  }

  public static List<String> getFieldNamesFromPad(InputStream padStream, boolean mustLoadPad)
    throws IOException
  {
    byte[] padBytes = new byte[padStream.available()];
    padStream.read(padBytes);
    ByteArrayInputStream padByteStream = new ByteArrayInputStream(padBytes);

    AshLogger.logFine("About to get the names of all user areas in the PAD taggeddynamic_data");
    List fieldNames = new ArrayList();
    String[] formPages = PadFileUtility.getPageAddresses(padByteStream);

    for (int i = 0; i < formPages.length; ++i) {
      String curentPageAddress = formPages[i];
      AshLogger.logFine("Handling page " + curentPageAddress);
      try
      {
        String[] pageUserAreaAppInfo = new String[0];

        if (mustLoadPad)
        {
          padByteStream = new ByteArrayInputStream(padBytes);
          PenHome.loadPad("only_used_here", padByteStream);

          pageUserAreaAppInfo = PadFileUtility.getPageUserAreaApplicationInfo(curentPageAddress, "only_used_here");

          PenHome.unloadPad("only_used_here");
        }
        else {
          pageUserAreaAppInfo = PadFileUtility.getPageUserAreaApplicationInfo(curentPageAddress, "Anoto_Forms_Solution_ASH");
        }

        for (int j = 0; j < pageUserAreaAppInfo.length; j += 3)
        {
          String userAreaName = pageUserAreaAppInfo[j];
          String userAreaAttributeName = pageUserAreaAppInfo[(j + 1)];

          if ((userAreaAttributeName.trim().equalsIgnoreCase("dynamic_data")) && 
            (!(fieldNames.contains(userAreaAttributeName)))) {
            fieldNames.add(userAreaName);
          }

        }

      }
      catch (Exception e)
      {
        throw new IllegalStateException("Something went wrong when validating dynamica data: " + e.getMessage());
      }

    }

    Collections.sort(fieldNames);
    return fieldNames;
  }

  private static String getIllegalFonts()  {
    String illegalFonts = "";
    for (Iterator iter = fonts.iterator(); iter.hasNext(); ) { 
    	String font = (String) iter.next();

    	if (!(Arrays.asList(AshProperties.supportedFonts).contains(font))) {
    		illegalFonts = illegalFonts.concat(font + " ");
    	}
    }
    
    return illegalFonts;
  }
}