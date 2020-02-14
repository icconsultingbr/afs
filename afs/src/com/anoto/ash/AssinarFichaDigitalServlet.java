package com.anoto.ash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
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
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;


/**
 * AshRenderServlet
 *
 */
@SuppressWarnings("rawtypes")
 public class AssinarFichaDigitalServlet extends HttpServlet {
	 
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String numeroOcorrencia = request.getParameter("id");
		
		try {
			FormCopyData formCopy = getFormCopy(numeroOcorrencia);
			
			if (formCopy == null) {
				throw new Exception("Registro não encontrado.");
			}
			
			FormCopyRenderer renderer = new FormCopyRenderer();
		    String imagePath = renderer.createImagesForUI(formCopy, numeroOcorrencia);
		    
		    File imageFile = new File(signPdf(imagePath));
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
	
	private String signPdf(String file) throws IOException, DocumentException, Exception {
		String fileKey = "C:\\apps\\ks\\ks.p12";
		String fileKeyPassword = "changeit";

		KeyStore ks = KeyStore.getInstance("pkcs12");
		ks.load(new FileInputStream(fileKey), fileKeyPassword.toCharArray());
		String alias = (String) ks.aliases().nextElement();
		PrivateKey key = (PrivateKey) ks.getKey(alias, fileKeyPassword.toCharArray());
		Certificate[] chain = ks.getCertificateChain(alias);

		PdfReader pdfReader = new PdfReader((new File(file)).getAbsolutePath());
		File outputFile = new File(file.replaceFirst("\\.pdf", "\\_SIGN.pdf"));

		PdfStamper pdfStamper = PdfStamper.createSignature(pdfReader, null, '\0', outputFile);
		PdfSignatureAppearance sap = pdfStamper.getSignatureAppearance();
		sap.setCrypto(key, chain, null, PdfSignatureAppearance.SELF_SIGNED);
		sap.setReason("Assinatura Digital");
		sap.setLocation("BR");
		sap.setVisibleSignature(new Rectangle(350, 50, 610, 500), 1, "SIGNATURE");

		pdfStamper.setFormFlattening(true);
		pdfStamper.close();

		return outputFile.getAbsolutePath();
	}

}