package com.anoto.wppgm;

import java.util.ArrayList;
import java.util.List;

import com.anoto.afs.dynamicdata.xml.Dynamicdata;


public class XmlHandler
{
  private Dynamicdata dynData;

  public XmlHandler(Dynamicdata dynData)
  {
    this.dynData = dynData;
  }


  public String[] getEndPageAddresses() {
    WppgmLogger.logFine("About to get all end page addresses");
    ArrayList<String> addressList = new ArrayList<String>();
    Dynamicdata.Data data = getDynData().getData();

    List<Dynamicdata.Data.Form> forms = data.getForm();

    for (Dynamicdata.Data.Form form : forms) {
      String endPageAddress = WppgmCommons.getEndPageAddressFromRange(form.getPageAddressRange());
      addressList.add(endPageAddress);
    }

    String[] endPageAddresses = (String[])(String[])addressList.toArray(new String[0]);

    WppgmCommons.sortByPageAddressAsc(endPageAddresses);

    return endPageAddresses;
  }

  public boolean isPadUpdated(String endPageAddress)
  {
    boolean padUpdated = false;

    if (WppgmCommons.findPadPath(endPageAddress).length() > 0)
    {
      //String padLicenseAddress = WppgmCommons.findPadLicenseAddress(endPageAddress);
      String padLicenseAddress = findPadLicenseAddress(endPageAddress);

      WppgmLogger.logFine("About to decide if PAD with address " + padLicenseAddress + " has been updated on the ASH");

      Long padUpdateTimeStampFromAsh = getPadUpdateTimeStamp(endPageAddress);

      Long storedPadUpdateTimeStamp = WppgmCommons.getPadTimeStamp(padLicenseAddress);

      if (padUpdateTimeStampFromAsh.longValue() > storedPadUpdateTimeStamp.longValue())
      {
        padUpdated = true;
        WppgmLogger.logFine("Pad " + padLicenseAddress + " has been updated on the ASH. ");
      }
      else {
        padUpdated = false;
        WppgmLogger.logFine("Pad " + padLicenseAddress + " has not been updated on the ASH. ");
      }
    }
    return padUpdated;
  }
  
  public static String findPadLicenseAddress(String pageAddress)
  {
    WppgmLogger.logFine("About to find the path to the PAD file which covers page " + pageAddress + "(if such a file exists)");
    String padLicenseAddress = "";

    String paNoPage = pageAddress.substring(0, pageAddress.lastIndexOf(".")).trim();
    String paNoPageNoBook = paNoPage.substring(0, paNoPage.lastIndexOf(".")).trim();
    //String bookLicenseAddress = paNoPage + ".*";
    String shelfLicenseAddress = paNoPageNoBook + ".*.*";

    //if (WppgmCommons.folderContainsFile(WppgmProperties.TEMPFOLDER, bookLicenseAddress))
    //{
    //  padLicenseAddress = bookLicenseAddress;
    //  WppgmLogger.logFine("Pad License Address: " + padLicenseAddress);
    //}
    //else if (WppgmCommons.folderContainsFile(WppgmProperties.TEMPFOLDER, shelfLicenseAddress))
    //{
      padLicenseAddress = shelfLicenseAddress;
      WppgmLogger.logFine("Pad License Address: " + padLicenseAddress);
    //}

    return padLicenseAddress;
  }

  public Long getPadUpdateTimeStamp(String endPageAddress)
  {
    String padLicenseAddress = WppgmCommons.findPadLicenseAddress(endPageAddress);

    WppgmLogger.logFine("About to get update timestamp for " + padLicenseAddress + " from xml");
    Dynamicdata.Data data = getDynData().getData();
    Long padUpdateTimeStampFromAsh = new Long(0L);

    List<Dynamicdata.Data.Form> forms = data.getForm();

    for (Dynamicdata.Data.Form form : forms) {
      String currentEndPageAddress = WppgmCommons.getEndPageAddressFromRange(form.getPageAddressRange());
      if (currentEndPageAddress.equalsIgnoreCase(endPageAddress))
      {
        padUpdateTimeStampFromAsh = form.getPadUpdated();

        break;
      }
    }

    return padUpdateTimeStampFromAsh;
  }

  public String getFormType() {
    WppgmLogger.logFine("About to get form type name");
    return this.dynData.getFormtype();
  }

  public Dynamicdata getDynData() {
    return this.dynData;
  }

  public List<Dynamicdata.Data.Form> getForms() {
    WppgmLogger.logFine("About to get all forms");
    Dynamicdata.Data data = getDynData().getData();
    List<Dynamicdata.Data.Form> forms = data.getForm();

    return forms;
  }

  public int getNumberOfForms() {
    WppgmLogger.logFine("About to get the number of forms");
    return getForms().size();
  }

  public Dynamicdata.Settings.Fields.Field.Text getTextSettingsByName(String fieldName) {
    WppgmLogger.logFine("About to get text settings for field: " + fieldName);
    Dynamicdata.Settings.Fields.Field.Text textSettings = null;
    Dynamicdata.Settings.Fields fields = getDynData().getSettings().getFields();

    if (fields != null)
    {
      List<Dynamicdata.Settings.Fields.Field> fieldList = fields.getField();

      for (Dynamicdata.Settings.Fields.Field currentField : fieldList) {
    	String currentFieldName = currentField.getName(); 
    	  
        if ((currentFieldName.trim()).equalsIgnoreCase(fieldName.trim()) && !currentField.isHidden()) {
        	textSettings = currentField.getText();
        	break;
        }
      }
    }

    return textSettings;
  }

  public String getTextValue(String fieldName, String pageAddress) {
    WppgmLogger.logFine("About to get the text value for field " + fieldName + " on page " + pageAddress);

    String textValue = "";
    List<Dynamicdata.Data.Form> formList = getForms();

    for (Dynamicdata.Data.Form currentForm : formList)
    {
      String paRange = currentForm.getPageAddressRange();

      if (WppgmCommons.pageInRange(pageAddress, paRange))
      {
        List<Dynamicdata.Data.Form.Field> fieldList = currentForm.getField();

        for (Dynamicdata.Data.Form.Field currentField : fieldList) {
          String currentFieldName = currentField.getName();
        
          if ((currentFieldName.trim()).equalsIgnoreCase(fieldName.trim())) {
            textValue = currentField.getValue();
          }
        }
      }
    }

    return textValue;
  }

  public String getChosenPrinterName() {
    WppgmLogger.logFine("About to get the name of the printer");

    String printerName = "n/a";
    try
    {
      printerName = this.dynData.getSettings().getPrinter().getName();
    }
    catch (Exception e)
    {
      WppgmLogger.logSevere("WPPGM got exception:  " + e.getMessage());
    }
    return printerName;
  }
}