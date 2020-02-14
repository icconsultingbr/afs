package com.anoto.ash.database;

import java.util.Date;
import java.util.Set;


public class FormTypeData
{
  private int formTypeId;
  private String formTypeName;
  private byte[] pdfFile;
  private int maxNbrOfFormCopies;
  private int remainingFormCopies;
  private int notificationLevel;
  private int notificationLevelPercent;
  private int allowPaperBasedVerification;
  private int pod;
  private Set<DynamicDataDefinition> dynamicDataDefinitions;
  private Date creationDate;
  private int displayPeriod;
  private ExportMethod exportMethod;
  private int multiplePenMode;
  private int multipleCompletionsMode;
  private boolean correct;

  public FormTypeData()
  {
    this.formTypeId = -1;
    this.formTypeName = "";

    this.maxNbrOfFormCopies = -1;
    this.remainingFormCopies = -1;
    this.notificationLevel = -1;
    this.notificationLevelPercent = -1;
    this.allowPaperBasedVerification = -1;
    this.pod = -1;

    this.displayPeriod = 10000;
    this.exportMethod = new ExportMethod();
    this.multiplePenMode = 0;

    this.correct = false; }

  public int getFormTypeId() {
    return this.formTypeId;
  }

  public void setFormTypeId(int formTypeId) {
    this.formTypeId = formTypeId;
  }

  public String getFormTypeName() {
    return this.formTypeName;
  }

  public void setFormTypeName(String formTypeName) {
    this.formTypeName = formTypeName;
  }

  public int getRemainingFormCopies() {
    return this.remainingFormCopies;
  }

  public void setRemainingFormCopies(int remainingFormCopies) {
    this.remainingFormCopies = remainingFormCopies;
  }

  public int getNotificationLevel() {
    return this.notificationLevel;
  }

  public void setNotificationLevel(int notificationLevel) {
    this.notificationLevel = notificationLevel;

    this.notificationLevelPercent = (int)(this.notificationLevel / this.maxNbrOfFormCopies * 100.0D + 0.5D);
  }

  public int getNbrOfUsedFormCopies() {
    return (this.maxNbrOfFormCopies - this.remainingFormCopies);
  }

  public int getMaxNbrOfFormCopies() {
    return this.maxNbrOfFormCopies;
  }

  public void setMaxNbrOfFormCopies(int maxNbrOfFormCopies) {
    this.maxNbrOfFormCopies = maxNbrOfFormCopies;
  }

  public int getNotificationLevelPercent()
  {
    return this.notificationLevelPercent;
  }

  public void setNotificationLevelPercent(int notificationLevelPercent)
  {
    this.notificationLevelPercent = notificationLevelPercent;

    this.notificationLevel = ((int)(this.maxNbrOfFormCopies * this.notificationLevelPercent + 0.5D) / 100);
  }

  public int getAllowPaperBasedVerification() {
    return this.allowPaperBasedVerification;
  }

  public void setAllowPaperBasedVerification(int allowPaperBasedVerification) {
    this.allowPaperBasedVerification = allowPaperBasedVerification;
  }

  public boolean getAllowedPaperBasedVerificationAsBoolean()
  {
    return (this.allowPaperBasedVerification == 1);
  }

  public Date getCreationDate()
  {
    return this.creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public int getDisplayPeriod() {
    return this.displayPeriod;
  }

  public void setDisplayPeriod(int displayPeriod) {
    this.displayPeriod = displayPeriod;
  }

  public Set<DynamicDataDefinition> getDynamicDataDefinitions() {
    return this.dynamicDataDefinitions;
  }

  public void setDynamicDataDefinitions(Set<DynamicDataDefinition> dynamicDataDefinitions) {
    this.dynamicDataDefinitions = dynamicDataDefinitions;
  }

  public byte[] getPdfFile() {
    return pdfFile;
  }

  public void setPdfFile(byte[] pdfFile) {
	this.pdfFile = pdfFile;
  }

  public int getPod() {
    return this.pod;
  }

  public void setPod(int pod) {
    this.pod = pod;
  }

  public ExportMethod getExportMethod() {
    return this.exportMethod;
  }

  public void setExportMethod(ExportMethod exportMethod) {
    this.exportMethod = exportMethod;
  }

  public int getMultipleCompletionsMode() {
    return this.multipleCompletionsMode;
  }

  public void setMultipleCompletionsMode(int multipleCompletionsMode) {
    this.multipleCompletionsMode = multipleCompletionsMode;
  }

  public int getMultiplePenMode() {
    return this.multiplePenMode;
  }

  public void setMultiplePenMode(int multiplePenMode) {
    this.multiplePenMode = multiplePenMode;
  }

  public boolean isCorrect() {
    return this.correct;
  }

  public void setCorrect(boolean correct) {
    this.correct = correct;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("FormTypeData[\n").append("\tid = ").append(getFormTypeId()).append("\n\tformTypeName = ").append(getFormTypeName()).append("\n\tmaxNbrOfFormCopies = ").append(getMaxNbrOfFormCopies()).append("\n\tremainingFormCopies = ").append(getRemainingFormCopies()).append("\n\tnotificationLevel = ").append(getNotificationLevel()).append("\n\tnotificationLevelPercent = ").append(getNotificationLevelPercent());

    if (this.dynamicDataDefinitions != null) {
      sb.append("\n\tDynamicDataDefinitions = ").append(this.dynamicDataDefinitions.toArray());
    }

    sb.append("\n]");

    return sb.toString();
  }
}