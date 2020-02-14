package com.anoto.ash;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;


@SuppressWarnings({"rawtypes","unchecked"})
public class AshPrintServlet extends HttpServlet {
	
 private static final long serialVersionUID = 1L;
	
  /**
    * @see Servlet#init(ServletConfig)
    */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try
    {
      ServletOutputStream responseStream;
      getServletContext().log("Received post command");

      String action = "";
      List fileItems = null;

      FileItemFactory factory = new DiskFileItemFactory();

      ServletFileUpload upload = new ServletFileUpload(factory);

      fileItems = upload.parseRequest(request);

      String result = "authorized";//authorizeUser(fileItems);

      if (!(result.equalsIgnoreCase("authorized"))) {
        throw new IllegalAccessException("You are unauthorized to perform this action");
      }

      action = getAction(fileItems);

      if ((action.equalsIgnoreCase("getXml")) && (action != null) && (action.length() > 0)) {
        InputStream xmlFile = null;
        xmlFile = getXml(fileItems);

        responseStream = response.getOutputStream();
        response.setContentType("application/xml");
        IOUtils.copy(xmlFile, responseStream);
      }

      else if (action.equalsIgnoreCase("getPdf")) {
        InputStream pdfFile = null;

        pdfFile = getPdf(fileItems);

        responseStream = response.getOutputStream();
        IOUtils.copy(pdfFile, responseStream);
      }
      else if (action.equalsIgnoreCase("getPad")) {
        InputStream padFile = null;
        padFile = getPad(fileItems);

        responseStream = response.getOutputStream();
        IOUtils.copy(padFile, responseStream);
      }
      else if (action.equalsIgnoreCase("getSeq")) {
    	BigInteger seq = getSequence(fileItems);
    	
    	response.setContentType("text/plain");
        PrintWriter responseWriter = response.getWriter();
        responseWriter.write(seq.toString());
      } else {
        PrintWriter responseWriter;
        if (action.equalsIgnoreCase("authorizeUser")) {
          String authorizationResult = "";

          authorizationResult = authorizeUser(fileItems);

          response.setContentType("text/plain");
          responseWriter = response.getWriter();
          responseWriter.write(authorizationResult);
        }
        else {
          String names = getFormTypeNames(fileItems);

          System.out.println("Names:" + names);
          response.setContentType("text/plain");
          responseWriter = response.getWriter();

          responseWriter.write(names);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      handleException(e);
      response.setContentType("text/plain");
      response.setStatus(400);
      PrintWriter responseWriter = response.getWriter();
      responseWriter.write(e.getMessage());
    }
  }

  private String getAction(List<FileItem> fileItems)
  {
    String action = "";

    for (Iterator i = fileItems.iterator(); i.hasNext(); ) { 
      FileItem item = (FileItem)i.next();
      if (item.getFieldName().equalsIgnoreCase("action"))
        action = item.getString();
    }

    getServletContext().log("Post action: " + action);
    return action;
  }

  private InputStream getXml(InputStream inputStream)
    throws Exception
  {
    getServletContext().log("Received an xml file for completion");
    InputStream xmlData = null;
    xmlData = AshPrintControl.getInstance().getDynData(inputStream);

    return xmlData;
  }
  
  /*
  private HashMap getXml(InputStream inputStream)
	throws Exception
  {
    getServletContext().log("Received an xml file for completion");
	return AshPrintControl.getInstance().getDynData(inputStream);
  }
  */

  private InputStream getXml(List<FileItem> fileItems) throws Exception {
    InputStream xmlStream = null;

    for (Iterator iter = fileItems.iterator(); iter.hasNext(); ) { 
    	FileItem item = (FileItem) iter.next();
    	if (item.getFieldName().equalsIgnoreCase("xmlFile")) {
    		xmlStream = item.getInputStream();
    	}
    }

    return getXml(xmlStream);
  }
  
  /*
  private HashMap getXml(List<FileItem> fileItems) throws Exception {
    InputStream xmlStream = null;

    for (Iterator iter = fileItems.iterator(); iter.hasNext(); ) { 
	  FileItem item = (FileItem) iter.next();
	  if (item.getFieldName().equalsIgnoreCase("xmlFile"))
	    xmlStream = item.getInputStream();
	}
	    
	return getXml(xmlStream);
  }
  */

  private InputStream getPad(List<FileItem> fileItems) throws Exception {
	  String formTypeName = "";
	  String endPageAddress = "";

	  for (Iterator iter = fileItems.iterator(); iter.hasNext(); ) { 
		  FileItem item = (FileItem) iter.next();
		  if (item.getFieldName().equalsIgnoreCase("formTypeName")) {
			  formTypeName = item.getString();
			  //System.out.println("Form type name:" + formTypeName);
		  } else if (item.getFieldName().equalsIgnoreCase("endPageAddress")) {
			  endPageAddress = item.getString();
			  //System.out.println("End PA:" + endPageAddress);
		  }
	  }

	  return getPad(formTypeName, endPageAddress);
  }

  private InputStream getPad(String formTypeName, String endPageAddress) throws Exception
  {
    getServletContext().log("About to find a PAD that covers page " + endPageAddress + ".Form type =  " + formTypeName);
    AshPrintControl printControl = AshPrintControl.getInstance();
    InputStream padStream = printControl.getPad(formTypeName, endPageAddress);
    return padStream;
  }

  private InputStream getPdf(List<FileItem> fileItems)
    throws Exception
  {
    String formTypeName = "";

    for (Iterator i$ = fileItems.iterator(); i$.hasNext(); ) { FileItem item = (FileItem)i$.next();
      if (item.getFieldName().equalsIgnoreCase("formTypeName")) {
        formTypeName = item.getString();
      }

    }

    return getPdf(formTypeName);
  }

  private InputStream getPdf(String formTypeName) throws Exception {
    getServletContext().log("About to find the pdf for form type: " + formTypeName);
    AshPrintControl printControl = AshPrintControl.getInstance();
    InputStream pdfStream = printControl.getPdf(formTypeName);

    return pdfStream;
  }

  private String getFormTypeNames(List<FileItem> fileItems) throws SQLException, IOException {
    getServletContext().log("About to get the names of all form types");
    AshPrintControl printControl = AshPrintControl.getInstance();
    boolean onlyFormTypesWithoutDynData = false;

    for (Iterator iter = fileItems.iterator(); iter.hasNext(); ) { 
      FileItem item = (FileItem) iter.next();
      if (item.getFieldName().equalsIgnoreCase("noDynData")) {
        String noDynData = item.getString();

        if ((noDynData != null) && (noDynData.equalsIgnoreCase("true")))
          onlyFormTypesWithoutDynData = true;
      }

    }

    String formTypeNames = printControl.getFormTypeNames(onlyFormTypesWithoutDynData);

    return formTypeNames;
  }

  private String authorizeUser(List<FileItem> fileItems)
  {
    String userName = "";
    String password = "";

    for (Iterator iter = fileItems.iterator(); iter.hasNext(); ) { 
      FileItem item = (FileItem) iter.next();
      if (item.getFieldName().equalsIgnoreCase("userName"))
        userName = item.getString();

      if (item.getFieldName().equalsIgnoreCase("password")) {
        password = item.getString();
      }

    }

    return authorizeUser(userName, password);
  }

  private String authorizeUser(String userName, String password)
  {
    String result = "";

    getServletContext().log("About to authorize a user with userName = " + userName);
    AshPrintControl printControl = AshPrintControl.getInstance();
    result = printControl.authorizeUser(userName, password);
    return result;
  }

  private void handleException(Exception e) {
    MailControl mailControl = MailControl.getInstance();
    if (e instanceof AshException) {
      AshException ashException = (AshException)e;
      Exception wrappedException = ashException.getWrappedException();

      getServletContext().log("Ash Print Servlet forced to handle exception: " + wrappedException.toString());
      mailControl.sendAdminMail("ASH got exception! Provided error message: " + wrappedException.toString(), AshProperties.adminErrorMailSubject, null);
    } else if (e instanceof IllegalAccessException)
    {
      getServletContext().log("Ash Print Servlet forced to handle exception: " + e.toString());
    } else {
      getServletContext().log("Ash Print Servlet forced to handle exception: " + e.toString());
      mailControl.sendAdminMail("ASH got exception! Exception error message: " + e.toString(), AshProperties.adminErrorMailSubject, null);
    }
  }
  
  public String iframeHtmlCode(HttpServletRequest request, String printResult) {
	return "<iframe src='http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" + printResult + "' width='0' height='0'></iframe>";
  }
  
  private BigInteger getSequence(List<FileItem> fileItems) throws Exception
  {
    String seqName = "seq_ficha_contingencia";

    for (FileItem fileItem : fileItems) {
    	if (fileItem.getFieldName().equalsIgnoreCase("seqName")) {
    		seqName = fileItem.getString();
    	}
	}
    
    AshPrintControl printControl = AshPrintControl.getInstance();
    BigInteger seq = printControl.getNextValSeq(seqName);
    return seq;
  }
  
}