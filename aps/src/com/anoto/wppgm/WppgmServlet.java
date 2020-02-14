package com.anoto.wppgm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;


@SuppressWarnings("unchecked")
public class WppgmServlet extends HttpServlet
{
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);

    File wppgmFolder = new File(WppgmProperties.TEMPFOLDER);
    File wppgmLogFolder = new File(WppgmProperties.LOGFOLDER);
    File wppgmPrintFolder = new File(WppgmProperties.PRINTFOLDER);
    File wppgmPostscriptFolder = new File(WppgmProperties.TEMPFOLDER + "\\" + WppgmCommons.getProperty("instance_name"));
    try
    {
      if (!(wppgmFolder.exists()))
      {
        FileUtils.forceMkdir(wppgmFolder);
      }

      File padPropertiesFile = new File(WppgmProperties.TEMPFOLDER + "\\pads.properties");
      if (!(padPropertiesFile.exists())) {
        padPropertiesFile.createNewFile();
      }

      if (!(wppgmLogFolder.exists()))
      {
        FileUtils.forceMkdir(wppgmLogFolder);
      }
      
      if (!(wppgmPrintFolder.exists()))
      {
        FileUtils.forceMkdir(wppgmPrintFolder);
      }
      
      if (!(wppgmPostscriptFolder.exists()))
      {
        FileUtils.forceMkdir(wppgmPostscriptFolder);
      }
    } catch (IOException ioe) {
      throw new ServletException("The WPPGM environment is not correctly set up!" + ioe.getMessage());
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    try
    {
      getServletContext().log("Received a post request");

      FileItemFactory factory = new DiskFileItemFactory();

      ServletFileUpload upload = new ServletFileUpload(factory);

      List<FileItem> fileItems = upload.parseRequest(request);

      String action = getAction(fileItems);
      PrintWriter responseWriter;
      if (action.equalsIgnoreCase("print")) {
        String printResult = initiatePrint(fileItems);     
        responseWriter = response.getWriter();
        responseWriter.write(printResult);
      }
      else if (action.equalsIgnoreCase("getFormTypeNames")) {
        String formTypeNames = getFormTypeNames(fileItems);

        responseWriter = response.getWriter();
        responseWriter.write(formTypeNames);
      }
      else if (action.equalsIgnoreCase("authorizeUser")) {
        String result = authorizeUser(fileItems);

        responseWriter = response.getWriter();
        responseWriter.write(result);
      }
      else if (action.equalsIgnoreCase("getPrinterNames")) {
        String printerNames = getPrinterNames();

        responseWriter = response.getWriter();
        responseWriter.write(printerNames);
      }
      else if (action.equalsIgnoreCase("getSeq")) {
        String result = getSequence(fileItems);

        responseWriter = response.getWriter();
        responseWriter.write(result);
      }

    }
    catch (Exception e)
    {
      getServletContext().log("ERROR: Forced to handle exception" + e.getMessage());

      response.setContentType("text/plain");
      response.setStatus(400);

      PrintWriter responseWriter = response.getWriter();
      responseWriter.write(e.getMessage());
      e.printStackTrace();
    }
  }

  private String getAction(List<FileItem> fileItems)
  {
    String action = "";

    for (FileItem item : fileItems) {
      if (item.getFieldName().equalsIgnoreCase("action")) {
        action = item.getString();
      }
    }
    getServletContext().log("Post action: " + action);
    return action;
  }

  private String getFormTypeNames(List<FileItem> fileItems) {
    String userName = "";
    String password = "";
    String noDynData = "false";

    for (FileItem item : fileItems) {
      if (item.getFieldName().equalsIgnoreCase("userName")) {
        userName = item.getString();
      }
      if (item.getFieldName().equalsIgnoreCase("password")) {
        password = item.getString();
      }

      if (item.getFieldName().equalsIgnoreCase("noDynData")) {
        noDynData = item.getString();
      }

    }

    getServletContext().log("About to get the names of all form types");
    AshConnector ashConnector = new AshConnector();
    return ashConnector.getFormTypeNames(userName, password, noDynData);
  }

  private String authorizeUser(List<FileItem> fileItems)
  {
    String userName = "";
    String password = "";

    for (FileItem item : fileItems) {
      if (item.getFieldName().equalsIgnoreCase("userName")) {
        userName = item.getString();
      }
      if (item.getFieldName().equalsIgnoreCase("password")) {
        password = item.getString();
      }

    }

    getServletContext().log("About to authorize user with userName= " + userName);
    AshConnector ashConnector = new AshConnector();
    return ashConnector.authorizeUser(userName, password);
  }

  private String getPrinterNames() {
    getServletContext().log("About to get the names of all available printers");
    return WppgmCommons.getAvailablePrinters();
  }

  private String initiatePrint(List<FileItem> fileItems)
    throws Exception
  {
    InputStream xmlStream = null;
    String userName = "";
    String password = "";

    for (FileItem item : fileItems) {
      if (item.getFieldName().equalsIgnoreCase("xmlFile")) {
        xmlStream = item.getInputStream();
      }

      if (item.getFieldName().equalsIgnoreCase("userName")) {
        userName = item.getString();
      }
      if (item.getFieldName().equalsIgnoreCase("password")) {
        password = item.getString();
      }

    }

    getServletContext().log("About to initiate printing");
    //WppgmMain main = new WppgmMain();
    //String resultString = main.initiatePrint(xmlStream, userName, password);
    String resultString = WppgmMain.initiatePrint(xmlStream, userName, password);

    return resultString;
  }
  
  private String getSequence(List<FileItem> fileItems)
  {
    String seqName = "";
    String userName = "";
    String password = "";

    for (FileItem item : fileItems) {
    	if (item.getFieldName().equalsIgnoreCase("seqName")) {
    		seqName = item.getString();
    	}
    	if (item.getFieldName().equalsIgnoreCase("userName")) {
    		userName = item.getString();
        }
    	if (item.getFieldName().equalsIgnoreCase("password")) {
            password = item.getString();
    	}
    }

    getServletContext().log("Get next value for seqName = " + seqName);
    AshConnector ashConnector = new AshConnector();
    return ashConnector.getSequence(seqName, userName, password);
  }
  
}