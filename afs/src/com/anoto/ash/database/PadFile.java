package com.anoto.ash.database;

import java.util.Date;


public class PadFile
{
  private int Id;
  private volatile byte[] padFile;
  private String padLicenseAddress;
  private FormTypeData formType;
  private Date lastModified;

  public PadFile()
  {
    this.Id = -1;

    this.padLicenseAddress = "";
  }

  public byte[] getPadFile() {
    return padFile;
  }

  public void setPadFile(byte[] padFile) {
	this.padFile = padFile;
  }
  
  public String getPadLicenseAddress() {
    return this.padLicenseAddress;
  }

  public void setPadLicenseAddress(String padLicenseAddress) {
    this.padLicenseAddress = padLicenseAddress;
  }

  public FormTypeData getFormType() {
    return this.formType;
  }

  public void setFormType(FormTypeData formType) {
    this.formType = formType;
  }

  public int getId() {
    return this.Id;
  }

  public void setId(int Id) {
    this.Id = Id;
  }

  public Date getLastModified() {
    return this.lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public boolean equals(Object o)
  {
    if (this == o)
      return true;

    if (!(o instanceof PadFile))
      return false;

    PadFile that = (PadFile)o;
    return (getId() == that.getId());
  }
}