package com.anoto.ash.db.dao;

import com.anoto.ash.database.FormCopyData;
import java.util.List;

public abstract interface FormCopyDAO
{
  public abstract boolean addOrUpdateFormCopy(FormCopyData paramFormCopyData);

  public abstract List<FormCopyData> getAllFormCopies();

  public abstract Integer getNumberOfFormCopiesFiltered(FormCopyData paramFormCopyData, int paramInt);

  public abstract List<FormCopyData> getFormCopiesFiltered(FormCopyData paramFormCopyData, int paramInt1, String paramString, int paramInt2);

  public abstract List<FormCopyData> getFormCopies(FormCopyData paramFormCopyData);

  public abstract boolean removeFormCopy(FormCopyData paramFormCopyData);

  public abstract FormCopyData getFormCopy(Long paramLong);
}