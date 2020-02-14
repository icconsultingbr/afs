/*     */ package com.anoto.ash;
/*     */ 
/*     */ import com.anoto.ash.database.MailSettingsDBHandler;
/*     */ import com.anoto.ash.database.MailSettingsData;
/*     */ import com.anoto.ash.database.UserData;
/*     */ import com.anoto.ash.portal.utils.ASHValidatorUtility;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
import org.apache.commons.lang.StringUtils;
/*     */ 
          @SuppressWarnings({"unchecked", "rawtypes"})
/*     */ public class MailControl
/*     */ {
/*  30 */   private static MailControl instance = null;
/*  31 */   private static com.anoto.util.MailControl ashMailer = null;
/*  32 */   
/*     */ 
/*     */   public static MailControl getInstance() {
/*  35 */     if (instance == null) {
/*  36 */       instance = new MailControl();
/*     */     }
/*     */ 
/*  39 */     return new MailControl();
/*     */   }
/*     */ 
/*     */   public boolean sendAdminMail(String message, String mailSubject, List<String> attachments)
/*     */   {
/*     */     try
/*     */     {
/*  50 */       String adminAddress = AshCommons.getAdminEmailAddresses();
/*     */ 
/*  52 */       AshLogger.log("Sending an email to admin with message: " + message);
/*  53 */       sendMail(null, message, adminAddress, null, null, mailSubject, attachments);
/*     */     } catch (AshSendMailException e) {
/*  55 */       AshLogger.logSevere(e.getMessage());
/*  56 */       return false;
/*     */     }
/*     */ 
/*  59 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean sendNotificationLevelMail(String message) {
/*     */     try {
/*  64 */       String adminAddress = AshCommons.getAdminEmailAddresses();
/*  65 */       AshLogger.log("Sending notification level mail with message: " + message);
/*  66 */       sendMail(null, message, adminAddress, null, null, "Notifiation level reached.", null);
/*     */     } catch (AshSendMailException e) {
/*  68 */       AshLogger.logSevere(e.getMessage());
/*  69 */       return false;
/*     */     }
/*     */ 
/*  72 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean sendNewAdminAddressMail(UserData user)
/*     */   {
/*  79 */     String mailMessage = "The admin address in the AFS system has been changed to this address\r\n";
/*  80 */     AshLogger.log("Sending email concerning change of admin email address to " + user.getEmail());
/*     */     try
/*     */     {
/*  83 */       sendMail(null, mailMessage, user.getEmail(), null, null, "New admin mail address", null);
/*     */     } catch (AshSendMailException e) {
/*  85 */       AshLogger.logSevere(e.getMessage());
/*  86 */       return false;
/*     */     }
/*     */ 
/*  89 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean sendNewUserMail(UserData user)
/*     */   {
/*  96 */     if (!(ASHValidatorUtility.validEmailAddress(user.getEmail()))) {
/*  97 */       return false;
/*     */     }
/*     */ 
/* 100 */     AshLogger.log("Sending email with new user information for user: " + user.toString());
/* 101 */     String mailMessage = "New login information in the Form portal \r\nUserName: " + user.getUserName() + "\r\n";
/*     */ 
/* 104 */     if (user.getPassword().length() > 0)
/*     */     {
/* 106 */       mailMessage = mailMessage + "Password: " + user.getPassword();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 111 */       sendMail(null, mailMessage, user.getEmail(), null, null, "New user credentials", null);
/*     */     } catch (AshSendMailException e) {
/* 113 */       AshLogger.logSevere(e.getMessage());
/* 114 */       return false;
/*     */     }
/*     */ 
/* 117 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean sendSupportMail(String subject, String text, String toMail, String from)
/*     */   {
/* 123 */     AshLogger.log("Sending a new support mail with subject" + subject);
/*     */     try {
/* 125 */       if (ASHValidatorUtility.validEmailAddress(from)) {
/* 126 */         sendMail(null, text, toMail, from, null, subject, null);
/*     */       }
/*     */       else
/* 129 */         sendMail(null, text, toMail, null, null, subject, null);
/*     */     }
/*     */     catch (AshSendMailException e) {
/* 132 */       AshLogger.logSevere(e.getMessage());
/* 133 */       return false;
/*     */     }
/*     */ 
/* 136 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean sendNewPasswordMail(UserData user, String newPassword)
/*     */   {
/* 141 */     AshLogger.log("Sending new password to the user: " + user.toString());
/* 142 */     String mailMessage = "New Password for user with userName: " + user + "\r\n" + "in the Anoto AFS\r\n" + "The new password is: " + newPassword;
/*     */     try
/*     */     {
/* 147 */       sendMail(null, mailMessage, user.getEmail(), null, null, "New user credentials", null);
/*     */     } catch (AshSendMailException e) {
/* 149 */       AshLogger.logSevere(e.getMessage());
/* 150 */       return false;
/*     */     }
/*     */ 
/* 153 */     return true;
/*     */   }
/*     */ 
/*     */   
	        public static synchronized void sendMail(MailSettingsData ms, String textToAppend, String mailTo, String cc, String mailFrom, String mailSubject, List<String> attachments) throws AshSendMailException {
/* 157 */     String from = mailFrom;
/* 158 */     MailSettingsData mailSettings = ms;
/*     */     try
/*     */     {
/* 161 */       if (null == mailSettings)
/*     */       {
/* 163 */         mailSettings = MailSettingsDBHandler.getMailSettings();
/*     */       }
/* 165 */       int port = -1;
/* 166 */       int popPort = -1;
/* 167 */       String mailer = mailSettings.getSmtpMailer();
/* 168 */       String host = mailSettings.getSmtpHost();
/* 169 */       String p = mailSettings.getSmtpPort();
/* 170 */       String pp = mailSettings.getPopPort();
/*     */ 
/* 172 */       if (null == from)
/*     */       {
/* 174 */         from = mailSettings.getSmtpFrom();
/*     */       }
/*     */ 
/* 177 */       boolean imap = false;
/*     */ 
/* 179 */       if (mailSettings.isImapActivated() == 1) {
/* 180 */         imap = true;
/*     */       }
/*     */ 
/* 183 */       String ehlo = mailSettings.getEhlo();
/*     */ 
/* 185 */       if ((null != p) && (!("".equals(p))))
/*     */         try {
/* 187 */           port = Integer.parseInt(p);
/*     */         }
/*     */         catch (NumberFormatException nfe) {
/*     */         }
/* 191 */       if ((null != pp) && (!("".equals(pp))))
/*     */         try {
/* 193 */           popPort = Integer.parseInt(pp);
/*     */         }
/*     */         catch (NumberFormatException nfe)
/*     */         {
/*     */         }
/* 198 */       String authUser = mailSettings.getAuthUser();
/* 199 */       String authPassword = mailSettings.getAuthPassword();
/*     */ 
/* 201 */       String encryption = mailSettings.getEncryption();
/*     */ 
/* 203 */       boolean isSsl = (null != encryption) && (encryption.equals("SSL"));
/* 204 */       boolean isTls = (null != encryption) && (encryption.equals("TLS"));
/*     */ 
/* 206 */       String popUser = mailSettings.getPopUsername();
/* 207 */       String popPassword = mailSettings.getPopPassword();
/*     */ 
/* 209 */       ashMailer = new com.anoto.util.MailControl();
/*     */ 
/* 211 */       if ((StringUtils.isNotEmpty(popUser)) && (StringUtils.isNotEmpty(popPassword)))
/* 212 */         ashMailer.setPopBeforeSmtp(popPort, mailSettings.getPopHost(), popUser, popPassword);
/* 213 */       else if (imap) {
/* 214 */         ashMailer.setImap(true);
/*     */       }
/*     */ 
/* 217 */       if (StringUtils.isNotEmpty(ehlo)) {
/* 218 */         ashMailer.setEhlo(ehlo);
/*     */       }
/*     */ 
/* 221 */       ashMailer.setSsl(isSsl);
/* 222 */       ashMailer.setTls(isTls);
/*     */ 
/* 225 */       AshLogger.logFine("Sending mail to: " + mailTo);
/* 226 */       AshLogger.logFine("CC: " + cc);
/* 227 */       AshLogger.logFine("From: " + from);
/* 228 */       AshLogger.logFine("Subject: " + mailSubject);
/* 229 */       AshLogger.logFine("Content: " + textToAppend);
/*     */ 
/* 232 */       List toAddresses = new ArrayList();
/* 233 */       String[] tos = mailTo.split(",");
/*     */ 
/* 235 */       for (int i = 0; i < tos.length; ++i) {
/* 236 */         toAddresses.add(tos[i]);
/*     */       }
/*     */ 
/* 239 */       mailTo = (String)toAddresses.get(0);
/*     */ 
/* 241 */       List ccAddresses = new ArrayList();
/* 242 */       if (toAddresses.size() > 1) {
/* 243 */         ccAddresses.addAll(toAddresses.subList(1, toAddresses.size()));
/*     */       }
/*     */ 
/* 246 */       if ((cc != null) && (cc.length() > 0)) {
/* 247 */         String[] ccs = cc.split(",");
/*     */ 
/* 249 */         for (int i = 0; i < ccs.length; ++i) {
/* 250 */           ccAddresses.add(ccs[i]);
/*     */         }
/*     */       }
/* 253 */       ashMailer.sendMultipartEmail(authUser, authPassword, port, host, textToAppend, mailSubject, attachments, ccAddresses, mailTo, from, mailer);
/*     */     }
/*     */     catch (Exception e) {
/* 256 */       AshLogger.logSevere("Couldn't send mail: \nMail To: " + mailTo + "\nMail From:" + from + "\nSubject: " + mailSubject + "\nReason: " + e.getMessage());
/*     */ 
/* 261 */       throw new AshSendMailException(e);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\public\afs_demo\WEB-INF\lib\ash-core.jar
 * Qualified Name:     com.anoto.ash.MailControl
 * JD-Core Version:    0.5.3
 */