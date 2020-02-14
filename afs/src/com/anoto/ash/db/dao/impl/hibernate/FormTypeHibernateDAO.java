package com.anoto.ash.db.dao.impl.hibernate;

import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.db.dao.FormTypeDAO;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.portal.AshUploadControl;
import com.anoto.ash.portal.result.FormTypeResult;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("formTypeDao")
@Scope(ScopeType.EVENT)
@SuppressWarnings("unchecked")
public class FormTypeHibernateDAO
  implements FormTypeDAO
{
  public boolean addOrUpdateFormType(FormTypeData formType)
  {
    return AshFormControl.getInstance().updateFormType(formType).isFormTypeOperationSuccessful();
  }

  public List<FormTypeData> getAllFormTypes() {
    return AshFormControl.getInstance().getAllFormTypes();
  }

  public List<FormTypeData> getFormTypes(FormTypeData formType) {
    return AshFormControl.getInstance().getFormType(formType);
  }

  public boolean removeFormType(FormTypeData formType) {
    return AshFormControl.getInstance().deleteFormType(formType).isFormTypeOperationSuccessful();
  }

  public FormTypeData getFormType(Long id) {
    return AshFormControl.getInstance().getFormType(id.intValue());
  }

  public FormTypeResult addNewFormType(String formTypeName, byte[] zipfile) {
    FormTypeResult result = AshUploadControl.getInstance().addFormType(zipfile, formTypeName);

    return result;
  }

  public FormTypeResult updatePAD(byte[] pad, byte[] xml, FormTypeData formType)
  {
    FormTypeResult result = AshUploadControl.getInstance().validateAddedPadFile(pad, formType);

    if (!(result.isPADFileValid())) {
      result = AshUploadControl.getInstance().validateAddedPadFile(pad, formType);

      if (result.isPADFileValid())
        AshUploadControl.getInstance().replacePadFile(formType.getFormTypeName() + ".pad", pad, formType);
    }
    else
    {
      AshUploadControl.getInstance().addPadFile(formType.getFormTypeName() + ".pad", pad, xml, formType);
    }

    return result;
  }
}