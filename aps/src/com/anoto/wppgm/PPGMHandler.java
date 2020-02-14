package com.anoto.wppgm;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;

import com.anoto.afs.dynamicdata.xml.Dynamicdata;
import com.anoto.api.core.PadFileUtility;
import com.anoto.api.util.PageAddress;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfIndirectObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;


public class PPGMHandler {
	private static String pdfFilePath;
	private static String padFilePath;
	private static String[] addresses;
	private static String splitPdfString;
	// private static String chosenPrinter;
	// private static String printerProfilesFolder = "";
	private static boolean useAdditionalSwitches = false;
	private static String commandLineSwitches = "";
	private static Dynamicdata dynData;
	private static String formTypeName;
	private static String osFontFolder;
	private static boolean useLimitedColors;

	private static int calculatePageIndex(int existingPageIndex) {
		int nbrOfFormPages = 1;
		try {
			FileInputStream padStream = new FileInputStream(padFilePath);
			nbrOfFormPages = PadFileUtility.getPageAddresses(padStream).length;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (existingPageIndex % nbrOfFormPages);
	}

	private static void setTextColor(Dynamicdata.Settings.Fields.Field.Text textSettings, PPGMAdapter ppgm,
			long anotoDocPointer) {
		try {
			Field colorField = Color.class.getDeclaredField(textSettings.getColor().toUpperCase());
			Color color = (Color) colorField.get(null);

			int red = color.getRed();
			int green = color.getGreen();
			int blue = color.getBlue();

			// WppgmLogger.logFine("Changing color to " + textSettings.getColor());
			ppgm.nativeChangeColor(anotoDocPointer, red, green, blue);
		} catch (Exception e) {
			WppgmLogger.logSevere("Got exception when trying to set color!. Mesage: " + e.getMessage());
		}
	}

	private static void resetTextColor(PPGMAdapter ppgm, long anotoDocPointer) {
		// WppgmLogger.logFine("Reseting color to black (0,0,0)");
		ppgm.nativeChangeColor(anotoDocPointer, 0, 0, 0);
	}

	private static void init(String padPath, String pdfPath, String submittedAddress, String printer,
			Dynamicdata dynamicData, String formType, boolean pdfSplit) {
		// WppgmLogger.log("About to print a form from " + formTypeName);
		pdfFilePath = pdfPath;
		// WppgmLogger.logFine("Pdf file path: " + pdfFilePath);
		padFilePath = padPath;
		// WppgmLogger.logFine("PAD file path: " + padFilePath);

		addresses = getFormPages(submittedAddress);

		commandLineSwitches = " --resolution 600";
		// chosenPrinter = printer;
		// WppgmLogger.logFine("Chosen printer: " + chosenPrinter);
		dynData = dynamicData;
		formTypeName = formType;
		osFontFolder = System.getenv("windir") + "\\Fonts\\";

		File pdfFile = new File(pdfFilePath);
		// splitPdfString = "gswin32c.exe -dNOPAUSE -dBATCH -dSAFER -sDEVICE#pswrite
		// -dFirstPage##XX -dLastPage##XX -dLanguageLevel#2 -sFONTPATH#" + osFontFolder
		// + " -dASCII85EncodePages#true -r2400 -q -sOutputFile#" + pdfFile.getParent()
		// + "\\" + formTypeName + "_page_#XX.eps " + pdfFilePath;
		splitPdfString = "gswin32c.exe -dNOPAUSE -dBATCH -dSAFER -sDEVICE#pswrite -dFirstPage##XX -dLastPage##XX -dLanguageLevel#3 -sFONTPATH#"
				+ osFontFolder + " -dASCII85EncodePages#true -r300 -q -sOutputFile#" + pdfFile.getParent() + "\\"
				+ formTypeName + "_page_#XX.eps " + pdfFilePath;

		// printerProfilesFolder = WppgmCommons.getProperty("printer_profiles_folder");

		// WppgmLogger.logFine("Using printer profiles from: " + printerProfilesFolder);
		useLimitedColors = false;
	}

	// public static synchronized void print(String padFilePath, String pdfFilePath,
	// String submittedPageAddress, String chosenPrinter, Dynamicdata dynData,
	// String formTypeName, boolean splitPdf)
	public static synchronized String print(String padFilePath, String pdfFilePath, String submittedPageAddress,
			String chosenPrinter, Dynamicdata dynData, String formTypeName, boolean splitPdf) {
		String retorno = "";
		String numeroAtendimento = "";
		String numeroSUS = "";
		String numeroCNES = "";

		try {
			init(padFilePath, pdfFilePath, submittedPageAddress, chosenPrinter, dynData, formTypeName, splitPdf);
			File pdfFile = new File(pdfFilePath);

			if (splitPdf) {
				splitPdf();
			}

			PPGMAdapter ppgm = new PPGMAdapter();

			long newAnotoDocPointer = ppgm.nativeCreateAnotoDoc();

			long existingAnotoDocPointer = ppgm.nativeCreateAnotoDocFromFile(padFilePath);

			// WppgmLogger.logFine("Setting pattern license");
			long licensePointer = ppgm.nativeGetPatternLicense(existingAnotoDocPointer);

			ppgm.nativeSetPatternLicense(newAnotoDocPointer, licensePointer);

			for (int i = 0; i < addresses.length; ++i) {
				String address = addresses[i];
				// WppgmLogger.log("\r\nAbout to handle page with address: " + address);
				
				PageAddress currentPage = new PageAddress(address);
				int pageIndex = calculatePageIndex(currentPage.getPage());

				FileInputStream padStream = new FileInputStream(padFilePath);
				String[] modulatedAddresses = PadFileUtility.getPageAddresses(padStream);
				String tmpAddress = modulatedAddresses[pageIndex];

				long modulatedExistingPagePointer = ppgm.nativeGetPage(existingAnotoDocPointer, tmpAddress);

				float pageWidth = ppgm.nativeGetPageWidth(modulatedExistingPagePointer);
				// WppgmLogger.logFine("Page width:" + pageWidth);
				float pageHeight = ppgm.nativeGetPageHeight(modulatedExistingPagePointer);
				// WppgmLogger.logFine("Page height:" + pageHeight);

				long newPagePointer = ppgm.nativeCreatePage(newAnotoDocPointer, address, address, pageWidth,
						pageHeight);

				// WppgmLogger.logFine("Setting page background");
				ppgm.nativeSetPageBackground(newPagePointer,
						pdfFile.getParent() + "\\" + formTypeName + "_page_" + (i + 1) + ".eps");

				// WppgmLogger.logFine("About to handle drawing areas");
				int nbrOfDrawingAreasOnPage = ppgm.nativeGetNumberOfDrawingAreasOnPage(modulatedExistingPagePointer);
				// WppgmLogger.logFine("Found " + nbrOfDrawingAreasOnPage + " drawing areas on
				// the page");
				for (int k = 0; k < nbrOfDrawingAreasOnPage; ++k) {
					long drawingAreaPointer = ppgm.nativeGetDrawingArea(modulatedExistingPagePointer, k);

					String drawingAreaName = ppgm.nativeGetDrawingAreaName(drawingAreaPointer);
					float drawingAreaLeft = ppgm.nativeGetDrawingAreaLeft(drawingAreaPointer);
					float drawingAreaTop = ppgm.nativeGetDrawingAreaTop(drawingAreaPointer);
					float drawingAreaWidth = ppgm.nativeGetDrawingAreaWidth(drawingAreaPointer);
					float drawingAreaHeight = ppgm.nativeGetDrawingAreaHeight(drawingAreaPointer);

					ppgm.nativeCreateDrawingArea(newPagePointer, drawingAreaName, drawingAreaTop, drawingAreaLeft,
							drawingAreaWidth, drawingAreaHeight);
					// System.out.println("Nbr of drawing areas found: " + nbrOfDrawingAreasOnPage);

					// WppgmLogger.logFine("About to handle user areas");

					int nbrOfUserAreasInDrawingArea = ppgm.nativeGetNumberOfUserAreasInDrawingArea(drawingAreaPointer);
					// WppgmLogger.logFine("Found " + nbrOfDrawingAreasOnPage + " user areas on the
					// page");

					StringBuffer userAreas = new StringBuffer();
					for (int l = 0; l < nbrOfUserAreasInDrawingArea; ++l) {
						long userAreaPointer = ppgm.nativeGetUserArea(drawingAreaPointer, l);
						String userAreaName = ppgm.nativeGetUserAreaName(userAreaPointer);

						userAreaName = prepareUserAreaName(userAreaName);
						userAreas.append(userAreaName);
						userAreas.append(",");
						
						float userAreaLeft = ppgm.nativeGetUserAreaLeft(userAreaPointer);
						float userAreaTop = ppgm.nativeGetUserAreaTop(userAreaPointer);
						addDynamicData(newAnotoDocPointer, newPagePointer, userAreaName, userAreaTop, userAreaLeft, ppgm, address);
						
						numeroAtendimento = getDynamicData(WppgmCommons.getProperty("campo_numero"), address);
						numeroCNES = getDynamicData(WppgmCommons.getProperty("campo_nro_cnes"), address);
						numeroSUS = getDynamicData(WppgmCommons.getProperty("campo_nro_sus"), address);
					}

					// WppgmLogger.logFine("User areas: " + userAreas.toString());
				}

				// WppgmLogger.logFine("About to handle pidgets");
				int nbrOfPidgetsOnPage = ppgm.nativeGetNumberOfPidgetsOnPage(modulatedExistingPagePointer);
				// WppgmLogger.logFine("Found " + nbrOfPidgetsOnPage + " pidgets on the page");

				StringBuffer pidgets = new StringBuffer();
				for (int j = 0; j < nbrOfPidgetsOnPage; ++j) {
					long pidgetPointer = ppgm.nativeGetPidget(modulatedExistingPagePointer, j);

					String pidgetName = ppgm.nativeGetPidgetName(pidgetPointer);
					pidgets.append(pidgetName);
					pidgets.append(",");

					float pidgetLeft = ppgm.nativeGetPidgetLeft(pidgetPointer);
					float pidgetTop = ppgm.nativeGetPidgetTop(pidgetPointer);

					ppgm.nativeCreatePidget(newAnotoDocPointer, newPagePointer, pidgetName, pidgetTop, pidgetLeft);
				}

				// WppgmLogger.logFine("Pidgets: " + pidgets.toString());
			}

			int numberOfAvailablePrinters = ppgm.nativeGetNumberOfAvailablePrinters();

			// WppgmLogger.logFine("Found " + numberOfAvailablePrinters + " available
			// printers");

			int printResult = -1;
			setPrinterProfile("printer", newAnotoDocPointer);
			ppgm.nativeAddAdditionalPrintSwitches(newAnotoDocPointer, commandLineSwitches);

			String psMode = WppgmCommons.getProperty("psMode");
			if (psMode != null && (psMode.trim()).equalsIgnoreCase("Y")) {
				// WppgmLogger.logFine("Skipping print! Creating postscript file instead!");
				String psFilePath = pdfFile.getParent() + "\\" + WppgmCommons.getProperty("instance_name") + "\\"
						+ formTypeName + "_" + addresses[(addresses.length - 1)] + ".ps";
				// WppgmLogger.logFine("Postscript file path: " + psFilePath);
				printResult = ppgm.nativeGeneratePostscriptForLaser(newAnotoDocPointer, psFilePath, useLimitedColors);
				retorno = psFilePath;
			} else {
				boolean matchingPrinterFound = false;
				for (int i = 0; i < numberOfAvailablePrinters; ++i) {
					String printerName = ppgm.nativeGetPrinterName(i);
					if (!printerName.equalsIgnoreCase(chosenPrinter.trim())) {
						if (!(chosenPrinter.trim()).equals("")) {
							continue;
						}
					}
					// WppgmLogger.log("Printing on " + printerName);
					// if (useAdditionalSwitches) {
					// WppgmLogger.logFine("Using printer specific printer profile");
					// } else {
					// WppgmLogger.logFine("Using default laser printer profile");
					// }
					printResult = ppgm.nativePrintWithPatternOnLaser(newAnotoDocPointer, i + 1, useLimitedColors,
							useAdditionalSwitches);
					matchingPrinterFound = true;
					break;
				}
				if (!matchingPrinterFound) {
					WppgmLogger.logSevere("Found no matching printer! Creating postscript file instead!");
					String psFilePath = pdfFile.getParent() + "\\" + WppgmCommons.getProperty("instance_name") + "\\"
							+ formTypeName + "_" + addresses[(addresses.length - 1)] + ".ps";
					// WppgmLogger.logFine("Postscript file path: " + psFilePath);
					printResult = ppgm.nativeGeneratePostscriptForLaser(newAnotoDocPointer, psFilePath,
							useLimitedColors);
				}
			}

			String tempFile = convertPsToPdf(retorno, numeroAtendimento);

			PdfReader reader = new PdfReader(tempFile);
			
			retorno = tempFile.replaceAll("\\.tmp", "");
			
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(retorno));
			
			PdfContentByte over = stamper.getOverContent(1);

			if (numeroCNES != null && !numeroCNES.trim().equals("")) {
				Image barcodeNumeroCNES = getBarcodeImage(stamper, numeroCNES, 370, 710);
				over.addImage(barcodeNumeroCNES);
			}
			
			if (numeroSUS != null && !numeroSUS.trim().equals("")) {
				Image barcodeNumeroSUS = getBarcodeImage(stamper, numeroSUS, 34, 676);
				over.addImage(barcodeNumeroSUS);
			}
			
			PdfWriter writer = stamper.getWriter();
			PdfAction action = new PdfAction(PdfAction.PRINTDIALOG);
			writer.setOpenAction(action);
			
			stamper.close();
			reader.close();
			
			FileUtils.forceDelete(new File(tempFile));

			String printResultString = "Success";
			if (printResult < 0) {
				printResultString = "Failed";
			}
			WppgmLogger.logFine("Print result: " + printResultString + " (code: " + printResult + ")");
			ppgm.nativeDestroyPatternLicense(licensePointer);
			ppgm.nativeDestroyAnotoDoc(newAnotoDocPointer);
			ppgm.nativeDestroyAnotoDoc(existingAnotoDocPointer);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retorno;
	}
	
	private static Image getBarcodeImage(PdfStamper stamper, String data, float posX, float posY) throws BadElementException, MalformedURLException, IOException, BadPdfFormatException {
		Image image = Image.getInstance(generateBarcode(data));
		PdfImage stream = new PdfImage(image, "", null);
		PdfIndirectObject ref = stamper.getWriter().addToBody(stream);
		image.setDirectReference(ref.getIndirectReference());
		image.setAbsolutePosition(posX, posY);
		return image;
	}

	private static String convertPsToPdf(String psFilePathList, String fileName) throws Exception {
		// Caminho do arquivo pdf.
		//String outputPdfFilePath = WppgmProperties.PRINTFOLDER + fileName + "_"	+ getDateString(new Date(), "yyyy-MM-dd_HH.mm") + ".pdf.tmp";
		String outputPdfFilePath = WppgmProperties.PRINTFOLDER + fileName + ".pdf.tmp";

		// Converte o arquivo postscript para pdf.
		String convertPS2PDF = "gswin32c.exe -q -dNumRenderingThreads#4 -dSAFER -dNOPAUSE -dBATCH -sDEVICE#pdfwrite -sPAPERSIZE#a4 -sOutputFile#"
				+ outputPdfFilePath + " " + psFilePathList;
		Process process = Runtime.getRuntime().exec(convertPS2PDF);

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// Erro na interrupção do processo.
		}

		// Mata o processo de conversão de ps para pdf.
		process.destroy();

		String[] psFilePathArray = psFilePathList.split(" ");

		for (int i = 0; i < psFilePathArray.length; i++) {
			File psFilePath = new File(psFilePathArray[i]);
			try {
				// Apaga arquivo ps.
				FileUtils.forceDelete(psFilePath);
			} catch (IOException e) {
				// Erro ao apagar arquivo ps.
			}
		}

		return outputPdfFilePath;
	}

	private static String getDateString(Date date, String format) {
		Locale locale = new Locale("pt", "BR");
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);
		String dateString = dateFormat.format(date);
		return dateString;
	}

	private static byte[] generateBarcode(String data) {
		byte[] imgb = null;
		final int dpi = 150;
		
		// Open output file
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			// Create the barcode bean
			Code39Bean bean = new Code39Bean();

			// Configure the barcode generator
			bean.setModuleWidth(UnitConv.in2mm(1.0f / dpi)); // makes the narrow bar, width exactly one pixel

			bean.setWideFactor(3.02);
			bean.doQuietZone(true);
			bean.setFontSize(1.6);
			bean.setBarHeight(10);
	
			//Set up the canvas provider for monochrome PNG output
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(baos, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);

			// Generate the barcode
			bean.generateBarcode(canvas, data);

			// Signal end of generation
			canvas.finish();

			imgb = baos.toByteArray();

		} catch (Exception e) {
			WppgmLogger.logSevere(e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
			}
		}

		return imgb;
	}

	private static void setPrinterProfile(String printerName, long anotoDocPointer) {
		int CS_LASER = 0;
		int CS_LIMITED_COLORS = 1;

		try {
			// WppgmLogger.logFine("About to set printer profile for printer: " +
			// printerName);
			// String profile_name = WppgmCommons.getProperty(printerName);
			String profile_name = "printer";

			if ((profile_name != null) && (profile_name.length() > 0)) {
				// WppgmLogger.logFine("Printer matched to profile: " + profile_name);
				Properties printerSettings = new Properties();
				// FileInputStream is = new FileInputStream(printerProfilesFolder + "\\" +
				// profile_name + ".txt");
				InputStream is = PPGMAdapter.class.getResourceAsStream("/printer.properties");
				printerSettings.load(is);

				String colSeparation = printerSettings.getProperty("colorseparation");
				if (colSeparation != null) {
					int colorSeparation = Integer.parseInt(colSeparation);
					if (colorSeparation == CS_LASER) {
						useLimitedColors = false;
						commandLineSwitches += " -pk2cmy";
					} else if (colorSeparation == CS_LIMITED_COLORS) {
						useLimitedColors = true;
						commandLineSwitches += " -ptwocomp";
					}
				}

				String usePsForms = printerSettings.getProperty("usepsforms");

				if ((usePsForms != null) && (usePsForms.trim().equalsIgnoreCase("1"))) {
					commandLineSwitches += " --usepsforms 1";
				}

				String setPageSize = printerSettings.getProperty("setpagesize");

				if ((setPageSize != null) && (setPageSize.trim().equalsIgnoreCase("1"))) {
					commandLineSwitches += " --setpagesize 1";
				}

				String dpi = printerSettings.getProperty("dpi");

				if (dpi != null) {
					commandLineSwitches = commandLineSwitches + " --dpi " + dpi;
				}

				String dotScale = printerSettings.getProperty("dotscale");

				if (dotScale != null) {
					int scale = Integer.parseInt(dotScale);
					commandLineSwitches = commandLineSwitches + " --dotscale " + (scale / 100.0D);
				}

				String dotRadius = printerSettings.getProperty("dotradius");

				if (dpi != null) {
					commandLineSwitches = commandLineSwitches + " --dotrad " + dotRadius + "mm";
				}

				String dotOffset = printerSettings.getProperty("dotoffset");

				if (dotOffset != null) {
					commandLineSwitches = commandLineSwitches + " --dotoffset " + dotOffset + "mm";
				}

				String bleedString = printerSettings.getProperty("bleed");
				if (bleedString != null) {
					int bleed = Integer.parseInt(bleedString);

					if (bleed > 0) {
						commandLineSwitches = commandLineSwitches + " -c " + bleed + "mm";

						String offsetX = printerSettings.getProperty("offsetx");
						if (offsetX != null) {
							commandLineSwitches = commandLineSwitches + " --pageoffsetx " + offsetX + "mm";
							System.out.println("");
						}

						String offsetY = printerSettings.getProperty("offsety");
						if (offsetY != null) {
							commandLineSwitches = commandLineSwitches + " --pageoffsety " + offsetY + "mm";
						}

					}

				}

				String optionalSwitches = printerSettings.getProperty("optional");

				if ((optionalSwitches != null) && (optionalSwitches.length() > 0)) {
					commandLineSwitches = commandLineSwitches + " " + optionalSwitches;
				}

				System.out.println(commandLineSwitches);

				useAdditionalSwitches = true;
			} else {
				// WppgmLogger.logFine("Couldn't find a matching printer profile");
				useAdditionalSwitches = false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			WppgmLogger.logSevere("Something went wrong. Use default laser print profile! ");
			useAdditionalSwitches = false;
		}
	}

	private static void splitPdf() {
		try {
			// WppgmLogger.logFine("About to split pdf to eps file(s)");
			FileInputStream padStream = new FileInputStream(padFilePath);
			int nbrOfFormPages = PadFileUtility.getPageAddresses(padStream).length;
			for (int i = 1; i < nbrOfFormPages + 1; ++i) {
				String pageSplitPdfString = splitPdfString.replaceAll("#XX", "" + i);
				Runtime.getRuntime().exec(pageSplitPdfString);

				long sleepTime = WppgmProperties.splitEpsSleepIntervalPerPage;

				while (!(new File(WppgmProperties.TEMPFOLDER + "\\" + formTypeName + "_page_" + i + ".eps")
						.canRead())) {
					Thread.sleep(sleepTime);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addDynamicData(long anotoDocPointer, long pagePointer, String userAreaName, float userAreaTop,
			float userAreaLeft, PPGMAdapter ppgm, String pageAddress) {
		XmlHandler xmlHandler = new XmlHandler(dynData);

		Dynamicdata.Settings.Fields.Field.Text textSettings = xmlHandler.getTextSettingsByName(userAreaName);

		if (textSettings != null) {
			// WppgmLogger.logFine("About to add dynamic data for field " + userAreaName);

			String textToAdd = xmlHandler.getTextValue(userAreaName, pageAddress);
			// WppgmLogger.logFine("Text: " + textToAdd);

			float top = userAreaTop;
			float left = userAreaLeft;

			float angle = textSettings.getAngle();
			// WppgmLogger.logFine("top,left,angle= " + userAreaTop + "," + userAreaLeft +
			// "," + angle);

			String textSize = textSettings.getSize() + "pt";
			String font = textSettings.getFont();
			String encoding = "Unicode";

			WppgmLogger.logFine("text size,font,encoding= " + textSize + "," + font + "," + encoding);

			setTextColor(textSettings, ppgm, anotoDocPointer);

			ppgm.addText(pagePointer, top, left, angle, textToAdd, textSize, font, encoding);

			resetTextColor(ppgm, anotoDocPointer);
		}
		
	}

	public static String[] getFormPages(String paSubmittedPage) {
		String[] formPageAddresses = new String[0];
		try {
			// WppgmLogger.logFine("About to get all pages in the form");
			ArrayList<String> pagesCurrentForm = new ArrayList<String>();

			int pageNumberSubmittedPage = Integer.parseInt(
					paSubmittedPage.substring(paSubmittedPage.lastIndexOf(".") + 1, paSubmittedPage.length()));
			String paSubmittedPageNoPage = paSubmittedPage.substring(0, paSubmittedPage.lastIndexOf(".")).trim();

			FileInputStream padStream = new FileInputStream(WppgmCommons.findPadPath(paSubmittedPage));
			int numberOfPages = PadFileUtility.getPageAddresses(padStream).length;

			String additionalPageAddress = "";
			int pageNumberCurrentPage = pageNumberSubmittedPage;
			int index = pageNumberSubmittedPage % numberOfPages;

			for (int i = index; i > index - numberOfPages; --i) {
				pageNumberCurrentPage = pageNumberSubmittedPage - i;
				additionalPageAddress = paSubmittedPageNoPage + "." + pageNumberCurrentPage;
				pagesCurrentForm.add(additionalPageAddress);
			}

			formPageAddresses = (String[]) (String[]) pagesCurrentForm.toArray(new String[0]);
			WppgmCommons.sortByPageAddressAsc(formPageAddresses);
		} catch (Exception e) {
			WppgmLogger.logSevere("Got exception" + e.getMessage());

			e.printStackTrace();
		}

		return formPageAddresses;
	}

	private static String prepareUserAreaName(String userAreaName) {
		if (userAreaName.contains("")) {
			userAreaName = userAreaName.replaceAll("", "");
		}
		return userAreaName;
	}
	
	private static String getDynamicData(String userAreaName, String pageAddress) {
		XmlHandler xmlHandler = new XmlHandler(dynData);
		return xmlHandler.getTextValue(userAreaName, pageAddress);
	}
}
