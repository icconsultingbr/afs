package com.anoto.ash.services;

import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.PredefinedThresholdData;
import com.anoto.ash.database.ThresholdData;
import com.anoto.ash.database.UserData;
import com.anoto.ash.db.dao.ThresholdDAO;
import com.anoto.ash.db.dao.impl.hibernate.ThresholdHibernateDAO;
import com.anoto.ash.utils.ResourceHandler;
import java.util.Iterator;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;


@Name("thresholdService")
@Scope(ScopeType.EVENT)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ThresholdService
{
  private ThresholdDAO thresholdDao = new ThresholdHibernateDAO();

  public boolean addOrUpdatePredefinedThreshold(PredefinedThresholdData editPredefinedThresholdData)
  {
    return this.thresholdDao.addOrUpdatePredefinedThreshold(editPredefinedThresholdData);
  }

  public boolean deletePredefinedThreshold(PredefinedThresholdData editPredefinedThresholdData) {
    List thresholds = this.thresholdDao.getAllThresholds();

    for (Iterator i$ = thresholds.iterator(); i$.hasNext(); ) { ThresholdData threshold = (ThresholdData)i$.next();
      if (threshold.getPredefinedValue().getPredefinedName().equals(editPredefinedThresholdData.getPredefinedName()))
        return false;

    }

    this.thresholdDao.deletePredefinedThreshold(editPredefinedThresholdData);

    return true;
  }

  public void deleteThreshold(ThresholdData toDelete) {
    this.thresholdDao.deleteThreshold(toDelete);
  }

  public List<ThresholdData> getAllThresholds() {
    return this.thresholdDao.getAllThresholds();
  }

  public List<ThresholdData> getThresholds(ThresholdData threshold) {
    return this.thresholdDao.getThresholds(threshold);
  }

  public List<ThresholdData> getThresholds(FormTypeData formType) {
    ThresholdData threshold = new ThresholdData();
    threshold.setFormType(formType);
    threshold.setUser(null);
    threshold.setPredefinedValue(null);

    List thresholds = this.thresholdDao.getThresholds(threshold);

    for (Iterator i$ = thresholds.iterator(); i$.hasNext(); ) { ThresholdData current = (ThresholdData)i$.next();
      if (current.getUser() == null) {
        UserData all = new UserData();
        all.setUserName("All");
        current.setUser(all);
      }
    }

    return thresholds;
  }

  public List<PredefinedThresholdData> getPredefinedThresholdDatas() {
    return this.thresholdDao.getPredefinedThresholdDatas();
  }

  public PredefinedThresholdData getPredefinedThresholdData(PredefinedThresholdData predefined) {
    return this.thresholdDao.getPredefinedThresholdData(predefined);
  }

  public void addOrUpdatePredefinedThreshold(List<PredefinedThresholdData> predefined) {
    for (Iterator i$ = predefined.iterator(); i$.hasNext(); ) { PredefinedThresholdData currentField = (PredefinedThresholdData)i$.next();
      this.thresholdDao.addOrUpdatePredefinedThreshold(currentField);
    }
  }

  public boolean checkForDuplicateThresholds(List<ThresholdData> checkThresholds)
  {
    boolean result = false;
    try
    {
      for (Iterator i$ = checkThresholds.iterator(); i$.hasNext(); ) { ThresholdData current = (ThresholdData)i$.next();
        int nrOfTimes = 0;

        for (Iterator i1 = checkThresholds.iterator(); i1.hasNext(); ) { ThresholdData checkAgainst = (ThresholdData)i1.next();
          if (current.compareTo(checkAgainst) == 0)
            ++nrOfTimes;

        }

        if (nrOfTimes > 1)
          result = true;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return result;
  }

  public boolean addOrUpdateThresholds(List<ThresholdData> thresholds) {
    boolean result = true;
    try {
      result = checkForDuplicateThresholds(thresholds);

      if (!(result)) {
        for (Iterator i$ = thresholds.iterator(); i$.hasNext(); ) { ThresholdData threshold = (ThresholdData)i$.next();
          if ((threshold.getUser() != null) && (threshold.getUser().getUserName().equals("All"))) {
            threshold.setUser(null);
          }
          else if (threshold.getUser() != null) {
            UserService userService = new UserService();
            threshold.setUser((UserData)userService.getUser(threshold.getUser()).get(0));
          }

          List predefineds = getPredefinedThresholdDatas();

          String thresholdName = threshold.getPredefinedValue().getPredefinedName();

          for (Iterator i1 = predefineds.iterator(); i1.hasNext(); ) { PredefinedThresholdData current = (PredefinedThresholdData)i1.next();

            if (current.getPredefinedName().equals(thresholdName))
              threshold.setPredefinedValue(current);

          }

          ThresholdData inDB = this.thresholdDao.getThreshold(threshold);

          threshold.setId(inDB.getId());
          this.thresholdDao.addOrUpdateThreshold(threshold);
        }

      }

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    if (result) {
      FacesMessages.instance().clear();
      FacesMessages.instance().add(ResourceHandler.getResource("duplicate_thresholds"), new Object[0]);
      result = false;
    }
    else {
      result = true;
    }

    return result;
  }
}