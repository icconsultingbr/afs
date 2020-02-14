package com.anoto.ash.portal;

import com.anoto.api.util.PadFileUtility;
import com.anoto.api.util.PageAddress;
import com.anoto.ash.AshCommons;
import com.anoto.ash.AshLogger;
import com.anoto.ash.Sorter;
import com.anoto.ash.database.BackgroundFile;
import com.anoto.ash.database.FormDBHandler;
import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.PadFile;
import com.anoto.ash.portal.result.FormCopyResult;
import com.anoto.ash.portal.result.FormTypeResult;
import com.anoto.ash.portal.utils.ZipFileHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.hibernate.HibernateException;


@SuppressWarnings({"unchecked", "rawtypes"})
public class AshUploadControl
{
  private static AshUploadControl instance = null;

  public static AshUploadControl getInstance()
  {
    if (instance == null) {
      instance = new AshUploadControl();
    }

    return new AshUploadControl();
  }

  public FormTypeResult setFormTypeAttributes(FormTypeData formTypeData)
  {
    AshLogger.logFine("Setting the Attributes for the Form Type: " + formTypeData.toString());

    FormTypeResult result = new FormTypeResult();

    try
    {
      int operationResult = FormDBHandler.updateFormType(formTypeData);

      if (operationResult == -11) {
        AshLogger.logFine("No such form type: " + formTypeData.getFormTypeName());
        result.setFormTypeOperationSuccessful(false);
        result.setFormTypeOperationMessage("No form type exists with the proposed form type name.");
      } else {
        result.setFormTypeOperationSuccessful(true);
      }
    }
    catch (HibernateException he) {
      AshLogger.logSevere("Error when setting the attributes for the form type, reason: " + he.getMessage());
      result.setFormTypeOperationSuccessful(false);
      result.setFormTypeOperationMessage("Severe Error. Please contact system administrator.");
    }

    result.setFormTypeData(formTypeData);
    return result;
  }

  public FormTypeResult addFormType(byte[] zipFileBytes, String formTypeName)
  {
    ZipFileHandler zipHandler = new ZipFileHandler(zipFileBytes);

    AshLogger.logFine("Adding a new form type: " + formTypeName);

    FormTypeResult result = new FormTypeResult();
    try {
      result = validate(zipHandler);
    } catch (Exception e) {
      result.setFormTypeOperationSuccessful(false);
      result.setFormTypeOperationMessage("The zip file contains corrupt files and can't be used.");
      AshLogger.logSevere("The zip file contains corrupt files and can't be used, reason: " + e.getMessage());
      return result;
    }

    if (result.isZipFileValid())
    {
      try
      {
        int nbrOfLicensePages = zipHandler.getNbrOfPadLicensePages();
        int nbrOfFormPages = zipHandler.getNbrOfPadFormPages();
        int maxNbrOfFormCopies = getMaxNumberOfFormCopies(nbrOfLicensePages, nbrOfFormPages);
        boolean xmlFileIncluded = zipHandler.getNumberOfXmlFiles() == 1;

        FormTypeData formTypeData = new FormTypeData();
        formTypeData.setCreationDate(new Date(System.currentTimeMillis()));
        formTypeData.setFormTypeName(formTypeName);

        formTypeData.setPdfFile(zipHandler.getPdfFileAsBytes());

        formTypeData.setMaxNbrOfFormCopies(maxNbrOfFormCopies);
        formTypeData.setRemainingFormCopies(maxNbrOfFormCopies);

        formTypeData.setNotificationLevelPercent(10);

        if (xmlFileIncluded)
        {
          formTypeData.setPod(0);
        }
        else
        {
          formTypeData.setPod(1);
        }

        PadFile padFile = new PadFile();
        byte[] padbyte = zipHandler.getPadFileAsBytes();
        padFile.setPadFile(padbyte);

        String padLicenseAddress = zipHandler.getPadLicenseAddress();
        AshLogger.logFine("Formtype PAD license address: " + padLicenseAddress);
        padFile.setPadLicenseAddress(padLicenseAddress);

        padFile.setFormType(formTypeData);

        padFile.setLastModified(new Date(System.currentTimeMillis()));

        AshFormControl formControl = AshFormControl.getInstance();

        int operationResult = formControl.addPadFile(padFile);

        if (operationResult < 0)
        {
          result.setFormTypeOperationSuccessful(false);
          return result;
        }

        BackgroundFile backgroundFile = null;

        for (int i = 0; i < zipHandler.getNumberOfPngFiles(); ++i)
        {
          backgroundFile = new BackgroundFile();
          backgroundFile.setBackgroundFile(zipHandler.getBackgroundFileAsBytes(i));
          backgroundFile.setFileName(zipHandler.getBackgroundFileName(i));
          AshLogger.logFine("Formtype background: " + zipHandler.getBackgroundFileName(i));
          backgroundFile.setFormType(formTypeData);

          operationResult = formControl.addBackgroundFile(backgroundFile);

          if (operationResult < 0)
          {
            formControl.deleteFormType(formTypeData);
            result.setFormTypeOperationSuccessful(false);
            return result;
          }

        }

        if (xmlFileIncluded) {
          AshLogger.logFine("The upload contains an xml file");

          InputStream xmlStream = zipHandler.getXmlFileAsStream();

          List pageAddressRanges = AshCommons.calculatePossibleAddressRanges(padFile);

          FormCopyResult addResult = AshCommons.addFormCopiesWithDynData(pageAddressRanges, xmlStream, formTypeData);

          if (!(addResult.isFormCopyOperationSuccessful()))
          {
            AshLogger.logSevere(addResult.getFormCopyOperationMessage());
            formControl.deleteFormType(formTypeData);
            result.setFormTypeOperationSuccessful(false);
            result.setFormTypeOperationMessage(addResult.getFormCopyOperationMessage());
            return result;
          }
        }
        AshCommons.loadPadFromByteArray(padbyte);
        AshLogger.logFine("Loading PAD with license address: " + padLicenseAddress);
        result.setFormTypeOperationSuccessful(true);

        AshFormControl.getInstance().generateExampleXml(formTypeData);

        result.setFormTypeData(formTypeData);
      }
      catch (Exception e) {
        AshLogger.logSevere("Error when adding form type, reason: " + e.getMessage());
        result.setFormTypeOperationSuccessful(false);
        result.setFormTypeOperationMessage("Severe Error. Please contact system administrator.");
      }

    }

    return result;
  }

  public FormTypeResult addPadFile(String padFileName, byte[] padFileBytes, byte[] xmlFileBytes, FormTypeData formTypeData)
  {
    ByteArrayInputStream padStream = new ByteArrayInputStream(padFileBytes);
    FormTypeResult result = new FormTypeResult();

    AshLogger.logFine("Adding PAD file: " + padFileName);

    result = validateAddedPadFile(padFileBytes, formTypeData);

    if (result.isPADFileValid())
    {
      try
      {
        padStream = new ByteArrayInputStream(padFileBytes);

        padStream = new ByteArrayInputStream(padFileBytes);
        int nbrOfPadLicensePages = PadFileUtility.getNumberOfLicensePages(padStream);
        AshLogger.logFine("Number of license pages for this PAD: " + nbrOfPadLicensePages);

        padStream = new ByteArrayInputStream(padFileBytes);
        int nbrOfPadFormPages = PadFileUtility.getPageAddresses(padStream).length;
        int nbrOfFormCopiesAdded = nbrOfPadLicensePages / nbrOfPadFormPages;
        AshLogger.logFine("Number of form copies for this PAD: " + nbrOfFormCopiesAdded);

        int oldFormCopiesCapacity = formTypeData.getMaxNbrOfFormCopies();
        int newFormCopiesCapacity = oldFormCopiesCapacity + nbrOfFormCopiesAdded;
        AshLogger.logFine("Old form copies capacity: " + oldFormCopiesCapacity);
        AshLogger.logFine("The new form copies capacity: " + newFormCopiesCapacity);

        formTypeData.setMaxNbrOfFormCopies(newFormCopiesCapacity);

        int oldRemaingFormCopies = formTypeData.getRemainingFormCopies();
        int newRemaingFormCopies = oldRemaingFormCopies + nbrOfFormCopiesAdded;
        AshLogger.logFine("Number of remaining form copies before update: " + oldRemaingFormCopies);
        AshLogger.logFine("Number of remaining form copies after update: " + newRemaingFormCopies);
        formTypeData.setRemainingFormCopies(newRemaingFormCopies);

        PadFile padFile = new PadFile();
        padStream = new ByteArrayInputStream(padFileBytes);
        String padLicenseAddress = PadFileUtility.getLicenseAddressFromPad(padStream);
        padFile.setPadLicenseAddress(padLicenseAddress);

        padFile.setPadFile(padFileBytes);

        padFile.setFormType(formTypeData);
        padFile.setLastModified(new Date(System.currentTimeMillis()));

        AshFormControl formControl = AshFormControl.getInstance();
        int operationResult = formControl.addPadFile(padFile);

        if (operationResult < 0)
        {
          result.setFormTypeOperationSuccessful(false);
          result.setFormTypeOperationMessage("Operation failed.");
          return result;
        }

        if (xmlFileBytes != null) {
          AshLogger.logFine("The upload contains an xml file");

          InputStream xmlStream = new ByteArrayInputStream(xmlFileBytes);

          List pageAddressRanges = AshCommons.calculatePossibleAddressRanges(padFile);

          FormCopyResult addResult = AshCommons.addFormCopiesWithDynData(pageAddressRanges, xmlStream, formTypeData);

          if (!(addResult.isFormCopyOperationSuccessful()))
          {
            AshLogger.logSevere(addResult.getFormCopyOperationMessage());
            formControl.deleteFormType(formTypeData);
            result.setFormTypeOperationSuccessful(false);
            return result;
          }

        }

        AshLogger.logFine("Loading PAD " + padFileName);
        AshCommons.loadPadFromByteArray(padFileBytes);

        result.setFormTypeData(formTypeData);
        result.setFormTypeOperationSuccessful(true);
      }
      catch (Exception e)
      {
        AshLogger.logSevere("Error when adding PAD, reason: " + e.getMessage());
        result.setFormTypeOperationSuccessful(false);
        result.setFormTypeOperationMessage("Severe Error. Please contact system administrator.");
      }

    }
    else
    {
      AshLogger.logFine("Not a valid PAD: " + padFileName);
    }

    return result;
  }

  public FormTypeResult replacePadFile(String padFileName, byte[] padFileBytes, FormTypeData formTypeData) {
    FormTypeResult result = new FormTypeResult();
    AshLogger.logFine("About to replace PAD file: " + padFileName);

    result = validateReplacedPadFile(padFileBytes, formTypeData);

    if (result.isPADFileValid())
    {
      try
      {
        PadFile padFile = new PadFile();
        padFile.setFormType(formTypeData);
        padFile = (PadFile)FormDBHandler.searchPadFile(padFile).get(0);

        padFile.setPadFile(padFileBytes);
        padFile.setLastModified(new Date(System.currentTimeMillis()));

        AshFormControl formControl = AshFormControl.getInstance();
        int operationResult = formControl.updatePadFile(padFile);

        if (operationResult < 0)
        {
          result.setFormTypeOperationSuccessful(false);
          result.setFormTypeOperationMessage("Operation failed.");
          return result;
        }

        AshCommons.loadPadFromByteArray(padFileBytes);

        result.setFormTypeOperationSuccessful(true);

        result.setFormTypeData(formTypeData);
      }
      catch (Exception e)
      {
        AshLogger.logSevere("Error when trying to write PAD File to disk, reason: " + e.getMessage());
        throw new IllegalStateException("Severe Error. Please Contact System Adminisitrator.");
      }
    }
    AshLogger.logFine("Not a valid PAD: " + padFileName);

    return result;
  }

  public boolean formTypeNameValid(String formTypeName) throws IllegalStateException
  {
    boolean formTypeNameValid = false;
    try
    {
      formTypeNameValid = !(FormDBHandler.formTypeNamePreviouslyUsed(formTypeName));
    } catch (HibernateException he) {
      AshLogger.logSevere("Error when validating form tyep, reason: " + he.getMessage());
      throw new IllegalStateException("Severe Error. Please contact system administrator.");
    }

    return formTypeNameValid;
  }

  private int getMaxNumberOfFormCopies(int nbrOfLicensePages, int nbrOfFormPages)
  {
    int numberOfForms = nbrOfLicensePages / nbrOfFormPages;

    if (nbrOfLicensePages > 256)
    {
      int numberOfFormsPerBook = 256 / nbrOfFormPages;

      numberOfForms = 24 * numberOfFormsPerBook;
    }

    return numberOfForms;
  }

  private FormTypeResult validate(ZipFileHandler zipHandler)
    throws IllegalStateException
  {
    FormTypeResult uploadResult = new FormTypeResult();
    try
    {
      uploadResult.setZipFileValid(zipHandler.zipFileIsValid());
    } catch (IOException ioe) {
      AshLogger.logSevere("Error when validating zip file with form type, reson: " + ioe.getMessage());
      throw new IllegalStateException("Severe Error. Please contact System Administrator.");
    }

    uploadResult.setZipFileValidationMessage(zipHandler.getValidationMessage());

    return uploadResult;
  }

  public FormTypeResult validateAddedPadFile(byte[] padFileBytes, FormTypeData formTypeData) {
    ByteArrayInputStream padStream = new ByteArrayInputStream(padFileBytes);

    FormTypeResult result = new FormTypeResult();

    String padLicenseAddress = "";
    String firstPageAddress = "";
    try
    {
      padLicenseAddress = PadFileUtility.getLicenseAddressFromPad(padStream);

      firstPageAddress = padLicenseAddress.replaceAll("[*]", "0");
    }
    catch (IllegalArgumentException iae) {
      AshLogger.logSevere("Error when validating PAD File, reason: " + iae.getMessage());
      result.setPADFileValid(false);
      result.setPADFileValidationMessage("The specified file is not a valid PAD file");

      return result;
    }

    if (FormDBHandler.padPreviouslySubmitted(padLicenseAddress))
    {
      AshLogger.logFine("This PAD has already been submitted: " + padLicenseAddress);
      result.setPADFileValid(false);
      result.setPADFileValidationMessage("The PAD file has already been submitted to the ASH.");
    }
    else if (!(AshCommons.validPatternSegment(new PageAddress(firstPageAddress).getSegment()))) {
      AshLogger.logSevere("Error: The uploaded PAD file covers a pattern area not allowed in AFS");
      result.setPADFileValid(false);
      result.setPADFileValidationMessage("The PAD file covers a pattern area not allowed in AFS");
    }
    else {
      String padDiscrepancies = getPadDiscrepancies(padFileBytes, padLicenseAddress, formTypeData);

      if (padDiscrepancies.length() > 0) {
        AshLogger.logSevere("Error: " + padDiscrepancies);
        result.setPADFileValid(false);
        result.setPADFileValidationMessage(padDiscrepancies);
      }
      else {
        result.setPADFileValid(true);
      }

    }

    return result;
  }

  public FormTypeResult validateReplacedPadFile(byte[] padFileBytes, FormTypeData formTypeData) {
    ByteArrayInputStream padStream = new ByteArrayInputStream(padFileBytes);

    FormTypeResult result = new FormTypeResult();

    String padLicenseAddress = "";
    String firstPageAddress = "";
    try
    {
      padLicenseAddress = PadFileUtility.getLicenseAddressFromPad(padStream);

      firstPageAddress = padLicenseAddress.replaceAll("[*]", "0");
    }
    catch (IllegalArgumentException iae) {
      AshLogger.logSevere("Error when validating PAD File, reason: " + iae.getMessage());
      result.setPADFileValid(false);
      result.setPADFileValidationMessage("The specified file is not a valid PAD file");

      return result;
    }

    if (!(FormDBHandler.padPreviouslySubmitted(padLicenseAddress)))
    {
      AshLogger.logFine("This PAD has never been submitted: " + padLicenseAddress);
      result.setPADFileValid(false);
      result.setPADFileValidationMessage("This PAD has never been submitted");
    }
    else if (!(AshCommons.validPatternSegment(new PageAddress(firstPageAddress).getSegment()))) {
      AshLogger.logSevere("Error: The uploaded PAD file covers a pattern area not allowed in AFS");
      result.setPADFileValid(false);
      result.setPADFileValidationMessage("The PAD file covers a pattern area not allowed in AFS");
    }
    else {
      String padDiscrepancies = getPadDiscrepancies(padFileBytes, padLicenseAddress, formTypeData);

      if (padDiscrepancies.length() > 0) {
        AshLogger.logSevere("Error: " + padDiscrepancies);
        result.setPADFileValid(false);
        result.setPADFileValidationMessage(padDiscrepancies);
      }
      else {
        result.setPADFileValid(true);
      }

    }

    return result;
  }

  private String getPadDiscrepancies(byte[] padFileBytes, String newPadLicenseAddress, FormTypeData formTypeData) {
    String discrepancies = "";
    ByteArrayInputStream newPadStream = new ByteArrayInputStream(padFileBytes);

    PadFile existingPadFile = new PadFile();
    existingPadFile.setFormType(formTypeData);
    existingPadFile = (PadFile)FormDBHandler.searchPadFile(existingPadFile).get(0);

    String firstPageAddress = existingPadFile.getPadLicenseAddress().replaceAll("[*]", "0");

    String[] pagesFirstFormCopy = PadFileUtility.getPageAddresses(firstPageAddress, "Anoto_Forms_Solution_ASH");
    Sorter.sortByPageAddressAsc(pagesFirstFormCopy);

    int nbrOfPagesPerFormExisting = pagesFirstFormCopy.length;
    int nbrOfPagesPerFormNew = PadFileUtility.getPageAddresses(newPadStream).length;

    newPadStream = new ByteArrayInputStream(padFileBytes);
    String[] backgroundImagesNew = PadFileUtility.getBackgroundImageNamesFromPad(newPadStream);
    String[] backgroundImagesExisting = new String[nbrOfPagesPerFormExisting];

    if (nbrOfPagesPerFormExisting != nbrOfPagesPerFormNew) {
      discrepancies = "This PAD file does not belong to formtype " + formTypeData.getFormTypeName() + "." + "" + "The number of pages per form don't match those of the previously uploaded PAD";

      return discrepancies;
    }

    for (int i = 0; i < nbrOfPagesPerFormExisting; ++i) {
      String currentPageAddress = pagesFirstFormCopy[i];

      String currentBgImageName = PadFileUtility.getBackgroundImageForPage(currentPageAddress, "Anoto_Forms_Solution_ASH");
      backgroundImagesExisting[i] = currentBgImageName;
    }

    if (!(Arrays.equals(backgroundImagesExisting, backgroundImagesNew))) {
      discrepancies = "This PAD file does not belong to formtype " + formTypeData.getFormTypeName() + "." + "" + "The background images don't match those of the previously uploaded PAD";

      return discrepancies;
    }

    return discrepancies;
  }
}