package com.anoto.ash.database;

public class BackgroundFile
{
  private int Id;
  private volatile byte[] backgroundFile;
  private String fileName;
  private FormTypeData formType;

  public BackgroundFile()
  {
    this.Id = -1;

    this.fileName = "";
  }

  public int getId() {
    return this.Id;
  }

  public void setId(int Id) {
    this.Id = Id;
  }

  public byte[] getBackgroundFile() {
	return backgroundFile;
  }

  public void setBackgroundFile(byte[] backgroundFile) {
	this.backgroundFile = backgroundFile;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public FormTypeData getFormType() {
    return this.formType;
  }

  public void setFormType(FormTypeData formType) {
    this.formType = formType;
  }
}