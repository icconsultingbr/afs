package com.anoto.ash.services;

import com.anoto.ash.database.ExportFormat;
import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.ImageFormat;
import com.anoto.ash.db.dao.FormTypeDAO;
import com.anoto.ash.db.dao.impl.hibernate.FormTypeHibernateDAO;
import com.anoto.ash.portal.AshSettingsControl;
import com.anoto.ash.portal.result.FormTypeResult;
import com.anoto.ash.utils.ResourceHandler;
import java.util.Iterator;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("formTypeService")
@Scope(ScopeType.EVENT)
@SuppressWarnings("rawtypes")
public class FormTypeService
{
  private FormTypeDAO formTypeDao = new FormTypeHibernateDAO();

  public boolean addOrUpdateFormType(FormTypeData formType)
  {
    return this.formTypeDao.addOrUpdateFormType(formType);
  }

  public String[] getPossibleFileFormats() {
    List exportFormats = AshSettingsControl.getInstance().getExportFormats();

    String[] exportFormatsArray = new String[exportFormats.size()];
    int pos = 0;

    for (Iterator i$ = exportFormats.iterator(); i$.hasNext(); ) { ExportFormat ef = (ExportFormat)i$.next();
      exportFormatsArray[pos] = ef.getName();
      ++pos;
    }

    return exportFormatsArray;
  }

  public boolean removeFormType(FormTypeData formType) {
    return this.formTypeDao.removeFormType(formType);
  }

  public List<FormTypeData> getFormTypes() {
    return this.formTypeDao.getAllFormTypes();
  }

  public List<FormTypeData> getFormTypes(FormTypeData formType) {
    return this.formTypeDao.getFormTypes(formType);
  }

  public FormTypeResult addNewFormType(String formTypeName, byte[] zipfile) {
    return this.formTypeDao.addNewFormType(formTypeName, zipfile);
  }

  public String updatePAD(String padFileName, byte[] pad, String xmlFileName, byte[] xml, FormTypeData formType) {
    String res = "";

    if ((pad == null) || (padFileName == null) || (!(padFileName.endsWith(".pad")))) {
      res = ResourceHandler.getResource("invalid_pad_file");
    }

    if ((xml == null) || (xmlFileName == null) || (!(xmlFileName.endsWith(".xml")))) {
      res = ResourceHandler.getResource("invalid_xml_file");
    }

    FormTypeResult result = this.formTypeDao.updatePAD(pad, xml, formType);

    if (result.isFormTypeOperationSuccessful()) {
      res = "";
    }
    else {
      res = result.getPADFileValidationMessage();
    }

    return res;
  }

  public String[] getPossibleImageFormats() {
    List result = AshSettingsControl.getInstance().getImageFormats();

    String[] imageFormatsArray = new String[result.size()];
    int pos = 0;

    for (Iterator i$ = result.iterator(); i$.hasNext(); ) { ImageFormat imageFormat = (ImageFormat)i$.next();
      imageFormatsArray[pos] = imageFormat.getName();
      ++pos;
    }

    return imageFormatsArray;
  }
}