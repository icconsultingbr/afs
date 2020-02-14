package com.anoto.ash.web.actions.formtypes;

import com.anoto.ash.AshProperties;
import com.anoto.ash.database.ExportFormat;
import com.anoto.ash.database.ExportMethod;
import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.ImageFormat;
import com.anoto.ash.database.PadFile;
import com.anoto.ash.database.PredefinedThresholdData;
import com.anoto.ash.database.ThresholdData;
import com.anoto.ash.database.UserData;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.portal.AshSettingsControl;
import com.anoto.ash.services.FormTypeService;
import com.anoto.ash.services.ThresholdService;
import com.anoto.ash.services.UserService;
import com.anoto.ash.utils.ResourceHandler;
import com.anoto.ash.vo.ink.VOControl;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.ServletContext;
import org.apache.commons.io.FileUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;


@Name("editFormTypeAction")
@Scope(ScopeType.CONVERSATION)
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditFormTypeAction
{

  @In(create=true)
  private FormTypeService formTypeService;

  @In(create=true)
  private ThresholdService thresholdService;

  @In(create=true)
  private UserService userService;

  @In(value="editFormType", required=false)
  @Out(value="editFormType", required=false)
  private FormTypeData editFormType;

  @RequestParameter("editFormTypeName")
  String editFormTypeName;

  @DataModel("thresholds")
  private List<ThresholdData> thresholds;

  @DataModelSelection
  @In(value="selectedThreshold", required=false, create=true)
  @Out(value="selectedThreshold", required=false)
  private ThresholdData selectedThreshold;
  private List<PredefinedThresholdData> predefinedThresholds;
  private String exportFormat;
  private String imageFormat;
  private byte[] padFile;
  private String fileName;
  private long size;
  private byte[] xmlFile;
  private String xmlFileName;
  private long xmlSize;
  private int[] displayPeriods = { 0, 1, 2, 5, 10, 15, 30, 60, 90, 180, 365, 750, 1000, 5000, 10000 };
  private boolean mailExport;
  private boolean folderExport;
  private String selectedTab;
  private String uploadPADErrorMsg;
  private String uploadXMLErrorMsg;

  public EditFormTypeAction()
  {
    this.mailExport = false;
    this.folderExport = false;

    this.selectedTab = ResourceHandler.getResource("form_type_update_pattern");

    this.uploadPADErrorMsg = "";
    this.uploadXMLErrorMsg = ""; }

  public String getUploadPADErrorMsg() {
    return this.uploadPADErrorMsg;
  }

  public void setUploadPADErrorMsg(String uploadPADErrorMsg) {
    this.uploadPADErrorMsg = uploadPADErrorMsg;
  }

  public String getUploadXMLErrorMsg() {
    return this.uploadXMLErrorMsg;
  }

  public void setUploadXMLErrorMsg(String uploadXMLErrorMsg) {
    this.uploadXMLErrorMsg = uploadXMLErrorMsg;
  }

  public String getSelectedTab() {
    return this.selectedTab;
  }

  public void setSelectedTab(String selectedTab) {
    this.selectedTab = selectedTab;
  }

  public int[] getDisplayPeriods() {
    return this.displayPeriods;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void setPadFile(byte[] padFile) {
    this.padFile = padFile;
  }

  public byte[] getPadFile() {
    return this.padFile;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public long getSize() {
    return this.size;
  }

  public void setXmlFile(byte[] xmlFile) {
    this.xmlFile = xmlFile;
  }

  public byte[] getXmlFile() {
    return this.xmlFile;
  }

  public String getXmlFileName() {
    return this.xmlFileName;
  }

  public void setXmlFileName(String xmlFileName) {
    this.xmlFileName = xmlFileName;
  }

  public long getXmlSize() {
    return this.xmlSize;
  }

  public void setXmlSize(long xmlSize) {
    this.xmlSize = xmlSize;
  }

  public void setFormTypeToEdit()
  {
    if (this.editFormType == null) {
      this.editFormType = new FormTypeData();
      this.editFormType.setFormTypeName(this.editFormTypeName);
      this.editFormType = ((FormTypeData)this.formTypeService.getFormTypes(this.editFormType).get(0));
    }

    if ((this.thresholds == null) || (this.thresholds.size() < 1)) {
      this.thresholds = this.thresholdService.getThresholds(this.editFormType);
      Collections.sort(this.thresholds);
    }

    if ((this.predefinedThresholds == null) || (this.predefinedThresholds.size() == 0)) {
      this.predefinedThresholds = this.thresholdService.getPredefinedThresholdDatas();
    }

    if (this.editFormType.getExportMethod().getType().equalsIgnoreCase(ExportMethod.FOLDER_EXPORT)) {
      this.folderExport = true;
      this.mailExport = false;
    }
    else if (this.editFormType.getExportMethod().getType().equalsIgnoreCase(ExportMethod.MAIL_EXPORT)) {
      this.folderExport = false;
      this.mailExport = true;
    }
    else {
      this.folderExport = true;
      this.mailExport = false;
    }

    if (this.editFormType.getExportMethod().getExportFormat() != null) {
      this.exportFormat = this.editFormType.getExportMethod().getExportFormat().getName();
    }

    if (this.editFormType.getExportMethod().getImageFormat() != null)
      this.imageFormat = this.editFormType.getExportMethod().getImageFormat().getName();
  }

  public List<String> getAllUsers()
  {
    UserService userService = new UserService();
    List users = userService.getAllUsers();

    List tmp = new ArrayList();
    tmp.add("All");

    for (Iterator i$ = users.iterator(); i$.hasNext(); ) { UserData currentUser = (UserData)i$.next();
      tmp.add(currentUser.getUserName());
    }

    Collections.sort(tmp);

    return tmp;
  }

  public List<String> getAllPredefinedThresholds()
  {
    List names = new ArrayList();

    for (Iterator i$ = this.predefinedThresholds.iterator(); i$.hasNext(); ) { PredefinedThresholdData current = (PredefinedThresholdData)i$.next();
      names.add(current.getPredefinedName());
    }

    return names;
  }

  public List<String> getAllFields() throws MalformedURLException {
    List fieldNames = new ArrayList();
    try {
      VOControl voControl = new VOControl();
      fieldNames = voControl.getFieldNamesForFormType(this.editFormType.getFormTypeName());

      Collections.sort(fieldNames);
    }
    catch (Exception e)
    {
    }
    List tmp = new ArrayList();
    tmp.add("All");

    for (Iterator i$ = fieldNames.iterator(); i$.hasNext(); ) { String fieldStr = (String)i$.next();
      tmp.add(fieldStr);
    }

    return tmp;
  }

  public List<PadFile> getPads()
  {
    PadFile pad = new PadFile();
    pad.setFormType(this.editFormType);
    List padFiles = AshFormControl.getInstance().getPadFile(pad);

    return padFiles;
  }

  public void setExportFormat(String exportFormat)
  {
    this.exportFormat = exportFormat;
  }

  public String getExportFormat() {
    return this.exportFormat;
  }

  public void setImageFormat(String imageFormat) {
    this.imageFormat = imageFormat;
  }

  public String getImageFormat() {
    return this.imageFormat;
  }

  public void addNewThreshold() {
    ThresholdData threshold = new ThresholdData();
    threshold.setFormType(this.editFormType);
    threshold.setFieldName("All");

    PredefinedThresholdData pre = new PredefinedThresholdData();
    pre.setPredefinedName("Medium");

    pre = this.thresholdService.getPredefinedThresholdData(pre);

    threshold.setPredefinedValue(pre);

    UserData user = new UserData();
    user.setUserName("All");
    threshold.setUser(user);

    this.thresholds.add(threshold);
  }

  public String deleteFormType() {
    String result = "OK";

    if (!(this.editFormType.isCorrect())) {
      if (this.formTypeService.removeFormType(this.editFormType)) {
        result = "OK";
      }
      else
        result = "ERROR";

    }

    return result;
  }

  public void deleteThreshold(ThresholdData toDelete) {
    this.thresholds.remove(toDelete);
    this.thresholdService.deleteThreshold(toDelete);

    Collections.sort(this.thresholds);
  }

  public String getExampleXML() {
    String result = "";

    AshFormControl.getInstance().generateExampleXml(this.editFormType);
    File source = new File(AshProperties.ashRootFolderPath + File.separator + this.editFormType.getFormTypeName() + "_auto_generated_dyndata_example.xml");
    ServletContext context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();

    File destiny = new File(context.getRealPath("/temp_img/" + this.editFormType.getFormTypeName() + "_auto_generated_dyndata_example.xml"));
    try
    {
      FileUtils.copyFile(source, destiny);

      result = "/afs/temp_img/" + this.editFormType.getFormTypeName() + "_auto_generated_dyndata_example.xml";
    }
    catch (IOException ex)
    {
    }
    return result;
  }

  public String createPDF() throws FileNotFoundException, SQLException, IOException
  {
    ServletContext context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
    String pdfDirectory = context.getRealPath("") + File.separator + "pdfs" + File.separator;

    File pdfFile = new File(pdfDirectory);

    if (!(pdfFile.exists()))
      pdfFile.mkdir();

    pdfFile = new File(pdfDirectory + this.editFormType.getFormTypeName() + ".pdf");

    if (!(pdfFile.exists()))
    {
      pdfFile.createNewFile();

      FileOutputStream fos = new FileOutputStream(pdfFile);

      ByteArrayInputStream bais = new ByteArrayInputStream(this.editFormType.getPdfFile());
      byte[] bytes = new byte[bais.available()];
      bais.read(bytes, 0, bais.available());
      fos.write(bytes);

      fos.flush();
      fos.close();
    }

    return "/afs/pdfs/" + this.editFormType.getFormTypeName() + ".pdf";
  }

  public boolean isFolderExport() {
    return this.folderExport;
  }

  public boolean isMailExport()
  {
    return this.mailExport;
  }

  public void exportMethodChanged(ValueChangeEvent e) {
    String value = e.getNewValue().toString();

    if (value.equalsIgnoreCase(ExportMethod.FOLDER_EXPORT)) {
      this.folderExport = true;
      this.mailExport = false;
    }
    else if (value.equalsIgnoreCase(ExportMethod.MAIL_EXPORT)) {
      this.folderExport = false;
      this.mailExport = true;
    }
    else {
      this.folderExport = true;
      this.mailExport = false;
    }
  }

  public void predefinedValueChanged(ValueChangeEvent e)
  {
    String value = e.getNewValue().toString();

    PredefinedThresholdData pre = new PredefinedThresholdData();
    pre.setPredefinedName(value);
    pre = this.thresholdService.getPredefinedThresholdData(pre);
    this.selectedThreshold.setPredefinedValue(pre);
  }

  public void userValueChanged(ValueChangeEvent e)
  {
    String value = e.getNewValue().toString();

    UserData user = new UserData();
    user.setUserName(value);

    user = (UserData)this.userService.getUser(user).get(0);

    this.selectedThreshold.setUser(user);
  }

  public void tabChanged(ValueChangeEvent e) {
    String tab = e.getNewValue().toString();

    if (tab.equals(ResourceHandler.getResource("form_type_update_attributes"))) {
      this.fileName = "";
      this.padFile = null;
      this.xmlFile = null;
      this.xmlFileName = "";
    }

    this.selectedTab = tab;
  }

  public void clearAttributes() {
  }

  public String saveAttributes() {
    String result = "OK";

    if (!(this.formTypeService.addOrUpdateFormType(this.editFormType))) {
      FacesMessages.instance().clear();
      FacesMessages.instance().add(ResourceHandler.getResource("error_when_saving_form_type_attributes"), new Object[0]);
      result = "ERROR";
    }

    return result;
  }

  public void clearUploadPad() {
  }

  public String uploadPad() {
    String result = "OK";

    if ((this.padFile != null) && (this.xmlFile != null) && (this.editFormType.getPod() == 0)) {
      result = this.formTypeService.updatePAD(this.fileName, this.padFile, this.xmlFileName, this.xmlFile, this.editFormType);
      if (result.length() == 0) {
        result = "OK";
      }
      else {
        this.uploadPADErrorMsg = result;
        result = "ERROR";
      }
    }
    else if (this.padFile != null) {
      result = this.formTypeService.updatePAD(this.fileName, this.padFile, this.xmlFileName, this.xmlFile, this.editFormType);
      if (result.length() == 0) {
        result = "OK";
      }
      else {
        this.uploadXMLErrorMsg = result;
        result = "ERROR";
      }
    }
    else {
      this.uploadPADErrorMsg = ResourceHandler.getResource("invalid_pad_file");
      result = "ERROR";
    }

    return result;
  }

  public String clearExport() {
    return "OK";
  }

  public String saveExport() {
    String result = "OK";

    List imageFormats = AshSettingsControl.getInstance().getImageFormats();
    for (Iterator i$ = imageFormats.iterator(); i$.hasNext(); ) { ImageFormat imf = (ImageFormat)i$.next();
      if (imf.getName().equals(this.imageFormat)) {
        this.editFormType.getExportMethod().setImageFormat(imf);
        break;
      }

    }

    List exportFormats = AshSettingsControl.getInstance().getExportFormats();
    for (Iterator i$ = exportFormats.iterator(); i$.hasNext(); ) { ExportFormat emf = (ExportFormat)i$.next();
      if (emf.getName().equals(this.exportFormat)) {
        this.editFormType.getExportMethod().setExportFormat(emf);
        break;
      }
    }

    if (isMailExport()) {
      this.editFormType.getExportMethod().setType(ExportMethod.MAIL_EXPORT);
    }
    else {
      this.editFormType.getExportMethod().setType(ExportMethod.FOLDER_EXPORT);
      this.editFormType.getExportMethod().setMailFrom("");
      this.editFormType.getExportMethod().setMailTo("");
    }

    if (!(this.formTypeService.addOrUpdateFormType(this.editFormType))) {
      FacesMessages.instance().clear();
      FacesMessages.instance().add(ResourceHandler.getResource("error_when_saving_form_type_export"), new Object[0]);
      result = "ERROR";
    }

    this.editFormType = ((FormTypeData)this.formTypeService.getFormTypes(this.editFormType).get(0));

    return result;
  }

  public String saveThresholds() {
    String result = "OK";

    if (!(this.thresholdService.addOrUpdateThresholds(this.thresholds))) {
      result = "ERROR";
    }

    return result;
  }

  public String clearThresholds() {
    return "OK";
  }
}