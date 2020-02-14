package com.anoto.wppgm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.anoto.afs.dynamicdata.DynamicDataDecorator;
import com.anoto.afs.dynamicdata.xml.Dynamicdata;

public class WppgmMain {

	public static synchronized String initiatePrint(InputStream xmlStream, String userName, String password)
			throws WppgmException {
		String retorno = "";

		try {
			boolean splitPdf = true;

			byte[] xmlBytes = IOUtils.toByteArray(xmlStream);
			ByteArrayInputStream xmlByteStream = new ByteArrayInputStream(xmlBytes);

			WppgmLogger.logFine("About to read the contents of the received xml file");

			DynamicDataDecorator dynDataDecorator = new DynamicDataDecorator();
			dynDataDecorator.setInputStream(xmlByteStream);
			Dynamicdata dynData = dynDataDecorator.getDynamicData();

			XmlHandler xmlHandler = new XmlHandler(dynData);

			String formTypeName = xmlHandler.getFormType();
			WppgmLogger.logFine("Form type name: " + formTypeName);

			String printerName = xmlHandler.getChosenPrinterName();
			WppgmLogger.logFine("Printer: " + printerName);

			int nbrOfFormCopies = xmlHandler.getNumberOfForms();
			WppgmLogger.logFine("Number of forms: " + nbrOfFormCopies);

			File xmlFile = new File(WppgmProperties.TEMPFOLDER + formTypeName + System.nanoTime() + "_"
					+ (int) Math.ceil((Math.random() * 1000000)) + ".xml");
			xmlFile.createNewFile();

			xmlByteStream = new ByteArrayInputStream(xmlBytes);

			FileOutputStream fileOutPutStream = new FileOutputStream(xmlFile.getPath());
			IOUtils.copy(xmlByteStream, fileOutPutStream);

			xmlStream.close();
			fileOutPutStream.close();

			WppgmLogger.logFine("About to contact the ASH");
			AshConnector ashConnector = new AshConnector();
			dynData = ashConnector.completeXml(xmlFile, userName, password);

			xmlHandler = new XmlHandler(dynData);

			String[] endPageAddresses = xmlHandler.getEndPageAddresses();

			String psFilePathList = "";
			for (int i = 0; i < endPageAddresses.length; ++i) {
				String padPath = "";
				String pdfPath = "";
				String currentEndPageAddress = endPageAddresses[i];

				padPath = WppgmCommons.findPadPath(currentEndPageAddress);

				if ((padPath.length() == 0) || (xmlHandler.isPadUpdated(currentEndPageAddress))) {
					WppgmLogger.logFine("We have to get the PAD file for the form with end page address: "
							+ currentEndPageAddress + " from the ASH");

					padPath = ashConnector.getPad(formTypeName, currentEndPageAddress, userName, password).getPath();

					Long padTimeStampFromAsh = xmlHandler.getPadUpdateTimeStamp(currentEndPageAddress);

					String padLicenseAddress = WppgmCommons.findPadLicenseAddress(currentEndPageAddress);

					WppgmCommons.setPadTimeStamp(padLicenseAddress, padTimeStampFromAsh);

					pdfPath = WppgmCommons.findPdfPath(formTypeName);

					if (pdfPath.length() == 0) {
						WppgmLogger
								.logFine("We have to get the Pdf file for form type " + formTypeName + " from the ASH");
						splitPdf = true;

						pdfPath = ashConnector.getPdf(formTypeName, userName, password).getPath();
					} else {
						WppgmLogger.logFine("We already have the Pdf file for form type " + formTypeName);
						splitPdf = false;
					}
				} else {
					WppgmLogger.logFine("We already have the PAD and PDF files for the form with end page address: "
							+ currentEndPageAddress);
					splitPdf = false;

					padPath = WppgmCommons.findPadPath(currentEndPageAddress);
					WppgmLogger.logFine("Using PAD: " + padPath);
					pdfPath = WppgmCommons.findPdfPath(formTypeName);
					WppgmLogger.logFine("Using Pdf: " + pdfPath);
				}

				psFilePathList = PPGMHandler.print(padPath, pdfPath, currentEndPageAddress, printerName, dynData,
						formTypeName, splitPdf) + " ";
			}

			retorno = new File(psFilePathList).getName();

		} catch (Exception e) {
			e.printStackTrace();
			handleException(e);
		}

		return retorno;
	}

	private static void handleException(Exception e) throws WppgmException {
		WppgmLogger.logSevere("WPPGM forced to handle exception: " + e.getMessage());
		throw new WppgmException(e.getMessage(), e);
	}

}