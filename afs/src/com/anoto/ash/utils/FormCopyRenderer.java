package com.anoto.ash.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;

import com.anoto.api.NoSuchPageException;
import com.anoto.api.Page;
import com.anoto.api.Pen;
import com.anoto.api.renderer.JRenderer;
import com.anoto.api.util.PadFileUtility;
import com.anoto.ash.AshCommons;
import com.anoto.ash.AshLogger;
import com.anoto.ash.AshProperties;
import com.anoto.ash.RendererObserver;
import com.anoto.ash.database.BackgroundFile;
import com.anoto.ash.database.DynamicDataEntry;
import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.portal.AshFormControl;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PdfWriter;

@SuppressWarnings({ "rawtypes" })
public class FormCopyRenderer {
	
	public List<String> createImagesForUI(FormCopyData formCopy, String userName, int pageNumber, String root) {
		List<String> files = new ArrayList<String>();

		files = createImages(formCopy, userName, pageNumber, root);

		return files;
	}

	@SuppressWarnings("unchecked")
	private List<String> createImages(FormCopyData formCopy, String additionalInfo, int pageNumberToRender, String root) {
		RendererObserver observer = null;
		List createdImages = new ArrayList();
		
		try {
			AshFormControl formControl = AshFormControl.getInstance();
			Pen pen = AshCommons.getPenFromByteArray(formCopy.getPgc());
			Iterator pageIterator = pen.getPages();

			Page currentPage = (Page) pageIterator.next();
			List pageAddresses = AshCommons.getFormPagesAsList(currentPage);
			String imageFormat = "png";
			String formTypeName = formCopy.getFormType().getFormTypeName();
			String penSerial = pen.getPenData().getPenSerial();

			File f = new File(root);

			if (!(f.exists())) {
				FileUtils.forceMkdir(f);
			}

			String address = "";
			if (pageNumberToRender < pageAddresses.size()) {
				address = (String) pageAddresses.get(pageNumberToRender);
			} else {
				return createdImages;
			}

			List tmpAddress = new ArrayList();
			tmpAddress.add(address);
			observer = new RendererObserver(tmpAddress, f, "", "", "", formTypeName, penSerial, "", false);

			BackgroundFile backgroundFile = new BackgroundFile();
			backgroundFile.setFileName(PadFileUtility.getBackgroundImageForPage(address, "Anoto_Forms_Solution_ASH"));
			backgroundFile.setFormType(formCopy.getFormType());

			backgroundFile = (BackgroundFile) formControl.getBackgroundFile(backgroundFile).get(0);

			InputStream bgStream = new ByteArrayInputStream(backgroundFile.getBackgroundFile());

			String backgroundImageIdentifier = formCopy.getFormType().getFormTypeName() + "|_|"
					+ backgroundFile.getFileName();
			try {
				currentPage = AshCommons.getPenFromByteArray(formCopy.getPgc()).getPage(address);

				String fileName = AshCommons.createShortPageAddressFileName(formCopy, address, root, additionalInfo);

				File pageFile = new File(fileName + "." + imageFormat);

				createdImages.add(pageFile.getAbsolutePath());

				JRenderer renderer = AshCommons.render(formCopy, currentPage, backgroundImageIdentifier, bgStream,
						pageFile.getPath(), false);
				observer.addRendererToObserve(renderer);

			} catch (NoSuchPageException nope) {
				String fileName = AshCommons.createPageAddressFileName(formCopy, address, root, additionalInfo);

				File emptyPageFile = new File(fileName + "." + imageFormat);

				createdImages.add(emptyPageFile.getAbsolutePath());
				JRenderer renderer = AshCommons.render(formCopy, address, backgroundImageIdentifier, bgStream,
						emptyPageFile.getPath(), false);
				observer.addRendererToObserve(renderer);

			} catch (Exception pe) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!(observer.checkIfAllRenderersCompleted()))
			try {
				Thread.sleep(100L);
			} catch (Exception e) {
			}

		return createdImages;
	}
	
	public String createImagesForUI(FormCopyData formCopy, String additionalInfo) throws Exception {		
		List<String> imageList = createImages(formCopy, additionalInfo);
		return mergeImageFiles(imageList, formCopy.getDynamicDataEntries());
	}

	public List<String> createImages(FormCopyData formCopy, String additionalInfo) {
		List<String> createdImages = new ArrayList<String>();
		
		try {
			AshLogger.logFine("About to create image for form " + formCopy.getPageAddressRange());
			Pen pen = AshCommons.getPenFromByteArray(formCopy.getPgc());
			Iterator pageIterator = pen.getPages();

			Page currentPage = (Page) pageIterator.next();
			List pageAddresses = AshCommons.getFormPagesAsList(currentPage);
			String imageFormat = formCopy.getFormType().getExportMethod().getImageFormat().getName().toLowerCase();
			RendererObserver observer = null;
			
			File exportFolder = new File(AshProperties.EXPORT_FOLDER + "\\" + formCopy.getFormType().getFormTypeName() + "\\" + AshCommons.getCurrentDateAndTime("yyyyMMdd"));
			if (!(exportFolder.exists())) {
				FileUtils.forceMkdir(exportFolder);
			}

			if (imageFormat.equalsIgnoreCase("pdf")) {
				observer = new RendererObserver(pageAddresses, exportFolder, "", "", "", formCopy.getFormType().getFormTypeName(), pen.getPenData().getPenSerial(), "", false);
			}

			for (Iterator i = pageAddresses.iterator(); i.hasNext();) {
				String address = (String) i.next();

				BackgroundFile backgroundFile = new BackgroundFile();
				backgroundFile.setFileName(PadFileUtility.getBackgroundImageForPage(address, "Anoto_Forms_Solution_ASH"));
				backgroundFile.setFormType(formCopy.getFormType());

				backgroundFile = (BackgroundFile) AshFormControl.getInstance().getBackgroundFile(backgroundFile).get(0);

				InputStream bgStream = new ByteArrayInputStream(backgroundFile.getBackgroundFile());

				String backgroundImageIdentifier = formCopy.getFormType().getFormTypeName() + "|_|"	+ backgroundFile.getFileName();
				try {
					currentPage = AshCommons.getPenFromByteArray(formCopy.getPgc()).getPage(address);

					String fileName = AshCommons.createPageAddressFileName(formCopy, address, exportFolder.getPath(), additionalInfo);
					File pageFile = new File(fileName + "." + imageFormat);

					if (imageFormat.equalsIgnoreCase("pdf")) {
						JRenderer renderer = AshCommons.render(formCopy, currentPage, backgroundImageIdentifier, bgStream, pageFile.getPath(), false);
						observer.addRendererToObserve(renderer);

					} else {
						AshCommons.render(formCopy, currentPage, backgroundImageIdentifier, bgStream, pageFile.getPath(), true);
					}
					
					createdImages.add(pageFile.getAbsolutePath());
					
				} catch (NoSuchPageException nope) {
					String fileName = AshCommons.createPageAddressFileName(formCopy, address, exportFolder.getPath(),additionalInfo);
					File emptyPageFile = new File(fileName + "." + imageFormat);

					if (imageFormat.equalsIgnoreCase("pdf")) {
						JRenderer renderer = AshCommons.render(formCopy, address, backgroundImageIdentifier, bgStream, emptyPageFile.getPath(), false);
						observer.addRendererToObserve(renderer);
					} else {
						AshCommons.render(formCopy, address, backgroundImageIdentifier, bgStream, emptyPageFile.getPath(), true);
					}

					AshLogger.logFine("No page with the address " + address	+ ", creating a file with just the background image: " + emptyPageFile.getAbsolutePath());
					
					createdImages.add(emptyPageFile.getAbsolutePath());
					
				} catch (Exception pe) {
					pe.printStackTrace();
					AshLogger.logSevere(pe.getMessage());
				}
			}
			
			if (observer != null) {
				while (!observer.checkIfAllRenderersCompleted()) {
					Thread.sleep(100L);
				}
			} else {
				for (String image : createdImages) {
					waitingImageFile(image);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			AshLogger.logSevere(e.getMessage());	
		}
		
		return createdImages;
	}
	
	private String mergeImageFiles(List<String> imageList, Set<DynamicDataEntry> dynamicDataEntries) throws Exception {
		String dest = imageList.get(0).replaceFirst("\\.[^.]*$", "\\.pdf");
		Image img = Image.getInstance(imageList.get(0));
		Document document = new Document(img);
		PdfWriter.getInstance(document, new FileOutputStream(dest));
		document.open();
		int page = 0;
		for (String image : imageList) {
			img = Image.getInstance(image);
			document.setPageSize(img);
			document.newPage();
			img.setAbsolutePosition(0, 0);
			document.add(img);
			if (page == 0) {
				for (DynamicDataEntry dynamicDataEntry : dynamicDataEntries) {
					if (dynamicDataEntry.getName().equalsIgnoreCase(AshProperties.getAfsProperty("ident_campo_nro_sus"))) {
						document.add(getBarcodeImage("12232432423423123", 42, 943));
					}
					if (dynamicDataEntry.getName().equalsIgnoreCase(AshProperties.getAfsProperty("ident_campo_nro_cnes"))) {
						document.add(getBarcodeImage("4342342", 490, 990));
					}
				}
			}
			page++;
		}
		document.close();
		
		for (String image : imageList) {
			FileUtils.forceDelete(new File(image));
		}
		
		return dest;
	}
	
	
	private void waitingImageFile(String imagePath) throws InterruptedException {
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
	
	private Image getBarcodeImage(String data, float posX, float posY) throws BadElementException, MalformedURLException, IOException, BadPdfFormatException {
		Image image = Image.getInstance(generateBarcode(data));
		image.setAbsolutePosition(posX, posY);
		return image;
	}
	
	private byte[] generateBarcode(String data) {
		byte[] imgb = null;
		final int dpi = 150;
		
		// Open output file
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			// Create the barcode bean
			Code39Bean bean = new Code39Bean();

			// Configure the barcode generator
			bean.setModuleWidth(UnitConv.in2mm(1.0f / dpi)); // makes the narrow bar, width exactly one pixel

			bean.setWideFactor(3.10);
			bean.doQuietZone(true);
			bean.setFontSize(1.6);
			bean.setBarHeight(14.8);
	
			//Set up the canvas provider for monochrome PNG output
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(baos, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);

			// Generate the barcode
			bean.generateBarcode(canvas, data);

			// Signal end of generation
			canvas.finish();

			imgb = baos.toByteArray();

		} catch (Exception e) {
			AshLogger.logSevere(e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
			}
		}

		return imgb;
	}
}