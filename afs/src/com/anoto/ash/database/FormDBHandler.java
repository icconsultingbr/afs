package com.anoto.ash.database;

import com.anoto.api.core.PadFileUtility;
import com.anoto.ash.AshCommons;
import com.anoto.ash.AshLogger;
import com.anoto.ash.AshProperties;
import com.anoto.ash.Sorter;

import java.math.BigInteger;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

public class FormDBHandler extends DBHandler
{
  public static final int ASC = 1;
  public static final int DESC = 2;
  public static final int NONE = 3;

  public static boolean deleteBackground(BackgroundFile currentBackground)
  {
    boolean result = false;
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);
    try
    {
      currentBackground.setFormType(null);
      session.delete(currentBackground);
    } catch (Exception e) {
      AshLogger.logSevere("Error when deleting background file: " + e.getMessage());
      result = false;
    }

    commitTransaction(session);
    closeSession();

    result = true;

    return result;
  }

  public static synchronized int deleteFormCopy(FormCopyData currentFormCopy)
  {
    int result = 0;

    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);
    try
    {
      currentFormCopy.setFormType(null);
      currentFormCopy.setOwner(null);
      session.delete(currentFormCopy);
      result = 0;
    } catch (Exception e) {
      AshLogger.logSevere("Error when deleting FormCopy: " + e.getMessage());
      result = -1;
    }

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized int deleteFormType(FormTypeData formTypeData) throws HibernateException
  {
    int result = 0;

    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);
    try
    {
      session.delete(formTypeData);
      result = 0;
    } catch (Exception e) {
      AshLogger.logSevere("Error when deleting FormType: " + e.getMessage());
      result = -1;
    }

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized int addFormType(FormTypeData formTypeData) throws HibernateException
  {
    int receivedFormTypeId = -1;
    AshLogger.logFine("About to add a new FormType: " + formTypeData.toString());

    if (formTypeNamePreviouslyUsed(formTypeData.getFormTypeName())) {
      AshLogger.logFine("User tried to use an existing Form Type Name when uploading a new one, form type name: " + formTypeData.getFormTypeName());
      return -10;
    }

    openSession();
    Session session = getCurrentSession();

    beginTransaction(session);

    Integer Id = (Integer)session.save(formTypeData);
    try
    {
      receivedFormTypeId = Id.intValue();
    } catch (Exception e) {
      AshLogger.logSevere("Error when adding FormType: " + e.getMessage());
      receivedFormTypeId = -1;
    }

    commitTransaction(session);

    closeSession();

    return receivedFormTypeId;
  }

  public static synchronized int addPadFile(PadFile padFile)
    throws HibernateException
  {
    AshLogger.logFine("About to add a new padFile, address: " + padFile.getPadLicenseAddress());

    if (padPreviouslySubmitted(padFile.getPadLicenseAddress())) {
      AshLogger.logFine("Tried to add an already existing pad file. Address: " + padFile.getPadLicenseAddress());
      return -12;
    }

    openSession();
    Session session = getCurrentSession();

    beginTransaction(session);

    session.saveOrUpdate(padFile);

    commitTransaction(session);

    closeSession();

    return padFile.getId();
  }

  public static synchronized int addBackgroundFile(BackgroundFile backgroundFile)
    throws HibernateException
  {
    AshLogger.logFine("About to add a new background file: " + backgroundFile.getFileName());
    int receivedId = -1;

    openSession();
    Session session = getCurrentSession();

    beginTransaction(session);

    Integer Id = (Integer)session.save(backgroundFile);
    try
    {
      receivedId = Id.intValue();
    } catch (Exception e) {
      AshLogger.logSevere("Error when adding background file:" + e.getMessage());
      receivedId = -1;
    }

    commitTransaction(session);

    closeSession();

    return receivedId;
  }

  public static synchronized int addFormCopy(FormCopyData formCopyData)
    throws HibernateException
  {
    int receivedFormCopyId = -1;

    if (formCopyData.getFormType() == null) {
      AshLogger.logFine("No form type for the FormCopy:" + formCopyData.toString());
      return -11;
    }

    FormTypeData storedFormType = getFormType(formCopyData.getFormType().getFormTypeId());

    if ((formCopyData.getFormType().getRemainingFormCopies() != -1) && (storedFormType.getRemainingFormCopies() != formCopyData.getFormType().getRemainingFormCopies()))
    {
      storedFormType.setRemainingFormCopies(storedFormType.getRemainingFormCopies() - 1);
    }
    
    formCopyData.setFormType(storedFormType);

    openSession();
    Session session = getCurrentSession();

    beginTransaction(session);

    Integer Id = (Integer)session.save(formCopyData);
    try
    {
      AshLogger.logFine("Adding a new form copy: " + formCopyData.toString());

      receivedFormCopyId = Id.intValue();
    } catch (Exception e) {
      AshLogger.logSevere(e.getMessage());
      receivedFormCopyId = -1;
    }

    commitTransaction(session);

    closeSession();

    return receivedFormCopyId;
  }
  

  public static boolean deletePad(PadFile pad)
  {
    boolean result = false;
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);
    try
    {
      pad.setFormType(null);
      session.delete(pad);
    } catch (Exception e) {
      AshLogger.logSevere("Error when deleting FormType: " + e.getMessage());
      result = false;
    }

    commitTransaction(session);
    closeSession();

    result = true;

    return result;
  }

  public static synchronized ArrayList<?> getAllFormTypes()
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();

    AshLogger.logFine("Getting all form types from database.");

    beginTransaction(session);
    List<?> result = session.createQuery("from FormTypeData order by formTypeName").list();

    commitTransaction(session);

    closeSession();

    return new ArrayList<Object>(result);
  }

  public static synchronized ArrayList<?> getAllFormCopies(boolean filter, boolean filterOnDisplayPeriod, String orderProperty, int order)
    throws HibernateException
  {
    FormCopyData formCopyData = new FormCopyData();
    List<?> result = null;

    if (filter)
      result = searchFormCopyFiltered(formCopyData, filterOnDisplayPeriod, -1, orderProperty, order);
    else {
      result = searchFormCopy(formCopyData, -1, "", 3);
    }

    return new ArrayList<Object>(result);
  }

  public static synchronized ArrayList<?> getAllPadFiles()
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();
    AshLogger.logFine("Getting all pads from database.");
    beginTransaction(session);
    List<?> result = session.createQuery("from PadFile").list();

    commitTransaction(session);

    closeSession();

    return new ArrayList<Object>(result);
  }

  public static synchronized FormTypeData getFormType(int formTypeId)
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting form type from database with id = " + formTypeId);

    FormTypeData formTypeData = (FormTypeData)session.get(FormTypeData.class, new Integer(formTypeId));

    commitTransaction(session);

    closeSession();

    return formTypeData;
  }

  public static synchronized FormCopyData getFormCopy(int formCopyId)
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();

    beginTransaction(session);
    AshLogger.logFine("Getting form copy from database with id = " + formCopyId);
    FormCopyData formCopyData = (FormCopyData)session.get(FormCopyData.class, new Integer(formCopyId));

    commitTransaction(session);
    closeSession();

    return formCopyData;
  }

  public static synchronized PadFile getPadFile(int id)
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();

    beginTransaction(session);
    AshLogger.logFine("Getting pad file from DB with id = " + id);
    PadFile padFile = (PadFile)session.get(PadFile.class, new Integer(id));

    commitTransaction(session);
    closeSession();

    return padFile;
  }

  public static synchronized int updateDynamicDataDefinition(DynamicDataDefinition dynamicDataDef)
    throws HibernateException
  {
    openSession();

    Session session = getCurrentSession();

    beginTransaction(session);

    AshLogger.logFine("Updating dynamic data definition");
    session.saveOrUpdate(dynamicDataDef);

    commitTransaction(session);

    closeSession();
    return 0;
  }

  public static synchronized DynamicDataDefinition getDynamicDataDefinition(int id)
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();

    beginTransaction(session);
    AshLogger.logFine("Getting Dynamic data definition from DB with id = " + id);
    DynamicDataDefinition definition = (DynamicDataDefinition)session.get(DynamicDataDefinition.class, new Integer(id));

    commitTransaction(session);
    closeSession();

    return definition;
  }

  public static synchronized int getDynamicDataDefinitionId(String fieldName, FormTypeData formTypeData)
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();

    beginTransaction(session);
    AshLogger.logFine("Getting dynamic data definition id for field " + fieldName + " of formtype " + formTypeData.getFormTypeName());

    List<?> result = session.createSQLQuery("Select * from dynamic_data_definitions where field_name='" + fieldName + "' and fk_form_type_id is null").list();

    if (result.size() == 0)
    {
      result = session.createSQLQuery("Select * from dynamic_data_definitions where field_name='" + fieldName + "' and fk_form_type_id=" + formTypeData.getFormTypeId()).list();
    }

    commitTransaction(session);
    closeSession();

    int id = -1;
    if (result.size() > 0) {
      Object[] objects = (Object[])(Object[])result.get(0);

      Integer receivedId = (Integer)objects[0];

      id = receivedId.intValue();
    }

    return id;
  }

  public static synchronized List<?> searchFormCopy(FormCopyData formCopyData, int startPos, String orderProperty, int order)
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting form copy from database by criteria");

    Criteria newCrit = null;
    List<?> result = new ArrayList<Object>();

    if (startPos < 0) {
      newCrit = SearchFormCopyCriteriaFactory.createCriteria(session, formCopyData, null, null);
    }
    else {
      newCrit = SearchFormCopyCriteriaFactory.createLikeQuery(session, formCopyData, null, null, orderProperty, order);
      newCrit.addOrder(Order.desc("latestSubmit"));
      newCrit.setFirstResult(startPos);
      newCrit.setMaxResults(10);
    }

    if (newCrit != null) {
      result = newCrit.list();
    }

    commitTransaction(session);
    closeSession();

    return result;
  }
  
  public static synchronized Integer searchNumberOfFormCopy(FormCopyData formCopyData, int startPos)
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    Integer result = Integer.valueOf(0);

    Criteria newCrit = null;

    newCrit = SearchFormCopyCriteriaFactory.createLikeQuery(session, formCopyData, null, null, "", 3);
    if (newCrit != null) {
      result = (Integer)newCrit.setProjection(Projections.rowCount()).uniqueResult();
    }

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized List<?> searchFormCopyFiltered(FormCopyData formCopyData, boolean filterOnDisplayPeriod, int startPage, String orderProperty, int order)
  {
    boolean removeMultiplePenModeFormTypes = false;

    if ((formCopyData.getOwner() != null) && (formCopyData.getOwner().getId() > 0)) {
      removeMultiplePenModeFormTypes = true;
    }

    List<?> formCopyList = searchFormCopy(formCopyData, startPage, orderProperty, order);

    if ((formCopyList != null) && (formCopyList.size() > 0))
    {
      filterFormCopyList(formCopyList, removeMultiplePenModeFormTypes, filterOnDisplayPeriod);
    }

    return formCopyList;
  }

  public static synchronized Integer searchNumberOfFormCopyFiltered(FormCopyData formCopyData, boolean filterOnDisplayPeriod, int startPage)
  {
    return searchNumberOfFormCopy(formCopyData, startPage);
  }

  public static synchronized List<?> searchFormType(FormTypeData formTypeData)
  {
    List<?> result = new ArrayList<FormTypeData>();

    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting form type from database by criteria: " + formTypeData.toString());
    Criteria crit = SearchFormTypeCriteriaFactory.createCriteria(session, formTypeData);

    result = crit.list();

    commitTransaction(session);
    closeSession();

    return result;
  }

  private static void filterFormCopyList(List<?> formCopyList, boolean removeMultiplePenModeFormTypes, boolean filterOnDisplayPeriod)
  {
    List<Object> tempFormCopyList = new ArrayList<Object>();
    tempFormCopyList.addAll(formCopyList);
    for (Iterator<?> iter = tempFormCopyList.iterator(); iter.hasNext(); ) { 
      FormCopyData currentFormCopy = (FormCopyData)iter.next();

      if (currentFormCopy.getFormStatus() == 4)
      {
        formCopyList.remove(currentFormCopy);
      }
      else if ((removeMultiplePenModeFormTypes) && (((currentFormCopy.getFormType().getMultiplePenMode() == 1) || (AshProperties.getProperty("multiplePenMode").equalsIgnoreCase("true")))))
      {
        formCopyList.remove(currentFormCopy);
      }
      else if (filterOnDisplayPeriod)
      {
        long displayPeriod = currentFormCopy.getFormType().getDisplayPeriod();
        long displayPeriodInMilliSeconds = displayPeriod * 24L * 3600L * 1000L;

        Date fromDate = new Date(System.currentTimeMillis() - displayPeriodInMilliSeconds);
        Date latestSubmit = currentFormCopy.getLatestSubmit();

        if (latestSubmit.before(fromDate))
          formCopyList.remove(currentFormCopy);
      }
    }
  }

  private static synchronized List<?> getFormType(String formTypeName)
    throws HibernateException
  {
    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting form type from database by name: " + formTypeName);
    Query query = session.createQuery("from FormTypeData where formTypeName like:formTypeName");

    query.setString("formTypeName", formTypeName);
    List<?> result = query.list();

    commitTransaction(session);

    closeSession();

    return result;
  }

  public static synchronized List<?> searchPadFile(PadFile padFile)
  {
    List<?> result = new ArrayList<Object>();

    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting pad files from database by criteria");
    Criteria crit = SearchPadFileCriteriaFactory.createCriteria(session, padFile);

    result = crit.list();

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized List<?> searchFont(Font font) {
    List<?> result = new ArrayList<Object>();

    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting font from database by criteria");
    Criteria crit = SearchFontCriteriaFactory.createCriteria(session, font);

    result = crit.list();

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized List<?> searchBackgroundFile(BackgroundFile backgroundFile) {
    List<?> result = new ArrayList<Object>();

    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting pad files from database by criteria");
    Criteria crit = SearchBackgroundFileCriteriaFactory.createCriteria(session, backgroundFile);

    result = crit.list();

    commitTransaction(session);
    closeSession();

    return result;
  }

  public static synchronized int updateFormType(FormTypeData formTypeData)
    throws HibernateException
  {
    if (getFormType(formTypeData.getFormTypeName()).size() <= 0) {
      AshLogger.logFine("No form type with that name : " + formTypeData.getFormTypeName());
      return -11;
    }

    openSession();

    Session session = getCurrentSession();

    beginTransaction(session);

    AshLogger.logFine("Updating the form type: " + formTypeData.toString());
    session.saveOrUpdate(formTypeData);

    commitTransaction(session);

    closeSession();
    return 0;
  }

  public static synchronized int updateFormCopy(FormCopyData formCopyData)
    throws HibernateException
  {
    FormCopyData storedFormCopy = getFormCopy(formCopyData.getFormCopyId());
    if (storedFormCopy == null) {
      AshLogger.logFine("No form copy with that id: " + formCopyData.getFormCopyId());
      return -14;
    }

    FormTypeData storedFormType = getFormType(storedFormCopy.getFormType().getFormTypeId());

    if ((formCopyData.getFormType().getRemainingFormCopies() != -1) && (storedFormType.getRemainingFormCopies() != formCopyData.getFormType().getRemainingFormCopies()))
    {
      storedFormType.setRemainingFormCopies(storedFormType.getRemainingFormCopies() - 1);
    }

    formCopyData.setFormType(storedFormType);

    openSession();

    Session session = getCurrentSession();

    beginTransaction(session);

    AshLogger.logFine("Updating the form copy: " + formCopyData.toString());
    session.saveOrUpdate(formCopyData);

    commitTransaction(session);

    closeSession();
    return 0;
  }

  public static synchronized int updatePadFile(PadFile padFile)
    throws HibernateException
  {
    if (getPadFile(padFile.getId()) == null) {
      AshLogger.logFine("No pad file with id: " + padFile.getId());
      return -15;
    }

    openSession();

    Session session = getCurrentSession();

    beginTransaction(session);

    AshLogger.logFine("Updating pad with license address: " + padFile.getPadLicenseAddress());
    session.saveOrUpdate(padFile);

    commitTransaction(session);

    closeSession();
    return 0;
  }

  public static synchronized boolean padPreviouslySubmitted(String padLicenseAddress)
  {
    PadFile padFile = new PadFile();
    padFile.setPadLicenseAddress(padLicenseAddress);

    return (searchPadFile(padFile).size() > 0);
  }

  public static synchronized boolean formTypeNamePreviouslyUsed(String formTypeName)
    throws HibernateException
  {
    return (getFormType(formTypeName).size() > 0);
  }

  public static class SearchPadFileCriteriaFactory
  {
    public static Criteria createCriteria(Session session, PadFile padFile) {
		if ((session != null) && (padFile != null)) {
			Criteria criteria = session.createCriteria(PadFile.class);
			AshLogger.logFine("Searching for a pad file: ");
			if (padFile.getFormType() != null) {
				//criteria.add(Expression.like("formType", padFile.getFormType()));
				criteria.add(Expression.eq("formType", padFile.getFormType()));
			}
			if ((padFile.getPadLicenseAddress() != null) && (padFile.getPadLicenseAddress().length() > 0)) {
				criteria.add(Expression.like("padLicenseAddress", padFile.getPadLicenseAddress()));
			}
			//criteria.addOrder(Order.desc("lastModified"));
			criteria.addOrder(Order.asc("id"));
			criteria.setMaxResults(100);
			return criteria;
		}
		return null;
	}
  }

  public static class SearchFontCriteriaFactory
  {
    public static Criteria createCriteria(Session session, Font font)
    {
      if ((session != null) && (font != null)) {
        Criteria criteria = session.createCriteria(Font.class);
        AshLogger.logFine("Searching for a font! ");

        if ((font.getName() != null) && (font.getName().length() > 0)) {
          criteria.add(Expression.like("name", font.getName()));
        }

        return criteria;
      }

      return null;
    }
  }

  public static class SearchBackgroundFileCriteriaFactory
  {
    public static Criteria createCriteria(Session session, BackgroundFile backgroundFile)
    {
      if ((session != null) && (backgroundFile != null)) {
        Criteria criteria = session.createCriteria(BackgroundFile.class);
        AshLogger.logFine("Searching for a background file: ");

        if (backgroundFile.getFormType() != null) {
          //criteria.add(Expression.like("formType", backgroundFile.getFormType()));
          criteria.add(Expression.eq("formType", backgroundFile.getFormType()));
        }

        if (backgroundFile.getFileName().length() > 0) {
          criteria.add(Expression.like("fileName", backgroundFile.getFileName()));
        }

        return criteria;
      }

      return null;
    }
  }

  public static class SearchFormTypeCriteriaFactory
  {
    public static Criteria createCriteria(Session session, FormTypeData formTypeData)
    {
      if ((session != null) && (formTypeData != null)) {
        Criteria crit = session.createCriteria(FormTypeData.class);
        AshLogger.logFine("Searching for a form type, like : " + formTypeData.toString());

        if (formTypeData.getFormTypeName() != null) {
          crit.add(Expression.like("formTypeName", formTypeData.getFormTypeName()));
        }

        return crit;
      }

      return null;
    }
  }

  public static class SearchFormCopyCriteriaFactory
  {
    public static synchronized Criteria createLikeQuery(Session session, FormCopyData formCopyData, Date from, Date to, String propertyName, int order) {
      if ((session != null) && (formCopyData != null)) {
        Criteria crit = session.createCriteria(FormCopyData.class);

        if ((formCopyData.getFormType() != null) && (formCopyData.getFormType().getFormTypeName() != null)) {
          List<?> formTypes = session.createCriteria(FormTypeData.class).add(Expression.like("formTypeName", formCopyData.getFormType().getFormTypeName())).list();

          if ((formTypes != null) && (formTypes.size() > 0)) {
            crit.add(Expression.in("formType", formTypes));
          }
          else {
            crit.add(Expression.isNull("formType"));
          }

        }

        if (formCopyData.getOwner() != null) {
          Criteria userCriteria = session.createCriteria(UserData.class);

          if ((formCopyData.getOwner().getUserName() != null) && (formCopyData.getOwner().getUserName().length() > 0)) {
            userCriteria.add(Expression.like("userName", formCopyData.getOwner().getUserName()));
          }

          if ((formCopyData.getOwner().getPenId() != null) && (formCopyData.getOwner().getPenId().length() > 0)) {
            userCriteria.add(Expression.like("penId", formCopyData.getOwner().getPenId()));
          }

          List<?> users = userCriteria.list();

          if ((users != null) && (users.size() > 0)) {
            crit.add(Expression.in("owner", users));
          }
          else {
            crit.add(Expression.isNull("owner"));
          }

        }

        if (formCopyData.getLatestSubmit() != null) {
          Calendar c;
          int lastDayInMonth;
          String toDate = null;

          Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

          String fromDate = formatter.format(formCopyData.getLatestSubmit());

          String yearStr = fromDate.substring(0, 4);
          String monthStr = fromDate.substring(5, 7);
          String dayStr = fromDate.substring(8, 10);
          String hourStr = fromDate.substring(11, 13);
          String minutesStr = fromDate.substring(14, 16);

          if (fromDate.endsWith("-01-01 00:00")) {
            c = Calendar.getInstance();
            c.set(Integer.valueOf(yearStr).intValue(), Integer.valueOf("11").intValue(), 1);

            lastDayInMonth = c.getActualMaximum(5);

            toDate = yearStr + "-12-" + lastDayInMonth + " 23:59";
          }
          else if (fromDate.endsWith("-01 00:00")) {
            c = Calendar.getInstance();
            c.set(Integer.valueOf(yearStr).intValue(), Integer.valueOf(monthStr).intValue() - 1, 1);

            lastDayInMonth = c.getActualMaximum(5);

            toDate = yearStr + "-" + monthStr + "-" + lastDayInMonth + " 23:59";
          }
          else if (fromDate.endsWith(" 00:00"))
          {
            toDate = yearStr + "-" + monthStr + "-" + dayStr + " 23:59";
          }
          else if (fromDate.endsWith(":00")) {
            toDate = yearStr + "-" + monthStr + "-" + dayStr + " " + hourStr + ":59";
          }

          yearStr = toDate.substring(0, 4);
          monthStr = toDate.substring(5, 7);
          dayStr = toDate.substring(8, 10);
          hourStr = toDate.substring(11, 13);
          minutesStr = toDate.substring(14, 16);

          Calendar myCal = Calendar.getInstance();

          myCal.set(Integer.valueOf(yearStr).intValue(), Integer.valueOf(monthStr).intValue() - 1, Integer.valueOf(dayStr).intValue(), Integer.valueOf(hourStr).intValue(), Integer.valueOf(minutesStr).intValue());

          Date toD = myCal.getTime();

          crit.add(Expression.between("latestSubmit", formCopyData.getLatestSubmit(), toD));
        }

        if (formCopyData.getVerificationNeeded() >= 0) {
          crit = crit.add(Property.forName("verificationNeeded").eq(Integer.valueOf(formCopyData.getVerificationNeeded())));
        }

        if (formCopyData.getMandatoryFieldsMissing() >= -1) {
          crit = crit.add(Property.forName("mandatoryFieldsMissing").eq(Integer.valueOf(formCopyData.getMandatoryFieldsMissing())));
        }

        if (formCopyData.getMarkedCompleted() >= 0) {
          crit = crit.add(Property.forName("markedCompleted").eq(Integer.valueOf(formCopyData.getMarkedCompleted())));
        }

        if (formCopyData.getImageMailFailed() >= 0) {
          crit = crit.add(Property.forName("imageMailFailed").eq(Integer.valueOf(formCopyData.getImageMailFailed())));
        }

        if (formCopyData.getExportMailFailed() >= 0) {
          crit = crit.add(Property.forName("exportMailFailed").eq(Integer.valueOf(formCopyData.getExportMailFailed())));
        }

        if ((formCopyData.getEndPageAddress() != null) && (formCopyData.getEndPageAddress().length() > 0)) {
          crit = crit.add(Property.forName("endPageAddress").like(formCopyData.getEndPageAddress()));
        }

        return crit;
      }

      return null;
    }

    public static synchronized Criteria createNumberOfLikeCriteria(Session session, FormCopyData formCopyData, Date from, Date to)
    {
      if ((session != null) && (formCopyData != null)) {
        Criteria crit = session.createCriteria(FormCopyData.class);

        if (formCopyData.getFormType() != null) {
          crit.createCriteria("formType").add(Expression.like("formTypeName", formCopyData.getFormType().getFormTypeName()));
        }

        if (formCopyData.getOwner() != null)
        {
          String userName = formCopyData.getOwner().getUserName();
          String penId = formCopyData.getOwner().getPenId();

          if ((userName.length() > 0) && (penId.length() == 0))
          {
            crit.createCriteria("owner").add(Expression.like("userName", formCopyData.getOwner().getUserName()));
          }
          else if ((userName.length() == 0) && (penId.length() > 0))
          {
            crit.createCriteria("owner").add(Expression.like("penId", formCopyData.getOwner().getPenId()));
          }
          else
          {
            crit.createCriteria("owner").add(Expression.like("userName", formCopyData.getOwner().getUserName())).add(Expression.like("penId", formCopyData.getOwner().getPenId()));
          }

        }

        if (formCopyData.getLatestSubmit() != null) {
          crit.add(Expression.like("latestSubmit", formCopyData.getLatestSubmit()));
        }

        if (formCopyData.getVerificationNeeded() >= 0) {
          crit.add(Restrictions.eq("verificationNeeded", Integer.valueOf(formCopyData.getVerificationNeeded())));
        }

        if (formCopyData.getMandatoryFieldsMissing() >= -1) {
          crit.add(Restrictions.eq("mandatoryFieldsMissing", Integer.valueOf(formCopyData.getMandatoryFieldsMissing())));
        }

        if (formCopyData.getMarkedCompleted() >= 0) {
          crit.add(Restrictions.eq("markedCompleted", Integer.valueOf(formCopyData.getMarkedCompleted())));
          AshLogger.logFine("Marked Completed = " + formCopyData.getMarkedCompleted());
        }

        if (formCopyData.getImageMailFailed() >= 0) {
          crit.add(Restrictions.eq("imageMailFailed", Integer.valueOf(formCopyData.getImageMailFailed())));
          AshLogger.logFine("Image mail failed = " + formCopyData.getImageMailFailed());
        }

        if (formCopyData.getExportMailFailed() >= 0) {
          crit.add(Restrictions.eq("exportMailFailed", Integer.valueOf(formCopyData.getExportMailFailed())));
          AshLogger.logFine("Export mail failed = " + formCopyData.getExportMailFailed());
        }

        if ((formCopyData.getEndPageAddress() != null) && (formCopyData.getEndPageAddress().length() > 0))
        {
          if (!(formCopyData.getEndPageAddress().startsWith("0."))) {
            formCopyData.setEndPageAddress("0." + formCopyData.getEndPageAddress());
          }

          String endPageAddress = formCopyData.getEndPageAddress();
          crit.add(Expression.like("endPageAddress", endPageAddress));
        }

        if (from != null) {
          crit.add(Restrictions.ge("latestSubmit", from));
          AshLogger.logFine("From Date = " + from);
        }

        if (to != null) {
          crit.add(Restrictions.le("latestSubmit", to));
          AshLogger.logFine("To Date = " + to);
        }

        return crit;
      }

      return null;
    }

    public static synchronized Criteria createCriteria(Session session, FormCopyData formCopyData, Date from, Date to)
    {
      if ((session != null) && (formCopyData != null)) {
        Criteria crit = session.createCriteria(FormCopyData.class);

        AshLogger.logFine("Searching for a form copy with the following criteria:");

        if (formCopyData.getFormType() != null) {
          crit.add(Restrictions.eq("formType", formCopyData.getFormType()));
          AshLogger.logFine("Form type = " + formCopyData.getFormType().toString());
        }

        if (formCopyData.getOwner() != null) {
          crit.add(Restrictions.eq("owner", formCopyData.getOwner()));
          AshLogger.logFine("Owner = " + formCopyData.getOwner().toString());
        }

        if (formCopyData.getVerificationNeeded() >= 0) {
          crit.add(Restrictions.eq("verificationNeeded", Integer.valueOf(formCopyData.getVerificationNeeded())));
          AshLogger.logFine("Verification Needed = " + formCopyData.getVerificationNeeded());
        }

        if (formCopyData.getMandatoryFieldsMissing() >= -1) {
          crit.add(Restrictions.eq("mandatoryFieldsMissing", Integer.valueOf(formCopyData.getMandatoryFieldsMissing())));
          AshLogger.logFine("Mandatory Field Missing = " + formCopyData.getMandatoryFieldsMissing());
        }

        if (formCopyData.getMarkedCompleted() >= 0) {
          crit.add(Restrictions.eq("markedCompleted", Integer.valueOf(formCopyData.getMarkedCompleted())));
          AshLogger.logFine("Marked Completed = " + formCopyData.getMarkedCompleted());
        }
        if (formCopyData.getImageMailFailed() >= 0) {
          crit.add(Restrictions.eq("imageMailFailed", Integer.valueOf(formCopyData.getImageMailFailed())));
          AshLogger.logFine("Image mail failed = " + formCopyData.getImageMailFailed());
        }
        if (formCopyData.getExportMailFailed() >= 0) {
          crit.add(Restrictions.eq("exportMailFailed", Integer.valueOf(formCopyData.getExportMailFailed())));
          AshLogger.logFine("Export mail failed = " + formCopyData.getExportMailFailed());
        }

        if ((formCopyData.getEndPageAddress() != null) && (formCopyData.getEndPageAddress().length() > 0))
        {
          if (!(formCopyData.getEndPageAddress().startsWith("0."))) {
            formCopyData.setEndPageAddress("0." + formCopyData.getEndPageAddress());
          }

          if (formCopyData.getEndPageAddress().contains("*")) {
            String endPageAddress = formCopyData.getEndPageAddress();
            endPageAddress = endPageAddress.substring(0, endPageAddress.indexOf("*"));
            crit.add(Restrictions.like("endPageAddress", endPageAddress));

            AshLogger.logFine("End Page Address = " + endPageAddress);
          } else {
            try {
              String[] pages = getFormPages(formCopyData.getEndPageAddress());

              crit.add(Restrictions.eq("endPageAddress", pages[(pages.length - 1)]));
            } catch (Exception e) {
              AshLogger.logSevere("Error when finding PAD for the following page address: " + formCopyData.getEndPageAddress() + ", error message: " + e.getMessage());
              return null;
            }
          }
        }

        if (from != null) {
          crit.add(Restrictions.ge("latestSubmit", from));
          AshLogger.logFine("From Date = " + from);
        }

        if (to != null) {
          crit.add(Restrictions.le("latestSubmit", to));
          AshLogger.logFine("To Date = " + to);
        }
        
        return crit;
      }

      return null;
    }

    private static synchronized String[] getFormPages(String submittedPage)
      throws Exception
    {
      ArrayList<String> pagesCurrentForm = new ArrayList<String>();

      String paSubmittedPage = submittedPage;

      int pageNumberSubmittedPage = Integer.valueOf(submittedPage.substring(submittedPage.lastIndexOf(".") + 1, submittedPage.length())).intValue();
      String paSubmittedPageNoPage = paSubmittedPage.substring(0, paSubmittedPage.lastIndexOf(".")).trim();

      int numberOfPages = PadFileUtility.getPageAddresses(paSubmittedPage, "Anoto_Forms_Solution_ASH").length;
      AshLogger.logFine("Page " + paSubmittedPage + " belongs to a form with " + numberOfPages + " pages");

      String additionalPageAddress = "";
      int pageNumberCurrentPage = pageNumberSubmittedPage;
      int index = pageNumberSubmittedPage % numberOfPages;

      for (int i = index; i > index - numberOfPages; --i)
      {
        pageNumberCurrentPage = pageNumberSubmittedPage - i;
        additionalPageAddress = paSubmittedPageNoPage + "." + pageNumberCurrentPage;
        pagesCurrentForm.add(additionalPageAddress);
      }

      String[] formPageAddresses = pagesCurrentForm.toArray(new String[0]);
      Sorter.sortByPageAddressAsc(formPageAddresses);

      return formPageAddresses;
    }
  }
  
  public static synchronized int addDynamicDataEntry(DynamicDataEntry dynamicDataEntry)
  	throws HibernateException {
	  int receivedFormCopyId = -1;

	  openSession();
	  Session session = getCurrentSession();

	  beginTransaction(session);

	  Integer Id = (Integer) session.save(dynamicDataEntry);
	  
	  try
	  {
	    AshLogger.logFine("Adding a new dynamic data entry: " + dynamicDataEntry.toString());
	
	    receivedFormCopyId = Id.intValue();
	  } catch (Exception e) {
	    AshLogger.logSevere(e.getMessage());
	    receivedFormCopyId = -1;
	  }
	
	  commitTransaction(session);
	
	  closeSession();
	
	  return receivedFormCopyId;
  }
  
  public static synchronized List<?> searchDynamicDataEntry(DynamicDataEntry dataEntry)
  {
    List<?> result = new ArrayList<Object>();

    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting dynamic data entry from database by criteria");
    Criteria crit = SearchDynamicDataEntryCriteriaFactory.createCriteria(session, dataEntry);

    result = crit.list();

    commitTransaction(session);
    closeSession();

    return result;
  }
  
  public static class SearchDynamicDataEntryCriteriaFactory
  {
    public static Criteria createCriteria(Session session, DynamicDataEntry dataEntry)
    {
      if ((session != null) && (dataEntry != null)) {
        Criteria criteria = session.createCriteria(DynamicDataEntry.class);
        AshLogger.logFine("Searching for a dynamic data entry: ");

        return criteria;
      }

      return null;
    }
  }
  
  public static synchronized int addFormCopiesPageAreas(List<?> listFormCopiesPageAreas)
	throws HibernateException {
	  int receivedFormCopyId = -1;

	  openSession();
	  Session session = getCurrentSession();

	  beginTransaction(session);
	  
	  for (Iterator<?> iterator = listFormCopiesPageAreas.iterator(); iterator	.hasNext();) {
		  FormCopyPageArea formCopiesPageAreas = (FormCopyPageArea) iterator.next();
		  session.saveOrUpdate(formCopiesPageAreas);
	  }
	  
	  commitTransaction(session);
	
	  closeSession();
	
	  return receivedFormCopyId;
  }
  
  public static synchronized List<?> searchFormCopiesPageAreas(FormCopyPageArea formCopiesPageAreas)
  {
    List<?> result = new ArrayList<Object>();

    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting formcopies page areas from database by criteria");
    Criteria crit = SearchFormCopiesPageAreasCriteriaFactory.createCriteria(session, formCopiesPageAreas);

    result = crit.list();

    commitTransaction(session);
    closeSession();

    return result;
  }
  
  public static class SearchFormCopiesPageAreasCriteriaFactory
  {
    public static Criteria createCriteria(Session session, FormCopyPageArea formCopiesPageAreas)
    {
      if ((session != null) && (formCopiesPageAreas != null)) {
        Criteria criteria = session.createCriteria(FormCopyPageArea.class);
        AshLogger.logFine("Searching for a formcopies page areas: ");

        if (formCopiesPageAreas.getName() != null && !(formCopiesPageAreas.getName().trim()).equals("")) {
            criteria.add(Expression.eq("name", formCopiesPageAreas.getName()));
        }
        
        if (formCopiesPageAreas.getFormCopyId() != 0) {
        	criteria.add(Expression.eq("formCopyId", formCopiesPageAreas.getFormCopyId()));
        }
        
        return criteria;
      }

      return null;
    }
  }

  public static synchronized List<?> searchFormCopies(FormCopyData formCopies)
  {
    List<?> result = new ArrayList<Object>();

    openSession();
    Session session = getCurrentSession();
    beginTransaction(session);

    AshLogger.logFine("Getting formcopies from database by criteria");
    Criteria crit = SearchFormCopiesCriteriaFactory.createCriteria(session, formCopies);

    result = crit.list();

    commitTransaction(session);
    closeSession();

    return result;
  }
  
  public static class SearchFormCopiesCriteriaFactory
  {
    public static Criteria createCriteria(Session session, FormCopyData formCopies)
    {
      if ((session != null) && (formCopies != null)) {
        Criteria criteria = session.createCriteria(FormCopyData.class);
        AshLogger.logFine("Searching for a formcopies: ");

        if (formCopies.getNumeroOcorrencia() != null && !(formCopies.getNumeroOcorrencia().trim()).equals("")) {
          criteria.add(Expression.eq("numeroOcorrencia", formCopies.getNumeroOcorrencia()));
        }
        
        criteria.addOrder(Order.desc("latestSubmit"));
        
        return criteria;
      }

      return null;
    }
  }
  
  public static synchronized String getMaxPageAddress(String formTypeName) {
	String result = null;
	Session session = DBHandler.getCurrentSession();
	DBHandler.beginTransaction(session);
	
	SQLQuery sql = session.createSQLQuery(
			"SELECT MAX(end_page_address_complete) " +
			"FROM tb_formcopies " + 
			"WHERE fk_form_type_id = (SELECT id FROM tb_formtypes WHERE form_type_name = ?) ");
	sql.setString(0, formTypeName);
    List<?> list = sql.list();
	
	if (list != null && list.size() > 0) {
		result = (String) list.get(0);		
	}
	
    DBHandler.commitTransaction(session);
    DBHandler.closeSession();
    return result;
  }
  
  public static synchronized int getCountPageAddress(String pageAddress) {
	int intResult = 0;
	Session session = DBHandler.getCurrentSession();
	DBHandler.beginTransaction(session);
	
	SQLQuery sql = session.createSQLQuery("SELECT count(*) FROM tb_formcopies WHERE end_page_address LIKE ? ");
	sql.setString(0, pageAddress);
	Integer bigIntResult = (Integer) sql.uniqueResult();
	
	if (bigIntResult != null) {
		intResult = bigIntResult.intValue();
	}
	
    DBHandler.commitTransaction(session);
    DBHandler.closeSession();
    return intResult;
  }
    
  public static synchronized BigInteger getNextValSeq(String nmSequence) {
	Session session = DBHandler.getCurrentSession();
	DBHandler.beginTransaction(session);

	//SQLQuery sql = session.createSQLQuery("select nextval('" + nmSequence + "')");
	SQLQuery sql = session.createSQLQuery("SELECT NEXT VALUE FOR " + nmSequence );

	BigInteger seq = (BigInteger) sql.uniqueResult();
		
    DBHandler.commitTransaction(session);
    DBHandler.closeSession();
    return seq;
  }

}