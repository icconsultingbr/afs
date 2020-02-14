package com.anoto.ash;

import com.anoto.afs.dynamicdata.DynamicDataDecorator;
import com.anoto.afs.dynamicdata.xml.Dynamicdata;
import com.anoto.afs.dynamicdata.xml.parser.Parser;
import com.anoto.api.FormatException;
import com.anoto.api.IllegalValueException;
import com.anoto.api.NoSuchPageException;
import com.anoto.api.NoSuchPermissionException;
import com.anoto.api.Page;
import com.anoto.api.PageArea;
import com.anoto.api.PageException;
import com.anoto.api.Pen;
import com.anoto.api.PenHome;
import com.anoto.api.RenderException;
import com.anoto.api.renderer.DynamicText;
import com.anoto.api.renderer.JRenderer;
import com.anoto.api.renderer.RenderMachine;
import com.anoto.api.renderer.impl.DynamicTextImpl;
import com.anoto.api.util.PadFileUtility;
import com.anoto.api.util.PageAddress;
import com.anoto.ash.database.BackgroundFile;
import com.anoto.ash.database.DynamicDataDefinition;
import com.anoto.ash.database.DynamicDataEntry;
import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.MailSettingsDBHandler;
import com.anoto.ash.database.MailSettingsData;
import com.anoto.ash.database.PadFile;
import com.anoto.ash.database.Role;
import com.anoto.ash.database.UserDBHandler;
import com.anoto.ash.database.UserData;
import com.anoto.ash.exporter.VOFormProcessorToResultDocumentFactory;
import com.anoto.ash.exporter.document.ResultDocument;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.portal.AshUserControl;
import com.anoto.ash.portal.result.FormCopyResult;
import com.anoto.ash.portal.utils.ASHValidatorUtility;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.w3c.tools.crypt.Md5;
import org.xml.sax.SAXException;

@SuppressWarnings("rawtypes")
public class AshCommons
{

  public static JRenderer render(FormCopyData formCopyData, Page page, String backgroundImageIdentifier, InputStream bgStream, String outputFilePath, boolean useRenderMachine)
    throws RenderException
  {
    JRenderer renderer = null;
    try {
      AshLogger.logFine("About to render page: " + page.getPageAddress());
      
      //RenderMachine.setMaxNumberOfRunningRenderers(5);
      RenderMachine.setMaxNumberOfRunningRenderers(50);

      renderer = new JRenderer(page);
      String imageFormat = outputFilePath.substring(outputFilePath.lastIndexOf(".") + 1);
      AshLogger.logFine("Image output format: " + imageFormat);

      if (renderer.isSupported(imageFormat))
      {
        AshLogger.logFine("Configuring dynamic data");
        if ((formCopyData.getDynamicDataEntries() == null) || (formCopyData.getDynamicDataEntries().size() == 0))
        {
          List oldFormCopyList = AshFormControl.getInstance().searchFormCopies(formCopyData, "");

          if ((oldFormCopyList != null) && (oldFormCopyList.size() > 0)) {
            FormCopyData oldFormCopy = (FormCopyData)oldFormCopyList.get(0);
            if ((oldFormCopy.getDynamicDataEntries() != null) && (oldFormCopy.getDynamicDataEntries().size() > 0))
              formCopyData.setDynamicDataEntries(oldFormCopy.getDynamicDataEntries());
          }

        }

        renderer.setInterpolation((short) 1);
        renderer.setUseAntiAliasing(true);
        renderer.setUseTextAntiAliasing(true);

        renderer.setBackground(backgroundImageIdentifier, bgStream);

        // Não inclui os dados do atendimento em fichas de contingência.
        //if (!formCopyData.getNumeroOcorrencia().contains("PC")) {
        	addDynamicDataToRenderer(page, formCopyData, renderer);
        //}
        
        if (imageFormat.equalsIgnoreCase(AshProperties.IMAGE_FORMAT_PDF)) {
          outputFilePath = outputFilePath.replaceAll("pdf", "jpg");

          renderer.setRenderToStream(true);
        }

        renderer.setOutputFile(outputFilePath);

        if (useRenderMachine)
          RenderMachine.getInstance().addRender(renderer);
        else
          renderer.renderToFile(outputFilePath);
      }
      else {
        AshLogger.logSevere(imageFormat + " not supported!");
      }
    } catch (Exception e) {
      AshLogger.logSevere("Got exception: " + e.getMessage());
      throw new RenderException(e.getMessage(), e);
    }

    return renderer;
  }

  public static JRenderer render(FormCopyData formCopyData, String address, String backgroundImageIdentifier, InputStream bgStream, String outputFilePath, boolean useRenderMachine) throws RenderException
  {
    JRenderer renderer = null;
    try
    {
      AshLogger.logFine("About to render (unwritten) page: " + address);
      
      //RenderMachine.setMaxNumberOfRunningRenderers(5);
      RenderMachine.setMaxNumberOfRunningRenderers(50);

      int[] pageDimensions = PadFileUtility.getPageDimensions(formCopyData.getEndPageAddress(), "Anoto_Forms_Solution_ASH");

      renderer = new JRenderer(pageDimensions[0], pageDimensions[1]);

      String imageFormat = outputFilePath.substring(outputFilePath.lastIndexOf(".") + 1);
      AshLogger.logFine("Image output format: " + imageFormat);

      if (renderer.isSupported(outputFilePath.substring(outputFilePath.lastIndexOf(".") + 1)))
      {
    	/*
        if ((formCopyData.getDynamicDataEntries() == null) || (formCopyData.getDynamicDataEntries().size() == 0))
        {
          FormCopyData oldFormCopy = (FormCopyData)AshFormControl.getInstance().searchFormCopies(formCopyData, "").get(0);

          if ((oldFormCopy.getDynamicDataEntries() != null) && (oldFormCopy.getDynamicDataEntries().size() > 0))
            formCopyData.setDynamicDataEntries(oldFormCopy.getDynamicDataEntries());

        }
        */

        renderer.setInterpolation((short) 1);
        renderer.setUseAntiAliasing(true);
        renderer.setUseTextAntiAliasing(true);

        renderer.setBackground(backgroundImageIdentifier, bgStream);

        addDynamicDataToRenderer(formCopyData, address, renderer);

        if (imageFormat.equalsIgnoreCase(AshProperties.IMAGE_FORMAT_PDF)) {
          outputFilePath = outputFilePath.replaceAll("pdf", "jpg");

          renderer.setRenderToStream(true);
        }

        renderer.setOutputFile(outputFilePath);

        if (useRenderMachine)
          RenderMachine.getInstance().addRender(renderer);
        else
          renderer.renderToFile(outputFilePath);
      }
      else {
        AshLogger.logSevere(imageFormat + " not supported!");
      }
    } catch (Exception e) {
      AshLogger.logSevere("Got exception: " + e.getMessage());
      throw new RenderException(e.getMessage(), e);
    }
    return renderer;
  }

  static void sendDiscardedDataToAdmin(Pen pgc, String[] formPages, FormTypeData formTypeData)
    throws PageException, NoSuchPermissionException, RenderException, SQLException, FormatException, IllegalValueException, IOException, NoSuchFieldException, IllegalAccessException
  {
    Sorter.sortByPageAddressAsc(formPages);
    AshLogger.log("Form with end page address " + formPages[(formPages.length - 1)] + " is locked");
    AshLogger.logFine(" About to send discarded data to admin");

    FormCopyData formCopy = new FormCopyData();
    formCopy.setEndPageAddress(formPages[(formPages.length - 1)]);
    FormCopyData formCopyData = (FormCopyData) AshFormControl.getInstance().searchFormCopies(formCopy, "").get(0);

    String pageBgImageName = "";

    String mailFrom = AshProperties.defaultAdminMailSender;
    if (formTypeData.getExportMethod().getMailFrom().length() != 0) {
      mailFrom = formTypeData.getExportMethod().getMailFrom();
    }
    else
    {
      MailSettingsData mailSettings = MailSettingsDBHandler.getMailSettings();

      if ((mailFrom != null) && (mailFrom.length() > 0))
        mailFrom = mailSettings.getSmtpFrom();

    }

    String mailTo = getAdminEmailAddresses();

    AshLogger.logFine("Using " + mailFrom + " as mail sender");
    AshLogger.logFine("address(es) to send to: " + mailTo);

    //File folderPath = getDirectoryForFormCopy(formCopyData, "\\Program Files\\Anoto\\Anoto Forms Solution\\temp");
    File folderPath = getDirectoryForFormCopy(formCopyData, AshProperties.rsTempDirectory);
    RendererObserver renderObserver = new RendererObserver(Arrays.asList(formPages), folderPath, mailTo, mailFrom, null, formTypeData.getFormTypeName(), pgc.getPenData().getPenSerial(), "Discarded data in AFS. Form is locked", true);

    String[] array = formPages;
    int length = array.length; 
    for (int i = 0; i < length; ++i) { 
      JRenderer currentRenderer;
      String address = array[i];
      try {
        Page currentPage = pgc.getPage(address);
        pageBgImageName = PadFileUtility.getBackgroundImageForPage(currentPage.getPageAddress(), "Anoto_Forms_Solution_ASH");
        currentRenderer = new JRenderer(currentPage);

        addDynamicDataToRenderer(currentPage, formCopyData, currentRenderer);
      }
      catch (NoSuchPageException nspe)
      {
        int[] pageDimensions = PadFileUtility.getPageDimensions(formPages[(formPages.length - 1)], "Anoto_Forms_Solution_ASH");
        pageBgImageName = PadFileUtility.getBackgroundImageForPage(formPages[(formPages.length - 1)], "Anoto_Forms_Solution_ASH");
        currentRenderer = new JRenderer(pageDimensions[0], pageDimensions[1]);

        addDynamicDataToRenderer(formCopyData, address, currentRenderer);
      }

      currentRenderer.setInterpolation((short) 1);
      currentRenderer.setUseAntiAliasing(true);
      currentRenderer.setUseTextAntiAliasing(true);

      if (!(pageBgImageName.endsWith("png"))) {
        pageBgImageName = pageBgImageName + "png";
      }

      BackgroundFile pageBg = new BackgroundFile();
      pageBg.setFormType(formTypeData);
      pageBg.setFileName(pageBgImageName);

      pageBg = (BackgroundFile)AshFormControl.getInstance().getBackgroundFile(pageBg).get(0);

      currentRenderer.setBackground(pageBgImageName, new ByteArrayInputStream(pageBg.getBackgroundFile()));

      String imageFormat = "png";
      if (!(formTypeData.getExportMethod().getImageFormat().getName().equalsIgnoreCase(AshProperties.IMAGE_FORMAT_NONE))) {
        imageFormat = formTypeData.getExportMethod().getImageFormat().getName().toLowerCase();
      }

      if (!(folderPath.exists())) {
        FileUtils.forceMkdir(folderPath);
      }

      StringBuffer outputFilePath = new StringBuffer(folderPath.getPath()).append(File.separator);
      outputFilePath.append(pgc.getPenData().getPenSerial()).append("_");
      outputFilePath.append(address).append("_");
      outputFilePath.append(getDateString(Calendar.getInstance().getTime()));

      if (imageFormat.equalsIgnoreCase(AshProperties.IMAGE_FORMAT_PDF))
      {
        imageFormat = "jpg";

        currentRenderer.setRenderToStream(true);
      }
      outputFilePath.append("." + imageFormat);

      currentRenderer.setOutputFile(outputFilePath.toString());

      renderObserver.addRendererToObserve(currentRenderer);

      currentRenderer.renderToFile(outputFilePath.toString());
    }
  }

  @SuppressWarnings("unchecked")
  private static void addDynamicDataToRenderer(FormCopyData formCopyData, String pageAddress, JRenderer renderer)
    throws IOException, SQLException, NoSuchFieldException, IllegalAccessException
  {
    AshLogger.logFine("About to add dynamic data to renderer");
    Set definitionSet = formCopyData.getFormType().getDynamicDataDefinitions();
    HashMap definitionFields = new HashMap();
    if (definitionSet != null)
    {
      for (Iterator i$ = definitionSet.iterator(); i$.hasNext(); ) { 
    	DynamicDataDefinition definition = (DynamicDataDefinition)i$.next();

        if (definition.getOnlyShowInExport() != 1)
        {
          definitionFields.put(definition.getFieldName(), definition);
        }
      }
    }

    Set entrySet = formCopyData.getDynamicDataEntries();
    HashMap entryFields = new HashMap();
    if (entrySet != null)
    {
      for (Iterator i = entrySet.iterator(); i.hasNext(); ) { 
    	DynamicDataEntry entry = (DynamicDataEntry) i.next();

        entryFields.put(entry.getName(), entry);
      }
    }

    PadFile padFile = new PadFile();
    padFile.setFormType(formCopyData.getFormType());
    padFile = (PadFile)AshFormControl.getInstance().getPadFile(padFile).get(0);

    List dynamicDataFieldNames = DynamicDataValidator.getFieldNamesFromPad(new ByteArrayInputStream(padFile.getPadFile()), false);

    for (Iterator i = dynamicDataFieldNames.iterator(); i.hasNext(); ) { 
      String dynamicDataFieldName = (String) i.next();

      DynamicDataDefinition definition = (DynamicDataDefinition)definitionFields.get(dynamicDataFieldName);

      DynamicDataEntry entry = (DynamicDataEntry)entryFields.get(dynamicDataFieldName);

      if ((entry != null) && (definition != null))
      {
        int[] userAreaPosition = PadFileUtility.getUserAreaPosition(pageAddress, dynamicDataFieldName, "Anoto_Forms_Solution_ASH");

        if ((userAreaPosition[0] != -1) && (userAreaPosition[1] != -1))
        {
          DynamicText dynamicText = createDynamicText(userAreaPosition[1] - 31, userAreaPosition[0] - 31, definition, entry.getValue());

          renderer.addDynamicText(dynamicText);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void addDynamicDataToRenderer(Page page, FormCopyData formCopyData, JRenderer renderer)
    throws IllegalValueException, NoSuchFieldException, IllegalAccessException
  {
    AshLogger.logFine("About to add dynamic data to renderer");
    Set definitionSet = formCopyData.getFormType().getDynamicDataDefinitions();
    HashMap definitionFields = new HashMap();
    if (definitionSet != null)
    {
      for (Iterator i = definitionSet.iterator(); i.hasNext(); ) { 
    	DynamicDataDefinition definition = (DynamicDataDefinition) i.next();

        if (definition.getOnlyShowInExport() != 1)
        {
          definitionFields.put(definition.getFieldName(), definition);
        }
      }
    }

    Set entrySet = formCopyData.getDynamicDataEntries();
    HashMap entryFields = new HashMap();
    if (entrySet != null)
    {
      for (Iterator i = entrySet.iterator(); i.hasNext(); ) { 
    	DynamicDataEntry entry = (DynamicDataEntry) i.next();
    	
    	String value = entry.getValue();
    	if (value == null || value.equals("") || value.isEmpty()) {
    		entry.setValue(" ");
    	}
    	
        entryFields.put(entry.getName(), entry);
      }

    }

    Iterator pageAreaIterator = page.getPageAreas(256);

    while (pageAreaIterator.hasNext()) {
      PageArea userArea = (PageArea)pageAreaIterator.next();
      String dynDataTagValue = userArea.getAttribute("dynamic_data");

      if (dynDataTagValue != null)
      {
        DynamicDataDefinition definition = (DynamicDataDefinition)definitionFields.get(userArea.getName());

        DynamicDataEntry entry = (DynamicDataEntry) entryFields.get(userArea.getName());

        if ((entry != null) && (definition != null))
        {
          DynamicText dynamicText = createDynamicText(userArea.getBounds().getX(), userArea.getBounds().getY() + 1F, definition, entry.getValue());
          renderer.addDynamicText(dynamicText);
        }
      }
    }
  }

  private static DynamicText createDynamicText(float xCoordinate, float yCoordinate, DynamicDataDefinition definition, String text)
    throws NoSuchFieldException, IllegalAccessException
  {
    DynamicText dynamicText = new DynamicTextImpl();
    
    java.lang.reflect.Field colorField = Color.class.getDeclaredField(definition.getColor().toUpperCase());
    Color textColor = (Color) colorField.get(null);
    dynamicText.setColor(textColor);

    setFont(definition, dynamicText);

    dynamicText.setText(text);
    //dynamicText.setX(xCoordinate);
    dynamicText.setX(xCoordinate + 1.5F);
    //dynamicText.setY(yCoordinate);
    dynamicText.setY(yCoordinate + 1.5F);

    dynamicText.setDegrees(-1 * definition.getAngle());

    return dynamicText;
  }

  private static int getFontStyle(String fontName)
  {
    int fontStyle = 0;
    if (fontName.toLowerCase().contains("bold")) {
      fontStyle = 1;
    }

    if (fontName.toLowerCase().contains("italic")) {
      fontStyle += 2;
    }

    return fontStyle;
  }

  private static void setFont(DynamicDataDefinition definition, DynamicText dynamicText)
  {
    String fontName = definition.getFont().getName();
    String preparedFontName = fontName;
    int fontStyle = 0;

    if (fontName.toLowerCase().contains("times")) {
      preparedFontName = "Times New";
    }
    else if (fontName.contains("AvantGarde")) {
      preparedFontName = "Avant Garde";
    }
    else if (fontName.contains("Garamond"))
      preparedFontName = "Garamond";
    else if (fontName.contains("Palatino-Roman")) {
      preparedFontName = "Palatino";
    }
    else if (fontName.contains(",")) {
      preparedFontName = fontName.substring(0, fontName.indexOf(","));
    }
    else if (fontName.toLowerCase().contains("-")) {
      preparedFontName = fontName.substring(0, fontName.indexOf("-"));
    }

    if (fontName.toLowerCase().contains("palatino"))
      preparedFontName = preparedFontName + " Linotype";

    if ((fontName.toLowerCase().contains("roman")) && (!(fontName.toLowerCase().contains("palatino")))) {
      preparedFontName = preparedFontName + " Roman";
    }

    if (fontName.toLowerCase().contains("demi"))
      preparedFontName = preparedFontName + " Demi";

    if ((fontName.toLowerCase().contains("book")) && (!(fontName.toLowerCase().contains("bookman")))) {
      preparedFontName = preparedFontName + " Book";
    }

    if (fontName.toLowerCase().contains("narrow"))
      preparedFontName = preparedFontName + " Narrow";

    if (fontName.toLowerCase().contains("light"))
      preparedFontName = preparedFontName + " Light";

    if (fontName.toLowerCase().contains("medium"))
      preparedFontName = preparedFontName + " Medium";

    if (fontName.toLowerCase().contains("oblique")) {
      preparedFontName = preparedFontName + " Oblique";
    }

    fontStyle = getFontStyle(fontName);

    dynamicText.setFont(new java.awt.Font(preparedFontName, fontStyle, Integer.parseInt(definition.getSize()) - 2));
  }

  public static Pen getPenFromByteArray(byte[] bytes)
  throws IllegalStateException
  {
	  Pen pen = null;
	  try
	  {
	    if (bytes != null)
	    {
	      ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
	
	      pen = PenHome.read(inputStream, "Anoto_Forms_Solution_ASH");
	    }
	  } catch (Exception e) {
	    AshLogger.logSevere("Severe Error. " + e.getMessage());
	    throw new IllegalStateException("Severe Error. " + e.getMessage());
	  }
	
	  return pen;
  }

  public static List<String> getFormPagesAsList(Page submittedPage)
  {
    String[] pages = getFormPages(submittedPage);
    return Arrays.asList(pages);
  }

  @SuppressWarnings("unchecked")
  public static String[] getFormPages(Page submittedPage)
  {
    ArrayList pagesCurrentForm = new ArrayList();

    String paSubmittedPage = submittedPage.getPageAddress();

    int pageNumberSubmittedPage = submittedPage.getPageNumber();
    String paSubmittedPageNoPage = paSubmittedPage.substring(0, paSubmittedPage.lastIndexOf(".")).trim();

    int numberOfPages = PadFileUtility.getPageAddresses(paSubmittedPage, "Anoto_Forms_Solution_ASH").length;
    AshLogger.logFine("Page " + paSubmittedPage + " belongs to a form with " + numberOfPages + " pages.");

    String additionalPageAddress = "";
    int pageNumberCurrentPage = pageNumberSubmittedPage;
    int index = pageNumberSubmittedPage % numberOfPages;

    for (int i = index; i > index - numberOfPages; --i)
    {
      pageNumberCurrentPage = pageNumberSubmittedPage - i;
      additionalPageAddress = paSubmittedPageNoPage + "." + pageNumberCurrentPage;
      pagesCurrentForm.add(additionalPageAddress);
    }

    String[] formPageAddresses = (String[])(String[])pagesCurrentForm.toArray(new String[0]);
    Sorter.sortByPageAddressAsc(formPageAddresses);

    return formPageAddresses;
  }

  public static String getChecksum(String stringToHash)
  {
    Md5 md5 = new Md5(stringToHash);
    md5.processString();

    String hashedString = md5.getStringDigest();

    return hashedString; }

  public static boolean validResolution(String res) {
    int resolution;
    try {
      resolution = Integer.parseInt(res);
      if ((1 <= resolution) && (resolution <= 1000))
        return true;

      AshLogger.logSevere("Error: The resolution value in the PAD file for the formtype is not between 1 and 1000. Default resolution(200) will be used");
    }
    catch (Exception e)
    {
      AshLogger.logSevere("Error: The resolution value in the PAD file for the formtype is not an integer. Default resolution(200) will be used");
    }

    return false;
  }

  public static synchronized void createImages(FormCopyData formCopy, String additionalInfo) {
    try {
      AshLogger.logFine("About to create image for form " + formCopy.getPageAddressRange());
      Pen pen = getPenFromByteArray(formCopy.getPgc());
      Iterator pageIterator = pen.getPages();

      Page currentPage = (Page)pageIterator.next();
      List pageAddresses = getFormPagesAsList(currentPage);
      int pageNumber = 1;
      String imageFormat = formCopy.getFormType().getExportMethod().getImageFormat().getName().toLowerCase();
      RendererObserver observer = null;
      
      File exportFolder = new File(AshProperties.EXPORT_FOLDER + "\\" + formCopy.getFormType().getFormTypeName());
      if (!(exportFolder.exists())) {
          FileUtils.forceMkdir(exportFolder);
      }

      if (imageFormat.equalsIgnoreCase("pdf")) {
        observer = new RendererObserver(pageAddresses, exportFolder, "", "", "", formCopy.getFormType().getFormTypeName(), pen.getPenData().getPenSerial(), "", false);
      }
      
      for (Iterator i = pageAddresses.iterator(); i.hasNext(); ) { 
    	String address = (String) i.next();

        BackgroundFile backgroundFile = new BackgroundFile();
        backgroundFile.setFileName(PadFileUtility.getBackgroundImageForPage(address, "Anoto_Forms_Solution_ASH"));
        backgroundFile.setFormType(formCopy.getFormType());

        backgroundFile = (BackgroundFile) AshFormControl.getInstance().getBackgroundFile(backgroundFile).get(0);

        InputStream bgStream = new ByteArrayInputStream(backgroundFile.getBackgroundFile());

        String backgroundImageIdentifier = formCopy.getFormType().getFormTypeName() + "|_|" + backgroundFile.getFileName();
        try
        {
          currentPage = getPenFromByteArray(formCopy.getPgc()).getPage(address);

          String fileName = createPageAddressFileName(formCopy, address, exportFolder.getPath(), additionalInfo);
          File pageFile = new File(fileName + "." + imageFormat);
          
          if (imageFormat.equalsIgnoreCase("pdf"))
          {	
            JRenderer renderer = render(formCopy, currentPage, backgroundImageIdentifier, bgStream, pageFile.getPath(), false);
            
            observer.addRendererToObserve(renderer);

          } else {
            render(formCopy, currentPage, backgroundImageIdentifier, bgStream, pageFile.getPath(), true);
          }

          ++pageNumber;
        }
        catch (NoSuchPageException nope)
        {
          String fileName = createPageAddressFileName(formCopy, address, exportFolder.getPath(), additionalInfo);
          File emptyPageFile = new File(fileName + "." + imageFormat);

          if (imageFormat.equalsIgnoreCase("pdf")) {
            JRenderer renderer = render(formCopy, address, backgroundImageIdentifier, bgStream, emptyPageFile.getPath(), false);
            observer.addRendererToObserve(renderer);
          }
          else {
            render(formCopy, address, backgroundImageIdentifier, bgStream, emptyPageFile.getPath(), true);
          }

          AshLogger.logFine("No page with the address " + address + ", creating a file with just the background image: " + emptyPageFile.getAbsolutePath());
          ++pageNumber;
        }
        catch (Exception pe) {
          pe.printStackTrace();
          AshLogger.logSevere(pe.getMessage());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      AshLogger.logSevere(e.getMessage());
    }
  }

  public static boolean validPatternSegment(int segment)
  {
    int[] allowedSegments = AshProperties.ALLOWED_PATTERN_SEGMENTS;
    for (int i = 0; i < allowedSegments.length; ++i) {
      if (segment == allowedSegments[i])
        return true;

    }

    return false;
  }

  public static void loadPadFromByteArray(byte[] padByteArray) {
    InputStream padStream;
    try {
      padStream = new ByteArrayInputStream(padByteArray);

      PenHome.loadPad("Anoto_Forms_Solution_ASH", padStream, true);
    } catch (Exception e) {
      AshLogger.logSevere("Error when loading pad from blob, reason: " + e.getMessage());
      throw new IllegalStateException("Severe Error. " + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  public static List<String> calculatePossibleAddressRanges(PadFile padFile) throws SQLException
  {
    int numberOfAddresses = PadFileUtility.getNumberOfLicensePages(new ByteArrayInputStream(padFile.getPadFile()));
    List pageAddresses = new ArrayList();

    int numberOfBooks = numberOfAddresses / 256;
    AshLogger.logFine("Number of books in license: " + numberOfBooks);

    int nrOfPagesPerForm = PadFileUtility.getPageAddresses(new ByteArrayInputStream(padFile.getPadFile())).length;
    String startAddress = PadFileUtility.getPageAddresses(new ByteArrayInputStream(padFile.getPadFile()))[0];

    if (numberOfBooks <= 1)
      pageAddresses.addAll(calculatePossibleAddressesForABook(startAddress, nrOfPagesPerForm, numberOfAddresses));
    else {
      for (int currentBook = 0; currentBook < numberOfBooks; ++currentBook) {
        String paNoPage = startAddress.substring(0, startAddress.lastIndexOf("."));

        String paNoPageNoBook = paNoPage.substring(0, paNoPage.lastIndexOf("."));
        String currentAddress = paNoPageNoBook + "." + currentBook + ".0";
        pageAddresses.addAll(calculatePossibleAddressesForABook(currentAddress, nrOfPagesPerForm, 256));
      }

    }

    return pageAddresses;
  }

  @SuppressWarnings("unchecked")
  private static List<String> calculatePossibleAddressesForABook(String startAddress, int nrOfPagesPerForm, int numberOfAddresses)
  {
    List pageAddresses = new ArrayList();

    for (int i = 0; i < numberOfAddresses / nrOfPagesPerForm; ++i)
    {
      int startPage = Integer.valueOf(startAddress.substring(startAddress.lastIndexOf(".") + 1, startAddress.length())).intValue();
      startPage += i * nrOfPagesPerForm;
      int endPage = startPage + nrOfPagesPerForm - 1;

      String address = "";

      if (startPage == endPage)
        address = startAddress.substring(0, startAddress.lastIndexOf(".")) + "." + startPage;
      else {
        address = startAddress.substring(0, startAddress.lastIndexOf(".")) + "." + startPage + "-" + endPage;
      }

      pageAddresses.add(address);
    }

    return pageAddresses;
  }

  @SuppressWarnings("unchecked")
  //public static FormCopyResult addFormCopiesWithDynData(List<String> pageAddressRanges, InputStream xmlStream, FormTypeData formTypeData)
  public static FormCopyResult addFormCopiesWithDynData(List<String> pageAddressRanges,  DynamicDataDecorator dynamicData, FormTypeData formTypeData)
    throws SQLException
  {
    String[] addressRanges = (String[])(String[])pageAddressRanges.toArray(new String[0]);
    Sorter.sortRangesByPageAddressAsc(addressRanges);

    FormCopyResult addResult = new FormCopyResult();

    AshLogger.logFine("About to add formcopies with dynamic data");
    //Wagner - Comentei
    //DynamicDataDecorator dynamicData = new DynamicDataDecorator();
    //dynamicData.setInputStream(xmlStream);

    if (dynamicData.getDynamicData().getData().getForm().size() > addressRanges.length) {
      AshLogger.logSevere("The XML file contains more form copies than the license.");
      addResult.setFormCopyOperationSuccessful(false);
      addResult.setFormCopyOperationMessage("The XML covers more copies than the license.");
      return addResult;
    }
    
    int i = 0;
    HashSet dynamicDataDefinitions = null;
    String[] array = addressRanges; 
    int length = array.length; 
    
    for (int i1 = 0; i1 < length; ++i1) { 
      String currentPageAddressRange = array[i1];

      dynamicDataDefinitions = new HashSet();

      FormCopyData formCopy = new FormCopyData();
      formCopy.setPageAddressRange(currentPageAddressRange);
      AshLogger.logFine("Current form: " + currentPageAddressRange);
      formCopy.setFormType(formTypeData);
      formCopy.setLocked(0);

      String endPage = "";

      if (currentPageAddressRange.contains("-"))
        endPage = currentPageAddressRange.substring(0, currentPageAddressRange.lastIndexOf(".") + 1) + currentPageAddressRange.substring(currentPageAddressRange.lastIndexOf("-") + 1, currentPageAddressRange.length());
      else {
        endPage = currentPageAddressRange;
      }

      formCopy.setEndPageAddress(endPage);
      
      // Wagner - Inclui
      // Início
      // Atualiza campo com o endereço completo da malha
      PageAddress address = new PageAddress(endPage);
      int segment = address.getSegment();
      int shelf = address.getShelf();
      int book = address.getBook();
      int page = address.getPage();
      
      DecimalFormat df = new DecimalFormat("0000");
      String endPageAddressComplete = "0" + "." +  df.format(segment) + "." + df.format(shelf) + "." + df.format(book) + "." + df.format(page);
      
      formCopy.setEndPageAddressComplete(endPageAddressComplete);
      // Fim

      HashMap definitionFields = new HashMap();

      Dynamicdata.Settings.Fields fields = dynamicData.getDynamicData().getSettings().getFields();
      
      // Wagner - Inclui
      String numeroOcorrencia = "";

      if (fields != null) {
        for (Iterator i2 = fields.getField().iterator(); i2.hasNext(); ) { 
          Dynamicdata.Settings.Fields.Field definitionField = (Dynamicdata.Settings.Fields.Field) i2.next();

          if (definitionField != null) {
            definitionFields.put(definitionField.getName(), definitionField);
          }
        }

        HashMap currentDynamicDataFields = getDynamicDataFieldsByCopyNumber(i, dynamicData);
        
        // Wagner - Inclui
        // Início
	  	try {
	  		numeroOcorrencia = (String) currentDynamicDataFields.get(AshProperties.getAfsProperty(AshProperties.NUMERO_OCORRENCIA));
	  	} catch(Exception e) {
	  		AshLogger.logSevere("Erro ao obter as informações do arquivo XML.");
	  		throw new IllegalStateException("Severe Error. " + e.getMessage());
	  	}
	  	// Fim

        for (Iterator i3 = definitionFields.keySet().iterator(); i3.hasNext(); ) { 
          String fieldName = (String) i3.next();

          DynamicDataEntry dynamicDataEntry = new DynamicDataEntry();
          
          // Wagner - Comentei
          //DynamicDataDefinition definition = convertFieldDefinitionToDynamicDataDefinition((Dynamicdata.Settings.Fields.Field)definitionFields.get(fieldName));

          // Wagner - Comentei
          //dynamicDataDefinitions.add(definition);

          // Wagner - Comentei
          //completeDynamicDataDefinitionsIfNeeded(dynamicDataDefinitions, formTypeData);

          // Wagner - Comentei
          //dynamicDataEntry.setName(definition.getFieldName());
          
          // Wagner - Inclui
          if (formTypeData.getDynamicDataDefinitions() == null || formTypeData.getDynamicDataDefinitions().size() == 0) {
        	  DynamicDataDefinition definition = convertFieldDefinitionToDynamicDataDefinition((Dynamicdata.Settings.Fields.Field)definitionFields.get(fieldName));
        	  dynamicDataDefinitions.add(definition);
          }
          
          // Wagner - Inclui
          dynamicDataEntry.setName(fieldName);

          if (currentDynamicDataFields.containsKey(fieldName)) {
            // Wagner - Comentei
        	//dynamicDataEntry.setValue((String) currentDynamicDataFields.get(fieldName));
        	  
        	// Wagner - Inclui
       	    String value = (String) currentDynamicDataFields.get(fieldName);
          	if (value == null || value.equals("") || value.length() == 0) {
          		value = " ";
          	}
            dynamicDataEntry.setValue(value);
          } else {
        	// Wagner - Comentei
            //dynamicDataEntry.setValue("");
        	dynamicDataEntry.setValue(" ");
          }
          
          formCopy.getDynamicDataEntries().add(dynamicDataEntry);
        }
      }
      formCopy.setLatestSubmit(new Date(System.currentTimeMillis()));

      /*
       * Wagner - Comentei
      UserData owner = new UserData();
      
      List roles = AshUserControl.getInstance().getRoles();

      for (Iterator i4 = roles.iterator(); i4.hasNext(); ) { 
    	Role role = (Role) i4.next();
        if (role.getRoleName().equals("Admin"))
          owner.setRole(role);
      }

      owner = (UserData) AshUserControl.getInstance().getUser(owner).get(0);

      formCopy.setOwner(owner);
      */
      
      AshFormControl formControl = AshFormControl.getInstance();

      // Wagner - Inclui
      formCopy.setNumeroOcorrencia(numeroOcorrencia);
      
   	  //addResult = AshFormControl.getInstance().addFormCopy(formCopy);
      addResult = formControl.addFormCopy(formCopy);
      
      if (!(addResult.isFormCopyOperationSuccessful()))
      {
        AshLogger.logSevere(addResult.getFormCopyOperationMessage());

        break;
      }

      /* Wagner - Comentei
      if (i == 0)
      {
        completeDynamicDataDefinitionsIfNeeded(dynamicDataDefinitions, formTypeData);

        formTypeData.setDynamicDataDefinitions(dynamicDataDefinitions);

        AshFormControl.getInstance().updateFormType(formTypeData);
      }
      */
      
      // Wagner - Inclui
      if (formTypeData.getDynamicDataDefinitions() == null || formTypeData.getDynamicDataDefinitions().size() == 0) {
    	  formTypeData.setDynamicDataDefinitions(dynamicDataDefinitions);
    	  AshFormControl.getInstance().updateFormType(formTypeData);
      }

      ++i;
    }

    return addResult;
  }
  
  @SuppressWarnings("unchecked")
  public static FormCopyResult addFormCopiesWithDynData(List<String> pageAddressRanges, InputStream xmlStream, FormTypeData formTypeData)
    throws SQLException
  {
    String[] addressRanges = (String[])(String[])pageAddressRanges.toArray(new String[0]);
    Sorter.sortRangesByPageAddressAsc(addressRanges);

    FormCopyResult addResult = new FormCopyResult();

    AshLogger.logFine("About to add formcopies with dynamic data");
    DynamicDataDecorator dynamicData = new DynamicDataDecorator();
    dynamicData.setInputStream(xmlStream);

    if (dynamicData.getDynamicData().getData().getForm().size() > addressRanges.length) {
      AshLogger.logSevere("The XML file contains more form copies than the license.");
      addResult.setFormCopyOperationSuccessful(false);
      addResult.setFormCopyOperationMessage("The XML covers more copies than the license.");
      return addResult;
    }

    int i = 0;
    HashSet dynamicDataDefinitions = new HashSet();
    String[] array = addressRanges; 
    int length = array.length; 
    
    for (int i1 = 0; i1 < length; ++i1) { 
      String currentPageAddressRange = array[i1];

      dynamicDataDefinitions = new HashSet();

      FormCopyData formCopy = new FormCopyData();
      formCopy.setPageAddressRange(currentPageAddressRange);
      AshLogger.logFine("Current form: " + currentPageAddressRange);
      formCopy.setFormType(formTypeData);
      formCopy.setLocked(0);

      String endPage = "";

      if (currentPageAddressRange.contains("-"))
        endPage = currentPageAddressRange.substring(0, currentPageAddressRange.lastIndexOf(".") + 1) + currentPageAddressRange.substring(currentPageAddressRange.lastIndexOf("-") + 1, currentPageAddressRange.length());
      else {
        endPage = currentPageAddressRange;
      }

      formCopy.setEndPageAddress(endPage);

      HashMap definitionFields = new HashMap();

      Dynamicdata.Settings.Fields fields = dynamicData.getDynamicData().getSettings().getFields();
      
      // Wagner - Inclui
      String numeroOcorrencia = "";

      if (fields != null) {
        for (Iterator i2 = fields.getField().iterator(); i2.hasNext(); ) { 
          Dynamicdata.Settings.Fields.Field definitionField = (Dynamicdata.Settings.Fields.Field) i2.next();

          if (definitionField != null) {
            definitionFields.put(definitionField.getName(), definitionField);
          }

        }

        HashMap currentDynamicDataFields = getDynamicDataFieldsByCopyNumber(i, dynamicData);
        
	  	try {
	  		numeroOcorrencia = (String) currentDynamicDataFields.get(AshProperties.getAfsProperty(AshProperties.NUMERO_OCORRENCIA));
	  	} catch(Exception e) {
	  		AshLogger.logSevere("Erro ao obter as informações do arquivo XML.");
	  		throw new IllegalStateException("Severe Error. " + e.getMessage());
	  	}

        for (Iterator i3 = definitionFields.keySet().iterator(); i3.hasNext(); ) { 
          String fieldName = (String) i3.next();

          DynamicDataEntry dynamicDataEntry = new DynamicDataEntry();
          DynamicDataDefinition definition = convertFieldDefinitionToDynamicDataDefinition((Dynamicdata.Settings.Fields.Field)definitionFields.get(fieldName));

          dynamicDataDefinitions.add(definition);

          completeDynamicDataDefinitionsIfNeeded(dynamicDataDefinitions, formTypeData);

          dynamicDataEntry.setName(definition.getFieldName());

          if (currentDynamicDataFields.containsKey(fieldName)) {
            // Wagner - Comentei
        	//dynamicDataEntry.setValue((String) currentDynamicDataFields.get(fieldName));
        	  
        	// Wagner - Inclui
       	    String value = (String) currentDynamicDataFields.get(fieldName);
          	if (value == null || value.equals("") || value.length() == 0) {
          		value = " ";
          	}
            dynamicDataEntry.setValue(value);
          } else {
        	// Wagner - Comentei
            //dynamicDataEntry.setValue("");
        	dynamicDataEntry.setValue(" ");
          }
          
          formCopy.getDynamicDataEntries().add(dynamicDataEntry);
        }
      }
      formCopy.setLatestSubmit(new Date(System.currentTimeMillis()));

      UserData owner = new UserData();

      List roles = AshUserControl.getInstance().getRoles();

      for (Iterator i4 = roles.iterator(); i4.hasNext(); ) { 
    	Role role = (Role) i4.next();
        if (role.getRoleName().equals("Admin"))
          owner.setRole(role);
      }

      owner = (UserData)AshUserControl.getInstance().getUser(owner).get(0);

      formCopy.setOwner(owner);
      
      AshFormControl formControl = AshFormControl.getInstance();

      // Wagner - Inclui
      formCopy.setNumeroOcorrencia(numeroOcorrencia);
      
   	  //addResult = AshFormControl.getInstance().addFormCopy(formCopy);
      addResult = formControl.addFormCopy(formCopy);
      
      if (!(addResult.isFormCopyOperationSuccessful()))
      {
        AshLogger.logSevere(addResult.getFormCopyOperationMessage());

        break;
      }

      if (i == 0)
      {
        completeDynamicDataDefinitionsIfNeeded(dynamicDataDefinitions, formTypeData);

        formTypeData.setDynamicDataDefinitions(dynamicDataDefinitions);

        AshFormControl.getInstance().updateFormType(formTypeData);
      }

      ++i;
    }

    return addResult;
  }

  private static DynamicDataDefinition convertFieldDefinitionToDynamicDataDefinition(Dynamicdata.Settings.Fields.Field definitionField)
  {
    DynamicDataDefinition definition = new DynamicDataDefinition();
    definition.setFieldName(definitionField.getName());
    definition.setColor(definitionField.getText().getColor());
    definition.setAngle(definitionField.getText().getAngle());
    definition.setSize(Integer.toString(definitionField.getText().getSize()));

    int onlyShowInExport = (definitionField.isHidden()) ? 1 : 0;
    definition.setOnlyShowInExport(onlyShowInExport);

    AshLogger.logFine("color,angle,size= " + definition.getColor() + "," + definition.getAngle() + "," + definition.getSize());

    com.anoto.ash.database.Font font = new com.anoto.ash.database.Font();
    font.setName(definitionField.getText().getFont());

    List<?> fontList = AshFormControl.getInstance().getFont(font);

    if (fontList == null)
    {
      font.setName(AshProperties.defaultFont);
      fontList = AshFormControl.getInstance().getFont(font);
    }
    font = (com.anoto.ash.database.Font)fontList.get(0);

    definition.setFont(font);

    AshLogger.logFine("Font= " + font.getName());

    return definition;
  }

  private static void completeDynamicDataDefinitionsIfNeeded(HashSet<DynamicDataDefinition> dynamicDataDefinitions, FormTypeData formTypeData)
  {
    for (Iterator i = dynamicDataDefinitions.iterator(); i.hasNext(); ) { 
      DynamicDataDefinition definition = (DynamicDataDefinition) i.next();
      DynamicDataDefinition tempDefinition = AshFormControl.getInstance().getDynamicDataDefinition(definition.getFieldName(), formTypeData);

      if (tempDefinition != null)
      {
        definition.setId(tempDefinition.getId());

        if (definition.getOnlyShowInExport() != 1) {
          definition.setOnlyShowInExport(tempDefinition.getOnlyShowInExport());
        }

        int updateResult = AshFormControl.getInstance().updateDynamicDataDefinition(definition);

        if (updateResult < 0) {
          AshLogger.logSevere("Something went wrong when updating a Dynamic Data Definition");
          throw new IllegalStateException("Something went wrong when updating a Dynamic Data Definition");
        }
      }
    }
  }

  public static String getEndPageAddressFromRange(String pageAddressRange)
  {
    AshLogger.logFine("About to get the end page address for the form with page address range: " + pageAddressRange);
    String endPageAddress = pageAddressRange;

    boolean severalPages = pageAddressRange.contains("-");

    if (severalPages) {
      String paNoPage = pageAddressRange.substring(0, pageAddressRange.lastIndexOf(".")).trim();

      String endPage = pageAddressRange.substring(pageAddressRange.lastIndexOf("-") + 1, pageAddressRange.length());
      endPageAddress = paNoPage + "." + endPage;
    }
    AshLogger.logFine("End page address: " + endPageAddress);
    return endPageAddress;
  }

  @SuppressWarnings("unchecked")
  public static boolean addDynamicDataToExport(ResultDocument resultDoc, FormCopyData formCopy)
  {
    AshLogger.logFine("About to add dynamic data to ");
    boolean dynamicDataAdded = false;
    if (formCopy.getDynamicDataEntries() != null) {
      Iterator dynDataEntries = formCopy.getDynamicDataEntries().iterator();

      while (dynDataEntries.hasNext()) {
        DynamicDataEntry dynamicDataEntry = (DynamicDataEntry)dynDataEntries.next();

        Hashtable fieldAttributes = new Hashtable();
        fieldAttributes.put("fieldType", "dynamic_data");
        dynamicDataAdded = true;
        VOFormProcessorToResultDocumentFactory.addFieldToResultDocument(resultDoc, dynamicDataEntry.getName(), dynamicDataEntry.getValue(), fieldAttributes);
      }

    }

    return dynamicDataAdded;
  }
  
  @SuppressWarnings("unchecked")
  public static HashMap<String, String> getDynamicDataFieldsByCopyNumber(int copyNumber, DynamicDataDecorator dynamicData)
  {
    HashMap result = new HashMap();
    Dynamicdata.Data.Form form = (Dynamicdata.Data.Form)dynamicData.getDynamicData().getData().getForm().get(copyNumber);

    for (Iterator i$ = form.getField().iterator(); i$.hasNext(); ) { com.anoto.afs.dynamicdata.xml.Dynamicdata.Data.Form.Field field = (com.anoto.afs.dynamicdata.xml.Dynamicdata.Data.Form.Field)i$.next();
      result.put(field.getName(), field.getValue());
    }

    return result;
  }

  public static synchronized String createShortPageAddressFileName(FormCopyData formCopyData, String address, String dir, String additionalInfo) {
    StringBuffer fileName = new StringBuffer(dir).append(File.separator);
    fileName.append(address).append("_");
    fileName.append("_").append(additionalInfo);

    return fileName.toString();
  }

  public static synchronized String createPageAddressFileName(FormCopyData formCopyData, String address, String dir, String additionalInfo) {
	  StringBuffer fileName = new StringBuffer(dir).append(File.separator);
	  fileName.append(formCopyData.getOwner().getPenId()).append("_");
	  fileName.append(address).append("_");
	  if (formCopyData.getNumeroOcorrencia() != null && !(formCopyData.getNumeroOcorrencia().trim()).equals("")) {
		  fileName.append(formCopyData.getNumeroOcorrencia());
	  }
	  return fileName.toString();
  }

  private static synchronized String getDateString(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss_(S)");

    return dateFormat.format(date);
  }

  public static String getAdminEmailAddresses()
  {
    Role role = UserDBHandler.getRole("Admin");
    UserData admin = new UserData();
    admin.setRole(role);

    List admins = AshUserControl.getInstance().getUser(admin);

    String adminAddress = "";

    for (Iterator i$ = admins.iterator(); i$.hasNext(); ) { UserData currentAdmin = (UserData)i$.next();

      if ((currentAdmin.getEmail() != null) && (currentAdmin.getEmail().length() > 0) && (ASHValidatorUtility.validEmailAddress(currentAdmin.getEmail()))) {
        adminAddress = adminAddress + currentAdmin.getEmail() + ",";
      }

    }

    if (adminAddress.lastIndexOf(",") > 0) {
      adminAddress = adminAddress.substring(0, adminAddress.lastIndexOf(","));
    }

    return adminAddress;
  }

  public static String getFormPaRange(String[] formPages)
  {
    String paRangeString = "";

    String firstPageAddress = formPages[0];

    int pageNumberFirstPage = new PageAddress(firstPageAddress).getPage();

    String lastPageAddress = formPages[(formPages.length - 1)];

    int pageNumberLastPage = new PageAddress(lastPageAddress).getPage();

    if (pageNumberFirstPage == pageNumberLastPage)
    {
      paRangeString = firstPageAddress;
    }
    else {
      paRangeString = firstPageAddress + "-" + pageNumberLastPage;
    }

    return paRangeString;
  }

  public static synchronized File getDirectoryForFormCopy(FormCopyData formCopyData, String baseDir) {
    File tmpDir = new File(baseDir);

    if (!(tmpDir.exists())) {
      tmpDir.mkdir();
    }

    File formTypeDir = new File(tmpDir.getPath() + File.separator + formCopyData.getFormType().getFormTypeName());

    if (!(formTypeDir.exists())) {
      formTypeDir.mkdir();
    }

    File pageAddressRangeDir = new File(formTypeDir + File.separator + formCopyData.getPageAddressRange() + "_" + System.currentTimeMillis());

    if (!(pageAddressRangeDir.exists())) {
      pageAddressRangeDir.mkdir();
    }

    return pageAddressRangeDir;
  }

  @SuppressWarnings("unchecked")
  public static String generateExampleXml(int numberOfForms, String printerName, FormTypeData formType, String outputFolder) throws IOException, FileNotFoundException, JAXBException, SAXException, SQLException {
    Dynamicdata dynData = new Dynamicdata();

    PadFile padFile = new PadFile();
    padFile.setFormType(formType);

    List padList = AshFormControl.getInstance().getPadFile(padFile);
    List dynDataFields = new ArrayList();
    if ((padList != null) && (padList.size() > 0)) {
      padFile = (PadFile)padList.get(0);

      InputStream padStream = new ByteArrayInputStream(padFile.getPadFile());

      dynDataFields = DynamicDataValidator.getFieldNamesFromPad(padStream, true);
    }

    dynData.setFormtype(formType.getFormTypeName());

    Dynamicdata.Settings settings = new Dynamicdata.Settings();
    Dynamicdata.Settings.Fields fields = new Dynamicdata.Settings.Fields();

    for (Iterator i = dynDataFields.iterator(); i.hasNext(); ) { 
      String dynDataFieldName = (String) i.next();

      Dynamicdata.Settings.Fields.Field currentField = new Dynamicdata.Settings.Fields.Field();

      currentField.setName(dynDataFieldName);

      Dynamicdata.Settings.Fields.Field.Text text = new Dynamicdata.Settings.Fields.Field.Text();
      text.setAngle(0);
      text.setColor("Black");
      text.setFont("Arial");
      text.setSize(10);
      currentField.setText(text);

      currentField.setHidden(new Boolean(false));

      fields.getField().add(currentField);
    }

    settings.setFields(fields);

    Dynamicdata.Settings.Printer printer = new Dynamicdata.Settings.Printer();
    printer.setName(printerName);
    settings.setPrinter(printer);

    dynData.setSettings(settings);

    Dynamicdata.Data data = new Dynamicdata.Data();

    List formList = data.getForm();

    for (int i1 = 0; i1 < numberOfForms; ++i1) {
      Dynamicdata.Data.Form form = new Dynamicdata.Data.Form();
      form.setCopyNumber(BigInteger.valueOf(i1 + 1));

      for (Iterator i2 = dynDataFields.iterator(); i2.hasNext(); ) { 
    	String dynDataFieldName = (String) i2.next();
        com.anoto.afs.dynamicdata.xml.Dynamicdata.Data.Form.Field field1 = new com.anoto.afs.dynamicdata.xml.Dynamicdata.Data.Form.Field();
        field1.setName(dynDataFieldName);
        field1.setValue(dynDataFieldName + " value " + (i1 + 1));

        form.getField().add(field1);
      }

      formList.add(form);
    }

    dynData.setData(data);

    if (!(outputFolder.endsWith("\\"))) {
      outputFolder = outputFolder + "\\";
    }

    if (!(new File(outputFolder).exists())) {
      FileUtils.forceMkdir(new File(outputFolder));
    }

    String filePath = outputFolder + "\\" + formType.getFormTypeName() + "_auto_generated_dyndata_example.xml";
    Parser.writeDynamicData(dynData, filePath);

    return filePath;
  }

  @SuppressWarnings("unused")
  private static synchronized long getTimeStamp() {
    return System.currentTimeMillis();
  }

  public static FormCopyData removeTemporaryPgc(FormCopyData formCopyData)
  {
    FormCopyData newFormCopy = new FormCopyData();
    newFormCopy.setFormCopyId(formCopyData.getFormCopyId());
    newFormCopy.setPgc(formCopyData.getPgc());
    newFormCopy.setPageAddressRange(formCopyData.getPageAddressRange());
    newFormCopy.setEndPageAddress(formCopyData.getEndPageAddress());
    newFormCopy.setMarkedCompleted(formCopyData.getMarkedCompleted());
    newFormCopy.setVerificationNeeded(formCopyData.getVerificationNeeded());
    newFormCopy.setMandatoryFieldsMissing(formCopyData.getMandatoryFieldsMissing());
    newFormCopy.setLatestSubmit(formCopyData.getLatestSubmit());
    newFormCopy.setVerificationInProgress(formCopyData.getVerificationInProgress());
    newFormCopy.setHidden(formCopyData.getHidden());
    newFormCopy.setLocked(formCopyData.getLocked());
    newFormCopy.setExportMailFailed(formCopyData.getExportMailFailed());
    newFormCopy.setImageMailFailed(formCopyData.getImageMailFailed());
    newFormCopy.setOwner(formCopyData.getOwner());
    newFormCopy.setFormType(formCopyData.getFormType());

    return newFormCopy;
  }
  
  // Wagner - Inclui
  /*
  @SuppressWarnings("unchecked")
  public static void addDynamicDataInFormCopy(FormCopyData formCopy) {
      if (formCopy.getNumeroOcorrencia() != null && !((formCopy.getNumeroOcorrencia()).trim()).equals("")) {
    	  // Busca os DynamicDataEntry a partir do número do atendimento.
          DynamicDataEntry dataEntry = new DynamicDataEntry();
	      dataEntry.setNumeroOcorrencia(formCopy.getNumeroOcorrencia());
		  List list = AshFormControl.getInstance().searchDynamicDataEntry(dataEntry);
		  if (list != null && list.size() > 0) {
		   	  formCopy.setDynamicDataEntries(new HashSet<DynamicDataEntry>(list));
		  }
      }
  }
  */
  
  // Wagner - Inclui
  /*
  public static void deletePageAddressFileName(String dir, String fileName, String imageFormat) {
	  File[] fileList = searchFileStartsWith(dir, fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.lastIndexOf("_")));
      
      if (fileList.length > 0) {
	      for (int j = 0; j < fileList.length; j++) {
	    	  File file = fileList[j];
	    	  
	    	  if (file.getName().endsWith("_." + imageFormat)) {
	    		  try {
	    			  FileUtils.forceDelete(file);
	    		  } catch (IOException e) {
	    			  AshLogger.logSevere("Delete failed: Couldn't delete the file, " + file.getAbsolutePath());
	    		  }
	    	  }
		  }
      }
  }
  */
  
  public static byte[] generateBarcode(String data) {
	  byte[] imgb = null;
	  final int dpi = 150;
		
	  try {
		  //Create the barcode bean
		  Code39Bean bean = new Code39Bean();
	        
		  //Configure the barcode generator
		  bean.setModuleWidth(UnitConv.in2mm(1.0f / dpi)); 	//makes the narrow bar 
		  													//width exactly one pixel
		  
		  bean.setWideFactor(3);
		  bean.doQuietZone(true);
		  bean.setFontSize(2);
		  bean.setBarHeight(30);
		  		  
		  //Open output file
		  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	
		  //Set up the canvas provider for monochrome JPEG output 
		  BitmapCanvasProvider canvas = new BitmapCanvasProvider(baos, "image/jpeg", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
	      
		  //Generate the barcode
		  bean.generateBarcode(canvas, data);
		  
		  //Signal end of generation
		  canvas.finish();
			
		  imgb = baos.toByteArray();
			
	  } catch (Exception e) {
		  AshLogger.logSevere(e.getMessage());
		  e.printStackTrace();
	  }
	  
	  return imgb;
  }
  
  // Wagner - Inclui
  public static File[] searchFileStartsWith(String dir, String fileName) {
	  File pageDir = new File(dir);

      class FileFilter implements FilenameFilter {
    	  String fileName = "";
    	  public FileFilter(String fileName) {
    		  this.fileName = fileName;
    	  }

    	  public boolean accept(File dir, String name) {
    		   return name.startsWith(fileName);
    	  }
      }
      
      FilenameFilter filter = new FileFilter(fileName);
      File[] fileList = pageDir.listFiles(filter);
	  
      return fileList;
  }
  
  public static Date dateWithTime(long timestamp) {
	  Calendar cal = Calendar.getInstance();
	  cal.setTime(new Date(timestamp));
	  return cal.getTime();
  }
  
  /**
	* Current date with time. 
	*/
  public static Date currentTimestamp() {
	  return dateWithTime(System.currentTimeMillis());
  }
  
  /**
	* Current date with time. 
	*/
  public static Time getTime(Date date) {
	  date = dateWithTime(date.getTime());
	  return new Time(date.getTime());
  }
  
  public static String getCurrentDateAndTime(String format) {
	  Locale locale = new Locale("pt","BR"); 
	  GregorianCalendar calendar = new GregorianCalendar(); 
	  SimpleDateFormat dateFormat = new SimpleDateFormat(format,locale); 
	  String currentDate = dateFormat.format(calendar.getTime());
		
	  return currentDate;
  }
  
  public static String getDateString(Date date, String format) {
	  Locale locale = new Locale("pt","BR"); 
	  SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale); 
	  String dateString = dateFormat.format(date);
		
	  return dateString;
  }
  
  public static String convertArrayToStringList(Object[] object) {
	  String list = "";
	  if (object != null && object.length > 0) {
		  for (int i = 0; i < object.length; i++) {
			  list += object[i];
		      if (i < object.length - 1) {
		    	  list += ", ";
		      }
		  }
	  }
	  return list;
  }
  
  public static Date parseDate(String dateString) {
	  Date result = null;
	  try {
		  if (dateString != null && dateString.length() == 10) {
			  Locale locale = new Locale("pt","BR"); 
			  SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", locale); 
			  result = dateFormat.parse(dateString);
		  }
	  } catch (ParseException e) {
		  //AshLogger.logSevere(e.getMessage());
		  //e.printStackTrace();
	  }
	  return result;
  }
  
  public static Time parseTime(String timeString) {
	  Time result = null;
	  try {
		  Locale locale = new Locale("pt","BR"); 
		  SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", locale); 
		  Date date = dateFormat.parse(timeString);
		  result = new Time(date.getTime());
	  } catch (ParseException e) {
		  //AshLogger.logSevere(e.getMessage());
		  //e.printStackTrace();
	  }
	  return result;
  }
  
  /** 
	 * Returns number of days between startDate and endDate<p> 
	 *  
	 * @param java.util.Date startDate
	 * @param java.util.Date endDate
	 * @param boolean includeStartDate<p>
	 *  
	 */	
	  public static int daysInterval (Date startDate, Date endDate,
	  		boolean includeStartDate ) {
		
		startDate = removeTime(startDate);
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);		

		endDate = removeTime(endDate);		
		Calendar end = Calendar.getInstance();
		end.setTime(endDate);
		
		if (includeStartDate) {
			start.add(Calendar.DATE, -1);
		}
		
		int days = 0;
		while (start.before(end)) {
			days++;
			start.add(Calendar.DATE,1);
		}
		return days;
	}
	  
  /**
	 * Puts hours, minutes, seconds and milliseconds to zero. <p>
	 * 
	 * @return The same date sent as argument (a new date is not created). If null
	 * 			if sent a null is returned.
	 */
	  public static Date removeTime(Date date) {
		if (date == null) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0); 
		cal.set(Calendar.MINUTE, 0);		
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		date.setTime(cal.getTime().getTime());
		return date;
	}
	  
	public static Date parseDate(String dateString, String format) {
		Date result = null;
		try {
			if (dateString != null && dateString.length() == 10) {
				Locale locale = new Locale("pt","BR"); 
				SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale); 
				result = dateFormat.parse(dateString);
			}
		} catch (ParseException e) {
			//AshLogger.logSevere(e.getMessage());
			//e.printStackTrace();
		}
		return result;
	}

}