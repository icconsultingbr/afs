/*     */ package com.anoto.util;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import javax.mail.MessagingException;
/*     */ import javax.mail.NoSuchProviderException;
/*     */ import org.apache.commons.mail.Email;
/*     */ import org.apache.commons.mail.EmailAttachment;
/*     */ import org.apache.commons.mail.EmailException;
/*     */ import org.apache.commons.mail.HtmlEmail;
/*     */ import org.apache.commons.mail.MultiPartEmail;
/*     */ 
/*     */ public class MailControl
/*     */ {
/*  26 */   private static int DEFAULT_SMTP_PORT = 25;
/*  27 */   private static int DEFAULT_SSL_PORT = 465;
/*  28 */   private static int DEFAULT_POP_PORT = 110;
/*  29 */   private static int DEFAULT_POP_SSL_PORT = 995;
/*  30 */   private static int DEFAULT_IMAP_PORT = 143;
/*  31 */   private static int DEFAULT_IMAP_SSL_PORT = 993;
/*  32 */   private String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
/*  33 */   private static String DEFAULT_HOST = "localhost";
/*     */   private boolean tls;
/*     */   private boolean ssl;
/*     */   private boolean debug;
/*     */   private boolean imap;
/*     */   private boolean popBeforeSmtp;
/*     */   private int pop3port;
/*     */   private String pop3host;
/*     */   private String pop3user;
/*     */   private String pop3password;
/*     */   private String ehlo;
/*     */   Properties properties;
/*     */ 
/*     */   public MailControl()
/*     */   {
/*  61 */     this.tls = false;
/*  62 */     this.ssl = false;
/*  63 */     this.debug = false;
/*  64 */     this.imap = false;
/*  65 */     this.popBeforeSmtp = false;
/*  66 */     this.pop3port = -1;
/*  67 */     this.pop3host = null;
/*  68 */     this.pop3user = null;
/*  69 */     this.pop3password = null;
/*  70 */     this.ehlo = null;
/*  71 */     this.properties = null;
/*     */   }
/*     */ 
/*     */   public boolean isImap() {
/*  75 */     return this.imap;
/*     */   }
/*     */ 
/*     */   public void setImap(boolean imap)
/*     */   {
/*  84 */     this.imap = imap;
/*  85 */     this.popBeforeSmtp = false;
/*     */   }
/*     */ 
/*     */   public String getEhlo() {
/*  89 */     return this.ehlo;
/*     */   }
/*     */ 
/*     */   public void setEhlo(String ehlo)
/*     */   {
/*  98 */     this.ehlo = ehlo;
/*     */   }
/*     */ 
/*     */   public boolean isPopBeforeSmtp() {
/* 102 */     return this.popBeforeSmtp;
/*     */   }
/*     */ 
/*     */   public boolean isDebug() {
/* 106 */     return this.debug;
/*     */   }
/*     */ 
/*     */   public void setDebug(boolean debug)
/*     */   {
/* 114 */     this.debug = debug;
/*     */   }
/*     */ 
/*     */   public void setSsl(boolean ssl)
/*     */   {
/* 122 */     this.ssl = ssl;
/*     */   }
/*     */ 
/*     */   public boolean isSsl() {
/* 126 */     return this.ssl;
/*     */   }
/*     */ 
/*     */   public boolean isTls() {
/* 130 */     return this.tls;
/*     */   }
/*     */ 
/*     */   public void setTls(boolean tls)
/*     */   {
/* 138 */     this.tls = tls;
/*     */   }
/*     */ 
/*     */   public String sendHtmlEmail(String userName, String password, int port, String host, String message, String mailSubject, List<String> cc, String recipient, String sender, String mailer)
/*     */     throws EmailException, MessagingException
/*     */   {
/* 162 */     HtmlEmail email = new HtmlEmail();
/* 163 */     email.setHtmlMsg(message);
/* 164 */     email.setTextMsg("YOUR EMAIL CLIENT DOES NOT SUPPORT HTML \n\n" + message);
/*     */ 
/* 166 */     return send(port, host, userName, password, email, cc, recipient, sender, null, mailSubject);
/*     */   }
/*     */ 
/*     */   public String sendMultipartEmail(String userName, String password, int port, String host, String message, String mailSubject, List<String> attachments, List<String> cc, String recipient, String sender, String mailer)
/*     */     throws EmailException, NoSuchProviderException, MessagingException
/*     */   {
/* 193 */     MultiPartEmail email = new MultiPartEmail();
/* 194 */     email.setMsg(message);
/* 195 */     attach(email, attachments);
/* 196 */     return send(port, host, userName, password, email, cc, recipient, sender, null, mailSubject);
/*     */   }
/*     */ 
/*     */   public void setPopBeforeSmtp(int pop3port, String pop3host, String pop3user, String pop3password)
/*     */   {
/* 210 */     this.pop3port = pop3port;
/* 211 */     this.pop3host = pop3host;
/* 212 */     this.pop3user = pop3user;
/* 213 */     this.pop3password = pop3password;
/* 214 */     this.popBeforeSmtp = true;
/* 215 */     this.imap = false;
/*     */   }
/*     */ 
/*     */   private void attach(MultiPartEmail mpe, List<String> attachments) throws EmailException {
/* 219 */     if ((attachments == null) || (attachments.size() <= 0))
/*     */       return;
/* 221 */     for (String path : attachments) {
/* 222 */       EmailAttachment attachment = new EmailAttachment();
/* 223 */       File attachmentFile = new File(path);
/* 224 */       attachment.setPath(path);
/* 225 */       attachment.setName(attachmentFile.getName());
/* 226 */       mpe.attach(attachment);
/*     */     }
/*     */   }
/*     */ 
/*     */   private String send(int port, String host, String username, String password, Email email, List<String> cc, String recipient, String sender, String mailer, String mailSubject)
/*     */     throws EmailException, NoSuchProviderException, MessagingException
/*     */   {
/* 235 */     if (-1 == port) {
/* 236 */       port = DEFAULT_SMTP_PORT;
/* 237 */       if ((this.ssl) && (this.imap))
/* 238 */         port = DEFAULT_IMAP_SSL_PORT;
/* 239 */       else if (this.imap)
/* 240 */         port = DEFAULT_IMAP_PORT;
/* 241 */       else if (this.ssl) {
/* 242 */         port = DEFAULT_SSL_PORT;
/*     */       }
/*     */     }
/* 245 */     if ((this.popBeforeSmtp) && (-1 == this.pop3port) && (this.ssl))
/* 246 */       this.pop3port = DEFAULT_POP_SSL_PORT;
/* 247 */     else if ((this.popBeforeSmtp) && (-1 == this.pop3port)) {
/* 248 */       this.pop3port = DEFAULT_POP_PORT;
/*     */     }
/*     */ 
/* 251 */     if (null == host) {
/* 252 */       host = DEFAULT_HOST;
/*     */     }
/* 254 */     if (this.debug)
/* 255 */       email.setDebug(true);
/*     */     else {
/* 257 */       email.setDebug(false);
/*     */     }
/*     */ 
/* 260 */     if (null != mailer) {
/* 261 */       email.addHeader("X-Mailer", mailer);
/*     */     }
/*     */ 
/* 264 */     email.addTo(recipient);
/* 265 */     email.setFrom(sender);
/* 266 */     if (null != mailSubject) {
/* 267 */       email.setSubject(mailSubject);
/*     */     }
/*     */ 
/* 270 */     email.setSentDate(new Date());
/*     */ 
/* 273 */     email.setHostName(host);
/* 274 */     email.setSmtpPort(port);
/*     */ 
/* 277 */     if ((cc != null) && (cc.size() > 0)) {
/* 278 */       for (String ccEmail : cc) {
/* 279 */         email.addCc(ccEmail);
/*     */       }
/*     */     }
/*     */ 
/* 283 */     if (this.tls)
/* 284 */       getProperties().setProperty("mail.smtp.starttls.enable", "true");
/*     */     else {
/* 286 */       getProperties().setProperty("mail.smtp.starttls.enable", "false");
/*     */     }
/* 288 */     if (this.ssl) {
/* 289 */       this.properties = getProperties();
/*     */ 
/* 291 */       this.properties.setProperty("mail.smtp.port", String.valueOf(port));
/* 292 */       this.properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
/* 293 */       this.properties.setProperty("mail.smtp.socketFactory.class", this.SSL_FACTORY);
/* 294 */       this.properties.setProperty("mail.smtp.socketFactory.fallback", "false");
/*     */ 
/* 296 */       if (this.popBeforeSmtp) {
/* 297 */         this.properties.setProperty("mail.pop3.socketFactory.port", String.valueOf(this.pop3port));
/* 298 */         this.properties.setProperty("mail.pop3.socketFactory.class", this.SSL_FACTORY);
/* 299 */         this.properties.setProperty("mail.pop3.socketFactory.fallback", "false");
/* 300 */       } else if (this.imap) {
/* 301 */         this.properties.setProperty("mail.imap.socketFactory.port", String.valueOf(port));
/* 302 */         this.properties.setProperty("mail.imap.socketFactory.class", this.SSL_FACTORY);
/* 303 */         this.properties.setProperty("mail.imap.socketFactory.fallback", "false");
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 308 */       this.properties = getProperties();
/*     */ 
/* 310 */       this.properties.setProperty("mail.smtp.port", String.valueOf(port));
/* 311 */       this.properties.remove("mail.smtp.socketFactory.port");
/* 312 */       this.properties.remove("mail.smtp.socketFactory.class");
/* 313 */       this.properties.remove("mail.smtp.socketFactory.fallback");
/*     */ 
/* 315 */       this.properties.remove("mail.pop3.socketFactory.port");
/* 316 */       this.properties.remove("mail.pop3.socketFactory.class");
/* 317 */       this.properties.remove("mail.pop3.socketFactory.fallback");
/* 318 */       this.properties.remove("mail.imap.socketFactory.port");
/* 319 */       this.properties.remove("mail.imap.socketFactory.class");
/* 320 */       this.properties.remove("mail.imap.socketFactory.fallback");
/*     */     }
/*     */ 
/* 324 */     if (this.popBeforeSmtp) {
/* 325 */       if (null == this.pop3host) {
/* 326 */         this.pop3host = host;
/*     */       }
/* 328 */       if (null == this.pop3user) {
/* 329 */         this.pop3user = username;
/*     */       }
/* 331 */       if (null == this.pop3password) {
/* 332 */         this.pop3password = password;
/*     */       }
/* 334 */       getProperties().setProperty("mail.pop3.port", String.valueOf(this.pop3port));
/* 335 */       email.setPopBeforeSmtp(true, this.pop3host, this.pop3user, this.pop3password);
/*     */     }
/* 337 */     else if (this.imap) {
/* 338 */       getProperties().setProperty("mail.imap.port", String.valueOf(port));
/* 339 */       email.setPopBeforeSmtp(false, null, null, null);
/*     */     }
/*     */     else
/*     */     {
/* 343 */       getProperties().remove("mail.imap.port");
/* 344 */       email.setPopBeforeSmtp(false, null, null, null);
/*     */     }
/*     */ 
/* 347 */     if (null != this.ehlo)
/*     */     {
/* 349 */       getProperties().setProperty("mail.smtp.localhost", this.ehlo);
/*     */     }
/*     */     else
/*     */     {
/* 353 */       getProperties().remove("mail.smtp.localhost");
/*     */     }
/*     */ 
/* 356 */     if ((null != username) && (null != password)) {
/* 357 */       email.setAuthentication(username, password);
/*     */     }
/*     */ 
/* 360 */     return email.send();
/*     */   }
/*     */ 
/*     */   private Properties getProperties() {
/* 364 */     if (null == this.properties) {
/* 365 */       this.properties = System.getProperties();
/*     */     }
/* 367 */     return this.properties;
/*     */   }
/*     */ }