package com.anoto.ash.database;

import com.anoto.api.Pen;
import com.anoto.api.util.PageAddress;
import com.anoto.ash.vo.ink.VOInterpretationResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class FormCopyData implements Comparable<Object> {
	
  public static final int NO = 0;
  public static final int YES = 1;
  public static final byte PIDGET_NOT_TICKED = 0;
  public static final byte MISSING_FIELDS = 1;
  public static final byte VERIFICATION_NEEDED = 2;
  public static final byte COMPLETED = 3;
  public static final byte UNKNOWN_STATUS = 4;
  private int formCopyId;
  private volatile byte[] pgc;
  private volatile byte[] temporaryPgc;
  private String pageAddressRange;
  private String endPageAddress;
  private String endPageAddressComplete;
  private FormTypeData formType;
  private UserData owner;
  private int markedCompleted;
  private int verificationNeeded;
  private int mandatoryFieldsMissing;
  private int locked;
  private Date latestSubmit;
  private ArrayList<Object> missingMandatoryFields;
  private ArrayList<Object> missingMandatoryOrFieldGroups;
  private VOInterpretationResult interpretationResult;
  private int verificationInProgress;
  private boolean verificationFailed;
  private boolean postCompletionDataNotCompleted;
  private int hidden;
  private String feedbackMessage;
  private Set<DynamicDataEntry> dynamicDataEntries;
  private boolean illegalPen;
  private boolean incorrectFormType;
  private int imageMailFailed;
  private int exportMailFailed;
  
  private String numeroOcorrencia;


public FormCopyData() {
    this.formCopyId = -10;

    this.pageAddressRange = "";
    this.endPageAddress = "";

    this.markedCompleted = -10;
    this.verificationNeeded = -10;
    this.mandatoryFieldsMissing = -10;
    this.locked = -10;

    this.missingMandatoryFields = new ArrayList<Object>();
    this.missingMandatoryOrFieldGroups = new ArrayList<Object>();
    this.interpretationResult = new VOInterpretationResult();
    this.verificationInProgress = 0;
    this.verificationFailed = false;
    this.postCompletionDataNotCompleted = false;
    this.hidden = 0;
    this.feedbackMessage = "";
    this.dynamicDataEntries = new HashSet<DynamicDataEntry>();
    this.illegalPen = false;
    this.incorrectFormType = false;
    this.imageMailFailed = -10;
    this.exportMailFailed = -10; }


  public int getFormCopyId() {
    return this.formCopyId;
  }

  public void setFormCopyId(int formCopyId) {
    this.formCopyId = formCopyId;
  }

  public String getPageAddressRange() {
    return this.pageAddressRange;
  }

  public void setPageAddressRange(String pageAddressRange) {
    this.pageAddressRange = pageAddressRange;
  }

  public long getEndPageAddressAsLong() {
    PageAddress pa = new PageAddress(this.endPageAddress);

    return pa.longValue();
  }

  public String getEndPageAddress() {
    return this.endPageAddress;
  }

  public void setEndPageAddress(String endPageAddress) {
    this.endPageAddress = endPageAddress;
  }
  
  public String getEndPageAddressComplete() {
	    return endPageAddressComplete;
  }

  public void setEndPageAddressComplete(String endPageAddressComplete) {
	    this.endPageAddressComplete = endPageAddressComplete;
  }

  public FormTypeData getFormType() {
    return this.formType;
  }

  public void setFormType(FormTypeData formType) {
    this.formType = formType;
  }

  public int getMarkedCompleted() {
    return this.markedCompleted;
  }

  public void setMarkedCompleted(int markedCompleted) {
    this.markedCompleted = markedCompleted;
  }

  public int getMandatoryFieldsMissing() {
    return this.mandatoryFieldsMissing;
  }

  public void setMandatoryFieldsMissing(int mandatoryFieldsMissing) {
    this.mandatoryFieldsMissing = mandatoryFieldsMissing;
  }

  public Date getLatestSubmit() {
    return this.latestSubmit;
  }

  public void setLatestSubmit(Date latestSubmit) {
    this.latestSubmit = latestSubmit;
  }

  public byte[] getPgc() {
    return pgc;
  }

  public void setPgc(byte[] pgc) {
	this.pgc = pgc;
  }

  public byte[] getTemporaryPgc() {
	return temporaryPgc;
  }

  public void setTemporaryPgc(byte[] temporaryPgc) {
	this.temporaryPgc = temporaryPgc;
  }

  public void setPgc(Pen pgcData) {
    try {
		this.pgc = getBytesFromPen(pgcData);
	} catch (IOException e) {}
  }

  public void setTemporaryPgc(Pen pgcData) {
    if (pgcData != null) {
	  try {
	    this.temporaryPgc = getBytesFromPen(pgcData);
	  } catch (IOException e) {}
    } else {
	    this.temporaryPgc = null;
    }
  }
  
  public UserData getOwner()
  {
    return this.owner;
  }

  public void setOwner(UserData owner) {
    this.owner = owner;
  }

  public int getVerificationNeeded() {
    return this.verificationNeeded;
  }

  public void setVerificationNeeded(int verificationNeeded) {
    this.verificationNeeded = verificationNeeded;
  }

  public ArrayList<Object> getMissingMandatoryFields()
  {
    return this.missingMandatoryFields;
  }

  public void setMissingMandatoryFields(ArrayList<Object> missingMandatoryFields) {
    this.missingMandatoryFields = missingMandatoryFields;
  }

  public ArrayList<Object> getMissingMandatoryOrFieldGroups() {
    return this.missingMandatoryOrFieldGroups;
  }

  public void setMissingMandatoryOrFieldGroups(ArrayList<Object> missingMandatoryOrFieldGroups) {
    this.missingMandatoryOrFieldGroups = missingMandatoryOrFieldGroups;
  }

  public int getHidden() {
    return this.hidden;
  }

  public void setHidden(int hidden) {
    this.hidden = hidden;
  }

  public Set<DynamicDataEntry> getDynamicDataEntries() {
    return this.dynamicDataEntries;
  }

  public void setDynamicDataEntries(Set<DynamicDataEntry> dynamicDataEntries) {
    this.dynamicDataEntries = dynamicDataEntries;
  }

  public int compareTo(Object o) {
    FormCopyData that = (FormCopyData)o;

    if (this == that)
      return 0;

    if (this.formCopyId == that.getFormCopyId())
      return 0;
    if (this.formCopyId < that.getFormCopyId())
      return -1;

    return 1;
  }

  private byte[] getBytesFromPen(Pen pen)
    throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    pen.write(out);

    byte[] data = out.toByteArray();

    return data;
  }

  public VOInterpretationResult getInterpretationResult() {
    return this.interpretationResult;
  }

  public void setInterpretationResult(VOInterpretationResult interpretationResult) {
    this.interpretationResult = interpretationResult;
  }

  public int getVerificationInProgress() {
    return this.verificationInProgress;
  }

  public void setVerificationInProgress(int verificationInProgress) {
    this.verificationInProgress = verificationInProgress;
  }

  public String getFeedbackMessage() {
    return this.feedbackMessage;
  }

  public void setFeedbackMessage(String msg) {
    this.feedbackMessage = msg;
  }

  public boolean equals(Object o)
  {
    if (this == o)
      return true;

    if (!(o instanceof FormCopyData))
      return false;

    FormCopyData that = (FormCopyData)o;
    return (getFormCopyId() == that.getFormCopyId());
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("FormCopyData[\n").append("\tid = ").append(getFormCopyId()).append("\n\tpageAddressRange = ").append(getPageAddressRange()).append("\n\tendPageAddress = ").append(getEndPageAddress()).append("\n\tformType = ").append(getFormType()).append("\n\tcorrectionPortalUser = ").append(getOwner()).append("\n\tmarkedCompleted = ").append(getMarkedCompleted()).append("\n\tverificationNeeded = ").append(getVerificationNeeded()).append("\n\tmandatoryFieldsMissing = ").append(getMandatoryFieldsMissing());

    if (getLatestSubmit() != null) {
      sb.append("\n\tlatestSubmit = ").append(getLatestSubmit().toString());
    }
    else {
      sb.append("\n\tlatestSubmit = null");
    }

    sb.append("\n\tverificationInProgress = ").append(getVerificationInProgress());

    sb.append("\n]");

    return sb.toString();
  }

  public boolean verificationFailed() {
    return this.verificationFailed;
  }

  public void setVerificationFailed(boolean verificationFailed) {
    this.verificationFailed = verificationFailed;
  }

  public boolean isPostCompletionDataNotCompleted() {
    return this.postCompletionDataNotCompleted;
  }

  public void setPostCompletionDataNotCompleted(boolean paperBasedVerificationNotCompleted) {
    this.postCompletionDataNotCompleted = paperBasedVerificationNotCompleted;
  }

  public boolean getIllegalPen() {
    return this.illegalPen;
  }

  public void setIllegalPen(boolean illegalPen) {
    this.illegalPen = illegalPen;
  }

  public int getLocked() {
    return this.locked;
  }

  public void setLocked(int locked) {
    this.locked = locked;
  }

  public int getImageMailFailed() {
    return this.imageMailFailed;
  }

  public void setImageMailFailed(int imageMailFailed) {
    this.imageMailFailed = imageMailFailed;
  }

  public int getExportMailFailed() {
    return this.exportMailFailed;
  }

  public void setExportMailFailed(int exportMailFailed) {
    this.exportMailFailed = exportMailFailed;
  }

  public byte getFormStatus() {
    if ((this.markedCompleted == 0) && (this.verificationNeeded == 1) && (this.mandatoryFieldsMissing == -1))
      return 0;

    if ((this.markedCompleted == 0) && (this.verificationNeeded == 1) && (this.mandatoryFieldsMissing == 1))
      return 1;

    if ((this.markedCompleted == 1) && (this.verificationNeeded == 1) && (this.mandatoryFieldsMissing == 0))
      return 2;

    if ((this.markedCompleted == 1) && (this.verificationNeeded == 0) && (this.mandatoryFieldsMissing == 0)) {
      return 3;
    }

    return 4;
  }

  public String getFormStatusAsString()
  {
    if ((this.markedCompleted == 0) && (this.verificationNeeded == 1) && (this.mandatoryFieldsMissing == -1))
      return "Pidget not ticked";

    if ((this.markedCompleted == 0) && (this.verificationNeeded == 1) && (this.mandatoryFieldsMissing == 1))
      return "Missing fields";

    if ((this.markedCompleted == 1) && (this.verificationNeeded == 1) && (this.mandatoryFieldsMissing == 0))
      return "Verification needed";

    if ((this.markedCompleted == 1) && (this.verificationNeeded == 0) && (this.mandatoryFieldsMissing == 0)) {
      return "Completed";
    }

    return "Unknown";
  }

  public boolean getIncorrectFormType()
  {
    return this.incorrectFormType;
  }

  public void setIncorrectFormType(boolean incorrectFormType) {
    this.incorrectFormType = incorrectFormType;
  }
  
  public String getNumeroOcorrencia() {
	return numeroOcorrencia;
  }
 
  public void setNumeroOcorrencia(String numeroOcorrencia) {
	this.numeroOcorrencia = numeroOcorrencia;
  }

}