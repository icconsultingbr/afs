package com.anoto.wppgm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.anoto.afs.dynamicdata.DynamicDataDecorator;
import com.anoto.afs.dynamicdata.xml.Dynamicdata;
import com.anoto.api.util.PadFileUtility;

import httplibrary.HttpPoster;

public class AshConnector
{
  public Dynamicdata completeXml(File xmlFile, String userName, String password)
    throws IllegalStateException
  {
    Dynamicdata dynData = new Dynamicdata();
    try
    {
      HttpPoster poster = new HttpPoster(WppgmCommons.getProperty("ash_url"));

      poster.addParameter("userName", userName);
      poster.addParameter("password", password);
      poster.addParameter("action", "getXml");
      poster.addFile("xmlFile", xmlFile);

      WppgmLogger.logFine("About to post a xml file to the ASH for completion");
      InputStream responseStream = poster.post();

      if (responseStream != null)
      {
        WppgmLogger.logFine("Response received, status: " + poster.getStatusCode());
        if (poster.getStatusCode() == 200) {
          DynamicDataDecorator dynDataDecorator = new DynamicDataDecorator();
          dynDataDecorator.setInputStream(responseStream);

          dynData = dynDataDecorator.getDynamicData();
        } else {
          handleError(responseStream);
        }

      }

      poster.closeConnection();
      
      FileUtils.forceDelete(xmlFile);
    }
    catch (Exception e)
    {
      throw new IllegalStateException(e.getMessage());
    }

    return dynData;
  }

  public File getPdf(String formTypeName, String userName, String password)
  {
    File pdfFile = null;
    try
    {
      HttpPoster poster = new HttpPoster(WppgmCommons.getProperty("ash_url"));

      poster.addParameter("userName", userName);
      poster.addParameter("password", password);
      poster.addParameter("action", "getPdf");
      poster.addParameter("formTypeName", formTypeName);

      WppgmLogger.logFine("About to get the pdf for formtype " + formTypeName + " from the ASH");
      InputStream responseStream = poster.post();

      if (responseStream != null) {
        WppgmLogger.logFine("Response received, status: " + poster.getStatusCode());
        if (poster.getStatusCode() == 200)
        {
          byte[] pdfBytes = IOUtils.toByteArray(responseStream);
          ByteArrayInputStream pdfByteStream = new ByteArrayInputStream(pdfBytes);

          synchronized (this)
          {
            pdfFile = new File(WppgmProperties.TEMPFOLDER + formTypeName + ".pdf");
            pdfFile.createNewFile();

            FileOutputStream pdfStream = new FileOutputStream(pdfFile);
            IOUtils.copy(pdfByteStream, pdfStream);
            pdfStream.close();
            pdfByteStream.close();
          }
        }
        else {
          handleError(responseStream);
        }

      }

      poster.closeConnection();
    }
    catch (Exception e)
    {
    }
    return pdfFile;
  }

  public File getPad(String formTypeName, String endPageAddress, String userName, String password)
  {
    File padFile = null;
    try
    {
      HttpPoster poster = new HttpPoster(WppgmCommons.getProperty("ash_url"));

      poster.addParameter("userName", userName);
      poster.addParameter("password", password);
      poster.addParameter("action", "getPad");
      poster.addParameter("formTypeName", formTypeName);
      poster.addParameter("endPageAddress", endPageAddress);

      WppgmLogger.logFine("About to get the PAD that covers page " + endPageAddress + " from the ASH.Form type name = " + formTypeName);
      InputStream responseStream = poster.post();

      if (responseStream != null) {
        WppgmLogger.logFine("Response received, status: " + poster.getStatusCode());
        if (poster.getStatusCode() == 200)
        {
          byte[] padBytes = IOUtils.toByteArray(responseStream);
          ByteArrayInputStream padByteStream = new ByteArrayInputStream(padBytes);

          synchronized (this)
          {
            String padFileName = PadFileUtility.getLicenseAddressFromPad(padByteStream).replaceAll("[*]", "X") + ".pad";

            padFile = new File(WppgmProperties.TEMPFOLDER + padFileName);
            padFile.createNewFile();

            padByteStream = new ByteArrayInputStream(padBytes);
            FileOutputStream padStream = new FileOutputStream(padFile);
            IOUtils.copy(padByteStream, padStream);
            padStream.close();
            padByteStream.close();
          }
        }
        else {
          handleError(responseStream);
        }

      }

      poster.closeConnection();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return padFile;
  }

  public String getFormTypeNames(String userName, String password, String noDynData)
  {
    String formTypeNames = "";
    try
    {
      HttpPoster poster = new HttpPoster(WppgmCommons.getProperty("ash_url"));

      poster.addParameter("noDynData", noDynData);
      poster.addParameter("password", password);
      poster.addParameter("userName", userName);
      poster.addParameter("action", "getFormTypeNames");
      WppgmLogger.logFine("About to get the names of all form types from the ASH");
      InputStream responseStream = poster.post();

      if (responseStream != null) {
        WppgmLogger.logFine("Response received, status: " + poster.getStatusCode());
        if (poster.getStatusCode() == 200) {
          byte[] responseBytes = new byte[responseStream.available()];

          while (responseStream.available() > 0) {
            responseStream.read(responseBytes);

            formTypeNames = new String(responseBytes);
            System.out.println("AshConnector,names: " + formTypeNames);
          }
        }
        else
        {
          handleError(responseStream);
        }

      }

      poster.closeConnection();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return formTypeNames;
  }

  public String authorizeUser(String userName, String password)
  {
    String result = "";
    try
    {
      HttpPoster poster = new HttpPoster(WppgmCommons.getProperty("ash_url"));

      poster.addParameter("action", "authorizeUser");
      poster.addParameter("userName", userName);
      poster.addParameter("password", password);

      WppgmLogger.logFine("About to send user info to the ASH for authorization. UserName: " + userName);
      InputStream responseStream = poster.post();

      if (responseStream != null) {
        WppgmLogger.logFine("Response received, status: " + poster.getStatusCode());

        if (poster.getStatusCode() == 200) {
          byte[] responseBytes = new byte[responseStream.available()];

          while (responseStream.available() > 0) {
            responseStream.read(responseBytes);

            result = new String(responseBytes);
            System.out.println("Login result: " + result);
          }
        }
        else {
          handleError(responseStream);
        }

      }

      poster.closeConnection();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  private void handleError(InputStream responseStream) throws IOException
  {
    byte[] errorResponse = new byte[responseStream.available()];
    responseStream.read(errorResponse);
    String errorMessage = new String(errorResponse);
    WppgmLogger.logSevere("Forced to handle error. Message: " + errorMessage);
    throw new IllegalStateException(errorMessage);
  }
  
  public String getSequence(String seqName, String userName, String password)
  {
    String result = "";
    try
    {
      HttpPoster poster = new HttpPoster(WppgmCommons.getProperty("ash_url"));

      poster.addParameter("action", "getSeq");
      poster.addParameter("seqName", seqName);
      poster.addParameter("userName", userName);
      poster.addParameter("password", password);

      WppgmLogger.logFine("Get next value sequence. SeqName: " + seqName);
      InputStream responseStream = poster.post();

      if (responseStream != null) {
        WppgmLogger.logFine("Response received, status: " + poster.getStatusCode());

        if (poster.getStatusCode() == 200) {
          byte[] responseBytes = new byte[responseStream.available()];

          while (responseStream.available() > 0) {
            responseStream.read(responseBytes);

            result = new String(responseBytes);
          }
        }
        else {
          handleError(responseStream);
        }

      }

      poster.closeConnection();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }
}


