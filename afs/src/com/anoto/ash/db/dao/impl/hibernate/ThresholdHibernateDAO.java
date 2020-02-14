package com.anoto.ash.db.dao.impl.hibernate;

import com.anoto.ash.database.PredefinedThresholdData;
import com.anoto.ash.database.ThresholdData;
import com.anoto.ash.db.dao.ThresholdDAO;
import com.anoto.ash.portal.ThresholdControl;
import java.util.List;

public class ThresholdHibernateDAO
  implements ThresholdDAO
{
  public List<PredefinedThresholdData> getPredefinedThresholdDatas()
  {
    return ThresholdControl.getInstance().getAllPredefinedThresholdDatas();
  }

  public List<ThresholdData> getThresholds(ThresholdData threshold) {
    return ThresholdControl.getInstance().getAllThresholdsForFormType(threshold.getFormType());
  }

  public ThresholdData getThreshold(ThresholdData threshold) {
    return ThresholdControl.getInstance().getThresholdData(threshold);
  }

  public List<ThresholdData> getAllThresholds() {
    return ThresholdControl.getInstance().getAllThresholds();
  }

  public boolean addOrUpdatePredefinedThreshold(PredefinedThresholdData predefeinedThreshold) {
    ThresholdControl.getInstance().addOrUpdatePredefinedThreshold(predefeinedThreshold);
    return true;
  }

  public void addOrUpdateThreshold(ThresholdData threshold) {
    ThresholdControl.getInstance().addOrUpdateThreshold(threshold);
  }

  public PredefinedThresholdData getPredefinedThresholdData(PredefinedThresholdData predefined) {
    return ThresholdControl.getInstance().getPredefinedThresholdData(predefined);
  }

  public void deletePredefinedThreshold(PredefinedThresholdData editPredefinedThresholdData) {
    ThresholdControl.getInstance().deletePredefinedThreshold(editPredefinedThresholdData);
  }

  public void deleteThreshold(ThresholdData toDelete) {
    ThresholdControl.getInstance().deleteThreshold(toDelete);
  }
}