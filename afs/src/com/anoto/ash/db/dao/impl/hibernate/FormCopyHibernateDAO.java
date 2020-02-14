package com.anoto.ash.db.dao.impl.hibernate;

import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.db.dao.FormCopyDAO;
import com.anoto.ash.portal.AshFormControl;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("formCopyDao")
@Scope(ScopeType.EVENT)
@SuppressWarnings({"unchecked", "rawtypes"})
public class FormCopyHibernateDAO implements FormCopyDAO {
  public boolean addOrUpdateFormCopy(FormCopyData formCopy) {
    return AshFormControl.getInstance().updateFormCopy(formCopy).isFormCopyOperationSuccessful();
  }

  public List getAllFormCopies() {
    return AshFormControl.getInstance().getAllFormCopiesFiltered();
  }

  public List getFormCopiesFiltered(FormCopyData formCopy, int startPos, String orderProperty, int order) {
    return AshFormControl.getInstance().getFormCopyFiltered(formCopy, startPos, orderProperty, order);
  }

  public List getFormCopies(FormCopyData formCopy) {
    return AshFormControl.getInstance().getFormCopy(formCopy);
  }

  public boolean removeFormCopy(FormCopyData formCopy) {
    return false;
  }

  public FormCopyData getFormCopy(Long id) {
    return AshFormControl.getInstance().getFormCopy(id.intValue());
  }

  public Integer getNumberOfFormCopiesFiltered(FormCopyData formCopy, int startPos) {
    return AshFormControl.getInstance().getNumberOfFormCopyFiltered(formCopy, startPos);
  }
}