package com.anoto.ash.services;

import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.db.dao.FormCopyDAO;
import com.anoto.ash.db.dao.impl.hibernate.FormCopyHibernateDAO;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.portal.result.FormCopyResult;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;


@Name("formCopyService")
@SuppressWarnings({"unchecked", "rawtypes"})
public class FormCopyService
{
  private FormCopyDAO formCopyDao = new FormCopyHibernateDAO();

  public boolean changeToComplete(FormCopyData formCopy)
  {
    boolean result = false;
    formCopy.setMarkedCompleted(1);
    FormCopyResult myResult = AshFormControl.getInstance().forceCompleteFormCopy(formCopy, false);
    if (myResult.isFormCopyOperationSuccessful()) {
      result = true;
    }
    else {
      result = false;
    }

    return result;
  }

  public List<FormCopyData> getFormCopies() {
    return this.formCopyDao.getAllFormCopies();
  }

  public List<FormCopyData> getFormCopiesFiltered(FormCopyData formCopy, int startPos, String orderProperty, int order) {
    return this.formCopyDao.getFormCopiesFiltered(formCopy, startPos, orderProperty, order);
  }

  public List<FormCopyData> getFormCopies(FormCopyData formCopy) {
    return this.formCopyDao.getFormCopies(formCopy);
  }

  public List<FormCopyData> getInboxFormCopies(FormCopyData fc, int startPos) {
    List result = new ArrayList();
    fc.setVerificationNeeded(1);
    result.addAll(getFormCopiesFiltered(fc, startPos, "", 3));
    return result;
  }

  public Integer countNumberOfCompletedFormCopies(FormCopyData fc, int startPos) {
    return this.formCopyDao.getNumberOfFormCopiesFiltered(fc, startPos);
  }

  public List<FormCopyData> getCompletedCopies(FormCopyData fc, int startPos, String orderProperty, int order) {
    return getFormCopiesFiltered(fc, startPos, orderProperty, order);
  }

  public boolean overrideMandatoryFields(FormCopyData formCopy) {
    boolean result = false;

    formCopy.setMarkedCompleted(0);
    formCopy.setVerificationNeeded(1);
    formCopy.setMandatoryFieldsMissing(-1);

    FormCopyResult myResult = AshFormControl.getInstance().forceCompleteFormCopy(formCopy, true);

    if (myResult.isFormCopyOperationSuccessful()) {
      result = true;
    }
    else {
      result = false;
    }

    return result;
  }

  public boolean changeToIncomplete(FormCopyData formCopy)
  {
    boolean result = false;

    formCopy.setMarkedCompleted(0);
    formCopy.setVerificationNeeded(1);
    formCopy.setMandatoryFieldsMissing(-1);
    FormCopyResult myResult = AshFormControl.getInstance().updateFormCopy(formCopy);

    if (myResult.isFormCopyOperationSuccessful()) {
      result = true;
    }
    else {
      result = false;
    }

    return result;
  }

  public boolean unlockForm(FormCopyData formCopy) {
    boolean result = false;
    formCopy.setLocked(0);

    formCopy.setMarkedCompleted(0);
    formCopy.setVerificationNeeded(1);
    formCopy.setMandatoryFieldsMissing(-1);

    FormCopyResult myResult = AshFormControl.getInstance().updateFormCopy(formCopy);

    if (myResult.isFormCopyOperationSuccessful()) {
      result = true;
    }
    else {
      result = false;
    }

    return result;
  }
}