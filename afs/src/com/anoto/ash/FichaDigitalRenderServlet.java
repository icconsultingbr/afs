package com.anoto.ash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.utils.FormCopyRenderer;


/**
 * AshRenderServlet
 *
 */
@SuppressWarnings("rawtypes")
 public class FichaDigitalRenderServlet extends HttpServlet {
	 
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//String username = request.getParameter("username");
	    //String password = request.getParameter("password");
		String numeroOcorrencia = request.getParameter("id");
		
		try {
			//String result = authorizeUser(username, password);

		    //if (!(result.equalsIgnoreCase("authorized"))) {
		    //	throw new IllegalAccessException("You are unauthorized to perform this action");
		    //}
			
			FormCopyData formCopy = getFormCopy(numeroOcorrencia);
			
			if (formCopy == null) {
				throw new Exception("Registro não encontrado.");
			}
			
			FormCopyRenderer renderer = new FormCopyRenderer();
			//File rootFolder = new File(AshProperties.EXPORT_FOLDER + "\\" + formCopy.getFormType().getFormTypeName() + "\\" + AshCommons.getCurrentDateAndTime("yyyyMMdd"));
		    //if (!(rootFolder.exists())) {
		    //	FileUtils.forceMkdir(rootFolder);
		    //}
	
			//List tmpImages = renderer.createImagesForUI(formCopy, numeroOcorrencia, this.pageNumber, rootFolder.getPath());
		    //String imagePath = (String) tmpImages.get(0);
		    String imagePath = renderer.createImagesForUI(formCopy, numeroOcorrencia);
		    
		    //aguardarGeracaoDaImagem(imagePath);
		    
		    File imageFile = new File(imagePath);
		    String mimeType = Files.probeContentType(imageFile.toPath());
		    response.setContentType(mimeType);
		    int len = (int) imageFile.length();
		    response.setContentLength(len);
	        byte[] b = new byte[len];
	        FileInputStream f = new FileInputStream(imageFile);
	        f.read(b);
	        ServletOutputStream o = response.getOutputStream();
	        o.write(b, 0, len);
	        o.flush();
	        o.close();
	        f.close();
			
		} catch (Exception e) {
			response.setContentType("text/plain");
			PrintWriter responseWriter = response.getWriter();
			e.printStackTrace();
			responseWriter.write("Erro ao gerar a imagem da ficha!");
		}
	}
	
	private FormCopyData getFormCopy(String numeroOcorrencia) {
	    FormCopyData formCopyData = null;
	    FormCopyData formCopy = new FormCopyData();
	    formCopy.setNumeroOcorrencia(numeroOcorrencia);

	    AshFormControl formControl = AshFormControl.getInstance();
	    List matchingFormCopies = formControl.searchFormCopies(formCopy);

	    Iterator formCopyIterator = matchingFormCopies.iterator();

	    if (formCopyIterator.hasNext()) {
	    	formCopyData = (FormCopyData)formCopyIterator.next();
	    }

	    return formCopyData;
	}
	
	/*
	private void aguardarGeracaoDaImagem(String imagePath) throws InterruptedException {
	    while(!(new File(imagePath)).canRead()) {
	    	Thread.sleep(500L);
	    }
	    long initImageSize = (new File(imagePath)).length();
	    int i = 0;
	    while (++i <= 20) {
	    	Thread.sleep(3000L);
	    	long imageSize = (new File(imagePath)).length();
	    	if (initImageSize == imageSize) {
	    		break;
	    	}
	    	initImageSize = imageSize;
	    }
	}
	*/
	
}