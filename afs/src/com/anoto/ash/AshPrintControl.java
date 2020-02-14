package com.anoto.ash;

import com.anoto.afs.dynamicdata.DynamicDataDecorator;
import com.anoto.afs.dynamicdata.xml.Dynamicdata;
//import com.anoto.afs.dynamicdata.xml.Dynamicdata.Data;
//import com.anoto.afs.dynamicdata.xml.Dynamicdata.Data.Form;
import com.anoto.api.util.PadFileUtility;
import com.anoto.api.util.PageAddress;
import com.anoto.ash.database.FormCopyData;
//import com.anoto.ash.database.FormDBHandler;
import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.PadFile;
import com.anoto.ash.database.UserData;
import com.anoto.ash.exceptions.FormTypeNotPodException;
import com.anoto.ash.exceptions.NoAvailableFormCopiesException;
import com.anoto.ash.exceptions.NoPADAvailableException;
import com.anoto.ash.exceptions.NoSuchFormTypeException;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.portal.AshUserControl;
import com.anoto.ash.portal.result.DynamicDataValidationResult;
import com.anoto.ash.portal.result.FormCopyResult;
import com.anoto.ash.portal.result.LoginResult;
//import com.anoto.patterncore.pad.PadLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
//import java.sql.Blob;
import java.sql.SQLException;
//import java.util.Date;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AshPrintControl
{
  private static AshPrintControl instance = null;

  public static AshPrintControl getInstance()
  {
    if (instance == null) {
      instance = new AshPrintControl();
    }

    return new AshPrintControl();
  }

  public InputStream getDynData(InputStream xmlStream)
    throws SQLException, IOException, NoAvailableFormCopiesException, NoSuchFormTypeException, NoPADAvailableException, Exception
  {
    ByteArrayInputStream xmlByteStream = null;
    AshLogger.logFine("About to complete a received xml file");

    InputStream padStream = null;

    byte[] xmlBytes = new byte[xmlStream.available()];
    xmlStream.read(xmlBytes);
    xmlByteStream = new ByteArrayInputStream(xmlBytes);

    DynamicDataDecorator dynDataDecorator = new DynamicDataDecorator();
    dynDataDecorator.setInputStream(xmlByteStream);
    Dynamicdata dynData = dynDataDecorator.getDynamicData();

    String formTypeName = dynData.getFormtype();
    AshLogger.logFine("Form type name: " + formTypeName);
    int nbrOfFormCopies = dynData.getData().getForm().size();
    AshLogger.logFine("Number of forms: " + nbrOfFormCopies);

    AshFormControl formControl = AshFormControl.getInstance();

    FormTypeData formType = new FormTypeData();
    formType.setFormTypeName(formTypeName);

    List formTypes = formControl.getFormType(formType);

    if (formTypes.size() > 0) {
      formType = (FormTypeData)formControl.getFormType(formType).get(0);
    } else {
      throw new NoSuchFormTypeException();
    }

    
    if (formType.getPod() != 1) {
      throw new FormTypeNotPodException();
    }

    PadFile padFile = new PadFile();
    padFile.setFormType(formType);

    List padFiles = formControl.getPadFile(padFile);

    if ((padFiles != null) && (padFiles.size() > 0)) {
      PadFile pad = (PadFile) padFiles.get(0);
      padStream = new ByteArrayInputStream(pad.getPadFile());
    }

    DynamicDataValidationResult result = DynamicDataValidator.validate(padStream, dynData, false);

    if (!(result.isValid()))
    {
      AshLogger.logSevere("The xml file is not valid!");
      AshLogger.logSevere(result.getValidationMessage());
      throw new IllegalArgumentException(result.getValidationMessage());
    }

    xmlByteStream = new ByteArrayInputStream(xmlBytes);

    ByteArrayOutputStream out = (ByteArrayOutputStream) getDynamicData(xmlByteStream);
    xmlByteStream = new ByteArrayInputStream(out.toByteArray());

    return xmlByteStream;
  }
  
  public InputStream getPdf(String formTypeName) throws SQLException
  {
    InputStream pdfStream = null;
    AshLogger.logFine("About to get the pdf for form type " + formTypeName);
    AshFormControl formControl = AshFormControl.getInstance();

    FormTypeData formTypeData = new FormTypeData();
    formTypeData.setFormTypeName(formTypeName);
    formTypeData = (FormTypeData)formControl.getFormType(formTypeData).get(0);

    pdfStream = new ByteArrayInputStream(formTypeData.getPdfFile());

    return pdfStream;
  }

  public InputStream getPad(String formTypeName, String endPageAddress) throws SQLException
  {
    InputStream padStream = null;

    AshLogger.logFine("About to get the PAD that covers page:  " + endPageAddress + ".Form type =  " + formTypeName);

    String licenseAddress = PadFileUtility.getPadLicenseAddress(endPageAddress, "Anoto_Forms_Solution_ASH");

    AshLogger.logFine("Pad license address: " + licenseAddress);

    AshFormControl formControl = AshFormControl.getInstance();

    FormTypeData formType = new FormTypeData();
    formType.setFormTypeName(formTypeName);

    formType = (FormTypeData)formControl.getFormType(formType).get(0);
    PadFile padFile = new PadFile();
    padFile.setFormType(formType);
    padFile.setPadLicenseAddress(licenseAddress);

    List padFiles = formControl.getPadFile(padFile);

    if ((padFiles != null) && (padFiles.size() > 0)) {
      PadFile pad = (PadFile)padFiles.get(0);

      padStream = new ByteArrayInputStream(pad.getPadFile());

      formControl.updatePadFile(pad);
    }

    return padStream;
  }

  public String getFormTypeNames(boolean onlyFormTypesWithoutDynData) throws SQLException, IOException {
    AshLogger.logFine("About to get the names of all form types");
    StringBuffer formTypeNames = new StringBuffer();
    AshFormControl formControl = AshFormControl.getInstance();

    List formTypeList = formControl.getAllFormTypes();

    Iterator iter = formTypeList.iterator();
    FormTypeData formType = null;
    
    while(iter.hasNext()) {
      formType = (FormTypeData)iter.next();

      if (formType.getPod() != 1)
        break;

      if (!(onlyFormTypesWithoutDynData))
        break;

      PadFile padFile = new PadFile();
      padFile.setFormType(formType);
      List padFileList = AshFormControl.getInstance().getPadFile(padFile);

      if ((padFileList == null) || (padFileList.size() <= 0)) 
    	break;
      
      padFile = (PadFile)padFileList.get(0);

      List dynDataFieldsNames = DynamicDataValidator.getFieldNamesFromPad(new ByteArrayInputStream(padFile.getPadFile()), false);

      if ((dynDataFieldsNames == null) || (dynDataFieldsNames.size() <= 0)) {
        break;
      } 

      String formTypeName = formType.getFormTypeName();
      formTypeNames.append(formTypeName);
      formTypeNames.append(" ");
    }
    
    return formTypeNames.toString();
  }

  public String authorizeUser(String userName, String pwd) {
    AshLogger.logFine("About to authorize user with userName= " + userName);
    AshUserControl userControl = AshUserControl.getInstance();
    String resultString = "";

    UserData user = new UserData();
    user.setUserName(userName);
    user.setPassword(pwd);

    LoginResult result = userControl.authorizeUser(user, false);

    if (result.isLoginOperationSuccessful())
    {
      resultString = "authorized";
    }
    else { resultString = result.getLoginOperationMessage();
    }

    return resultString;
  }

  private static synchronized OutputStream getDynamicData(InputStream xmlStream)
    throws NoAvailableFormCopiesException, NoSuchFormTypeException, NoPADAvailableException, SQLException, Exception
  {
    DynamicDataDecorator dynamicData = new DynamicDataDecorator();
    
    byte[] xmlBytes = new byte[xmlStream.available()];
    
    xmlStream.read(xmlBytes);
    ByteArrayInputStream xmlByteStream = new ByteArrayInputStream(xmlBytes);
    dynamicData.setInputStream(xmlByteStream);

    FormTypeData formType = new FormTypeData();
    formType.setFormTypeName(dynamicData.getDynamicData().getFormtype());

    List formtypes = AshFormControl.getInstance().getFormType(formType);

    if (formtypes.size() == 0) {
    	throw new NoSuchFormTypeException();
    }

    formType = (FormTypeData) formtypes.get(0);

    int numberOfFormCopies = dynamicData.getDynamicData().getData().getForm().size();
    
    AshFormControl formControl = AshFormControl.getInstance();
    
    // Recupera o número de atendimento do XML.
	HashMap currentDynamicDataFields = AshCommons.getDynamicDataFieldsByCopyNumber(0, dynamicData);
	String numeroOcorrencia = (String) currentDynamicDataFields.get(AshProperties.getAfsProperty(AshProperties.NUMERO_OCORRENCIA));
	
	if (StringUtils.isEmpty(numeroOcorrencia)) {
		throw new NoSuchFieldException(AshProperties.NUMERO_OCORRENCIA);
	}
	
	FormCopyData formCopy = new FormCopyData();
	formCopy.setNumeroOcorrencia(numeroOcorrencia);
	
	// Verifica se a impressão do atendimento foi gerada. 
	List formList = AshFormControl.getInstance().searchFormCopies(formCopy);
	boolean isFichaImpressa = ((formList != null && formList.size() > 0)?true:false);
	
	String actualPageAddress = null;
	String licenseAddress = null;
	
	// Se a ficha já foi impressa recupera o endereço gravado.
    if (isFichaImpressa) {
    	FormCopyData formCopyTmp = (FormCopyData) formList.get(0);
    	formType = formCopyTmp.getFormType();
    	actualPageAddress = formCopyTmp.getEndPageAddress();
    	
    } else {
        if (numberOfFormCopies > formType.getRemainingFormCopies()) {
        	throw new NoAvailableFormCopiesException();
        }
        
        actualPageAddress = formControl.getMaxPageAddress(formType.getFormTypeName());
    }
    
    if (actualPageAddress != null) {
    	PageAddress address = new PageAddress(actualPageAddress);
        int segment = address.getSegment();
        int shelf = address.getShelf();
        int book = address.getBook();
        int page = address.getPage();
        
        actualPageAddress = "0" + "." + segment + "." + shelf + "." + book + "." + page;
        
        if ((book == 23 && page == 255) && !isFichaImpressa) {
        	shelf = shelf + 1;
        	
        	while (formControl.getCountPageAddress("0" + "." + segment + "." + shelf + ".%") == 6144) {
        		shelf = shelf + 1;
        	}
        }
        
        licenseAddress = "0" + "." + segment + "." + shelf + ".*.*";
    }

    PadFile pad = new PadFile();
    pad.setFormType(formType);
    pad.setPadLicenseAddress(licenseAddress);
    List pads = formControl.getPadFile(pad);

    HashMap padMap = new HashMap();
    if (pads.size() == 0) {
    	System.out.println("###PadLicenseAddress: '" + pad.getPadLicenseAddress() + "' não econtrado!");
    	throw new NoPADAvailableException();
    }
    
    PadFile currentPadTmp = (PadFile) pads.get(0);
    padMap.put(currentPadTmp.getPadLicenseAddress(), currentPadTmp);
    List possibleAddressRanges = AshCommons.calculatePossibleAddressRanges(currentPadTmp);
    
    int index = 0;
    if (actualPageAddress != null) {
	    for (int i = 0; i < possibleAddressRanges.size(); i++) {
	    	String addressRange = (String) possibleAddressRanges.get(i);
	    	if (addressRange.contains("-")) {
	    		addressRange = addressRange.substring(0, addressRange.lastIndexOf(".") + 1) + addressRange.substring(addressRange.lastIndexOf("-") + 1, addressRange.length());
	    	}	    	
	    	if (addressRange.equals(actualPageAddress)) {
	    		index = i;
	    		if (!isFichaImpressa) {
	    	    	index += 1;
	    	    }
	    	}
		}
    }
    
    List pageAddressRangesToAdd = new LinkedList();
    
    int i = 0;
    for (Iterator iter = possibleAddressRanges.listIterator(index); iter.hasNext();) {
    	String currentAddressRange = (String) iter.next();
      
    	if (i >= numberOfFormCopies) {
    		break;
    	}

	    String endPageAddress = AshCommons.getEndPageAddressFromRange(currentAddressRange);
	    String padLicenseAddress = PadFileUtility.getPadLicenseAddress(endPageAddress, "Anoto_Forms_Solution_ASH");
	    PadFile currentPad = (PadFile) padMap.get(padLicenseAddress);
	    Long padUpdated = new Long(currentPad.getLastModified().getTime());
	
	    ((Dynamicdata.Data.Form) (dynamicData.getDynamicData().getData().getForm()).get(i)).setPadUpdated(padUpdated);
	    ((Dynamicdata.Data.Form) (dynamicData.getDynamicData().getData().getForm()).get(i)).setPageAddressRange(currentAddressRange);
	
	    pageAddressRangesToAdd.add(currentAddressRange);
	       
	    ++i;
    }
    
    FormCopyResult addResult = new FormCopyResult();
    
    // Grava somente os atendimentos que ainda não foram impressos.
    if (isFichaImpressa) {
    	addResult.setFormCopyOperationSuccessful(true);
    } else {
    	addResult = AshCommons.addFormCopiesWithDynData(pageAddressRangesToAdd, dynamicData, formType);
    }
    
    if (!(addResult.isFormCopyOperationSuccessful())) {
    	AshLogger.logSevere(addResult.getFormCopyOperationMessage());
    	throw new IllegalStateException(addResult.getFormCopyOperationMessage());
    }
    
    AshLogger.logFine("Ash got a request for dynamic data for the form type: " + dynamicData.getDynamicData().getFormtype());

    return dynamicData.getOutputStream();
  }
   
  // Wagner - Inclui
  public BigInteger getNextValSeq(String nmSequence) {
	  BigInteger seq = AshFormControl.getInstance().getNextValSeq(nmSequence);
	  AshLogger.logFine("Sequence value: " + seq);
	  return seq;
  }
  
}