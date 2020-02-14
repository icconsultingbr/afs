package com.anoto.ash.db.dao;

import com.anoto.ash.database.PredefinedThresholdData;
import com.anoto.ash.database.ThresholdData;
import java.util.List;

public abstract interface ThresholdDAO
{
  public abstract void deletePredefinedThreshold(PredefinedThresholdData paramPredefinedThresholdData);

  public abstract void deleteThreshold(ThresholdData paramThresholdData);

  public abstract PredefinedThresholdData getPredefinedThresholdData(PredefinedThresholdData paramPredefinedThresholdData);

  public abstract List<PredefinedThresholdData> getPredefinedThresholdDatas();

  public abstract boolean addOrUpdatePredefinedThreshold(PredefinedThresholdData paramPredefinedThresholdData);

  public abstract void addOrUpdateThreshold(ThresholdData paramThresholdData);

  public abstract List<ThresholdData> getAllThresholds();

  public abstract List<ThresholdData> getThresholds(ThresholdData paramThresholdData);

  public abstract ThresholdData getThreshold(ThresholdData paramThresholdData);
}