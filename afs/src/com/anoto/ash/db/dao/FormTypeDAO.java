package com.anoto.ash.db.dao;

import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.portal.result.FormTypeResult;
import java.util.List;

public abstract interface FormTypeDAO
{
  public abstract boolean addOrUpdateFormType(FormTypeData paramFormTypeData);

  public abstract List<FormTypeData> getAllFormTypes();

  public abstract List<FormTypeData> getFormTypes(FormTypeData paramFormTypeData);

  public abstract boolean removeFormType(FormTypeData paramFormTypeData);

  public abstract FormTypeData getFormType(Long paramLong);

  public abstract FormTypeResult addNewFormType(String paramString, byte[] paramArrayOfByte);

  public abstract FormTypeResult updatePAD(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, FormTypeData paramFormTypeData);
}