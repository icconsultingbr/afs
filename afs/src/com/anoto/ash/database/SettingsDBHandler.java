/*     */ package com.anoto.ash.database;
/*     */ 
/*     */ import com.anoto.ash.AshLogger;
/*     */ import com.anoto.ash.AshSetupControl;
/*     */ import java.util.List;
/*     */ import org.hibernate.Hibernate;
import org.hibernate.Session;
/*     */ 
		  @SuppressWarnings({"unchecked", "rawtypes"})
/*     */ public class SettingsDBHandler extends DBHandler
/*     */ {
/*     */   
			public static synchronized SettingsData getSettings()
/*     */   {
/*  21 */     AshLogger.logFine("About to retrieve settings from DB");
/*  22 */     openSession();
/*  23 */     Session session = getCurrentSession();
/*  24 */     beginTransaction(session);
/*     */ 
/*  26 */     SettingsData settingsData = null;
/*  27 */     List result = session.createQuery("from SettingsData").list();
/*     */ 
/*  30 */     if ((result == null) || (result.size() == 0)) {
/*  31 */       AshSetupControl.createDefaultValuesForDB(true);
/*  32 */       result = session.createQuery("from SettingsData").list();
/*     */     }
/*     */ 
/*  35 */     List settingsDataList = result;
/*     */ 
/*  37 */     if ((settingsDataList != null) && (settingsDataList.size() > 0)) {
/*  38 */       settingsData = (SettingsData)settingsDataList.get(0);
/*     */ 
/*  40 */       if (settingsData.getFppUrl() == null) {
/*  41 */         settingsData.setFppUrl("");
/*     */       }
/*     */ 
/*  44 */       if (settingsData.getLogLevel() == null) {
/*  45 */         settingsData.setLogLevel(new LogLevel());
/*     */       }
/*     */ 
/*  48 */       if (settingsData.getRootFolder() == null) {
/*  49 */         settingsData.setRootFolder("");
/*     */       }
/*     */ 
/*  52 */       if (settingsData.getVerificationModule() == null) {
/*  53 */         settingsData.setVerificationModule(new VerificationModule());
/*     */       }
/*     */     }
/*  56 */     commitTransaction(session);
/*  57 */     closeSession();
/*     */ 
/*  59 */     return settingsData;
/*     */   }
/*     */ 
/*     */   public static synchronized int updateSettings(SettingsData settingsData) {
/*  63 */     SettingsData oldData = getSettings();
/*     */ 
/*  65 */     if (oldData == null)
/*     */     {
/*  67 */       return -1;
/*     */     }
/*     */ 
/*  70 */     openSession();
/*  71 */     Session session = getCurrentSession();
/*     */ 
/*  73 */     beginTransaction(session);
/*     */ 
/*  76 */     settingsData.setId(oldData.getId());
/*     */ 
/*  78 */     if (settingsData.getLogLevel().getName().equals(oldData.getLogLevel().getName())) {
/*  79 */       settingsData.setLogLevel(oldData.getLogLevel());
/*  80 */     } else if (settingsData.getLogLevel().getId() == -1)
/*     */     {
/*  83 */       LogLevel logLvl = (LogLevel)session.createQuery("from LogLevel loglvl where loglvl.name = ?").setString(0, settingsData.getLogLevel().getName()).uniqueResult();
/*  84 */       settingsData.setLogLevel(logLvl);
/*     */     }
/*     */ 
/*  87 */     if (settingsData.getVerificationModule().getName().equals(oldData.getVerificationModule().getName())) {
/*  88 */       settingsData.setVerificationModule(oldData.getVerificationModule());
/*  89 */     } else if (settingsData.getVerificationModule().getId() == -1)
/*     */     {
/*  92 */       VerificationModule verModel = (VerificationModule)session.createQuery("from VerificationModule verModel where verModel.name = ?").setString(0, settingsData.getVerificationModule().getName()).uniqueResult();
/*  93 */       settingsData.setVerificationModule(verModel);
/*     */     }
/*     */ 
/*  96 */     session.saveOrUpdate(settingsData);
/*     */ 
/*  98 */     AshLogger.logFine("Updating the values for the settings, new settings: " + settingsData.toString());
/*     */ 
/* 100 */     commitTransaction(session);
/* 101 */     closeSession();
/* 102 */     return 0;
/*     */   }
/*     */ 
/*     */   public static synchronized void addVerificationModule(String name) {
/* 106 */     openSession();
/* 107 */     Session session = getCurrentSession();
/*     */ 
/* 109 */     beginTransaction(session);
/* 110 */     AshLogger.logFine("Adding a new verification module: " + name);
/* 111 */     VerificationModule vm = new VerificationModule();
/* 112 */     vm.setName(name);
/*     */ 
/* 114 */     session.save(vm);
/*     */ 
/* 116 */     commitTransaction(session);
/* 117 */     closeSession();
/*     */   }
/*     */ 
/*     */   public static synchronized List getVerificationModules() {
/* 121 */     openSession();
/* 122 */     Session session = getCurrentSession();
/*     */ 
/* 124 */     beginTransaction(session);
/* 125 */     AshLogger.logFine("Retrieving all verification modules.");
/* 126 */     //List lvls = session.createSQLQuery("SELECT name as name FROM verification_module").addScalar("name", Hibernate.STRING).list();
			  List lvls = session.createSQLQuery("SELECT name as name FROM tb_verification_module").addScalar("name", Hibernate.STRING).list();
/*     */ 
/* 128 */     commitTransaction(session);
/* 129 */     closeSession();
/* 130 */     return lvls;
/*     */   }
/*     */ 
/*     */   public static synchronized void addLogLevel(String name) {
/* 134 */     openSession();
/* 135 */     Session session = getCurrentSession();
/*     */ 
/* 137 */     beginTransaction(session);
/* 138 */     AshLogger.logFine("Adding a new log level: " + name);
/* 139 */     LogLevel level = new LogLevel();
/* 140 */     level.setName(name);
/*     */ 
/* 142 */     session.save(level);
/*     */ 
/* 144 */     commitTransaction(session);
/* 145 */     closeSession();
/*     */   }
/*     */ 
/*     */   public static synchronized List getAllPossibleLogLevels() {
/* 149 */     openSession();
/* 150 */     Session session = getCurrentSession();
/*     */ 
/* 152 */     beginTransaction(session);
/* 153 */     AshLogger.logFine("Retrieving all possible log levels.");
/* 154 */     //List lvls = session.createSQLQuery("SELECT name as name FROM log_levels").addScalar("name", Hibernate.STRING).list();
			  List lvls = session.createSQLQuery("SELECT name as name FROM tb_log_levels").addScalar("name", Hibernate.STRING).list();
/*     */ 
/* 156 */     commitTransaction(session);
/* 157 */     closeSession();
/* 158 */     return lvls;
/*     */   }
/*     */ 
/*     */   public static synchronized List<ExportFormat> getAllPossibleExportFormats() {
/* 162 */     openSession();
/* 163 */     Session session = getCurrentSession();
/*     */ 
/* 165 */     beginTransaction(session);
/* 166 */     AshLogger.logFine("Retrieving all possible export formats.");
/* 167 */     List lvls = session.createQuery("from ExportFormat").list();
/*     */ 
/* 169 */     commitTransaction(session);
/* 170 */     closeSession();
/* 171 */     return lvls;
/*     */   }
/*     */ 
/*     */   public static List<ImageFormat> getImageFormats()
/*     */   {
/* 176 */     AshLogger.logFine("About to retrieve image formats from DB");
/* 177 */     openSession();
/* 178 */     Session session = getCurrentSession();
/* 179 */     beginTransaction(session);
/*     */ 
/* 181 */     List result = session.createQuery("from ImageFormat").list();
/*     */ 
/* 183 */     commitTransaction(session);
/* 184 */     closeSession();
/*     */ 
/* 186 */     return result;
/*     */   }
/*     */ }