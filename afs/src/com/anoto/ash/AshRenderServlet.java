package com.anoto.ash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.utils.FormCopyRenderer;


/**
 * AshRenderServlet
 *
 */
@SuppressWarnings("rawtypes")
 public class AshRenderServlet extends HttpServlet {
	 
	private static final long serialVersionUID = 1L;
	
	private int pageNumber = 0;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String idFormcopies = request.getParameter("idFormcopies");
		
		try {
		    AshFormControl formControl = AshFormControl.getInstance();
		    FormCopyData formCopy = formControl.getFormCopy(Integer.parseInt(idFormcopies));
			
			FormCopyRenderer renderer = new FormCopyRenderer();
			File rootFolder = new File(AshProperties.EXPORT_FOLDER + "\\" + formCopy.getFormType().getFormTypeName() + "\\" + AshCommons.getCurrentDateAndTime("yyyyMMdd"));
		    if (!(rootFolder.exists())) {
		    	FileUtils.forceMkdir(rootFolder);
		    }
	
			List tmpImages = renderer.createImagesForUI(formCopy, idFormcopies, this.pageNumber, rootFolder.getPath());
		    String imagePath = (String) tmpImages.get(0);
		    
		    aguardarGeracaoDaImagem(imagePath);
		    
		    response.setContentType("text/plain");
			ServletOutputStream outputStream = response.getOutputStream(); 
			outputStream.print(imagePath);
			outputStream.flush();
			outputStream.close();
			
		} catch (Exception e) {
			response.setContentType("text/plain");
			PrintWriter responseWriter = response.getWriter();
			e.printStackTrace();
			responseWriter.write("Ficha de atendimento com o Id '" + idFormcopies + "' não econtrada!");
		}
	}

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
	
	
}