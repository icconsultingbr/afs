package com.anoto.ash.database;

import java.util.Date;

public class FormCopyPageArea
{
  private int id;
  private String name;
  private long numberPenStrokes;
  private int defaultRenderingHeight;
  private int defaultRenderingWidth;
  private Date timestampStart;
  private Date timestampEnd;
  private FormCopyData formCopyData;


public FormCopyPageArea() { }

  public FormCopyPageArea(int id, String name)
  {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getNumberPenStrokes() {
	return numberPenStrokes;
  }

  public void setNumberPenStrokes(long numberPenStrokes) {
	this.numberPenStrokes = numberPenStrokes;
  }
  
  public int getDefaultRenderingHeight() {
	return defaultRenderingHeight;
  }

  public void setDefaultRenderingHeight(int defaultRenderingHeight) {
	this.defaultRenderingHeight = defaultRenderingHeight;
  }

  public int getDefaultRenderingWidth() {
	return defaultRenderingWidth;
  }

  public void setDefaultRenderingWidth(int defaultRenderingWidth) {
	this.defaultRenderingWidth = defaultRenderingWidth;
  }
  
  public boolean equals(Object o)
  {
    if (this == o)
      return true;

    if (!(o instanceof FormCopyPageArea))
      return false;

    FormCopyPageArea that = (FormCopyPageArea) o;
    return (getName().equals(that.getName()));
  }
  
  public FormCopyData getFormCopyData() {
	return formCopyData;
  }

  public void setFormCopyData(FormCopyData formCopyData) {
	this.formCopyData = formCopyData;
  }
  
  public void setFormCopyId(int formCopyId) {
	  if (this.formCopyData == null) {
		  this.formCopyData = new FormCopyData();
	  }
	  
	  this.formCopyData.setFormCopyId(formCopyId);
  }
  
  public int getFormCopyId() {
	  if (this.formCopyData == null) {
		  return 0;
	  }
	  
	  return this.formCopyData.getFormCopyId();
  }

  public Date getTimestampStart() {
	return timestampStart;
  }

  public void setTimestampStart(Date timestampStart) {
	this.timestampStart = timestampStart;
  }

  public Date getTimestampEnd() {
	return timestampEnd;
  }

  public void setTimestampEnd(Date timestampEnd) {
	this.timestampEnd = timestampEnd;
  }
}