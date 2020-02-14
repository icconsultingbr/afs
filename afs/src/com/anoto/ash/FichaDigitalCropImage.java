package com.anoto.ash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.utils.FormCopyRenderer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

import javax.imageio.ImageIO;

/**
 * AshRenderServlet
 * 
 */
@SuppressWarnings("rawtypes")
public class FichaDigitalCropImage {

	private static final long serialVersionUID = 1L;

	private Rectangle clip;

	private int pageNumber = 0;

	public class CropImage {
		public String fullPathFilename;
		public int height;
		public int width;
		public int startX;
		public int startY;
	}
	
	/**
	 * 
	 * @param numeroOcorrencia
	 * @param cropImages
	 * @throws Exception
	 */
	public void saveCropsImages(String numeroOcorrencia,
			List<CropImage> cropImages) throws Exception {

		if (numeroOcorrencia != null) {
			FormCopyData formCopy = getFormCopy(numeroOcorrencia);
			try {
				if (formCopy == null) {
					throw new Exception();
				}

				FormCopyRenderer renderer = new FormCopyRenderer();
				File rootFolder = new File(AshProperties.EXPORT_FOLDER + "\\"
						+ formCopy.getFormType().getFormTypeName() + "\\"
						+ AshCommons.getCurrentDateAndTime("yyyyMMdd"));
				if (!(rootFolder.exists())) {
					FileUtils.forceMkdir(rootFolder);
				}

				List tmpImages = renderer
						.createImagesForUI(formCopy, numeroOcorrencia,
								this.pageNumber, rootFolder.getPath());
				String imagePath = (String) tmpImages.get(0);

				// --------------------- Cropping image ------------------------

				BufferedImage originalImage = readImage(imagePath);

				for (CropImage crop : cropImages) {
					//processando a imagem
					BufferedImage processedImage = cropMyImage(originalImage,
							crop.width, crop.height, crop.startX, crop.startY);
					//gravando a imagem
					writeImage(processedImage, crop.fullPathFilename, "jpg");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param numeroOcorrencia
	 * @return
	 */
	private FormCopyData getFormCopy(String numeroOcorrencia) {
		FormCopyData formCopyData = null;
		FormCopyData formCopy = new FormCopyData();
		formCopy.setNumeroOcorrencia(numeroOcorrencia);

		AshFormControl formControl = AshFormControl.getInstance();
		List matchingFormCopies = formControl.searchFormCopies(formCopy);

		Iterator formCopyIterator = matchingFormCopies.iterator();

		if (formCopyIterator.hasNext()) {
			formCopyData = (FormCopyData) formCopyIterator.next();
		}

		return formCopyData;
	}
	
	/**
	 * 
	 * @param img
	 * @param cropWidth
	 * @param cropHeight
	 * @param cropStartX
	 * @param cropStartY
	 * @return
	 * @throws Exception
	 */
	public BufferedImage cropMyImage(BufferedImage img, int cropWidth,
			int cropHeight, int cropStartX, int cropStartY) throws Exception {
		BufferedImage clipped = null;
		Dimension size = new Dimension(cropWidth, cropHeight);

		createClip(img, size, cropStartX, cropStartY);

		try {
			int w = clip.width;
			int h = clip.height;

			System.out.println("Crop Width " + w);
			System.out.println("Crop Height " + h);
			System.out.println("Crop Location " + "(" + clip.x + "," + clip.y
					+ ")");

			clipped = img.getSubimage(clip.x, clip.y, w, h);

		} catch (RasterFormatException rfe) {
			return null;
		}
		return clipped;
	}

	/**
	 * 
	 * @param img Imagem para ser cortada
	 * @param size Dimensoes da imagem	 *            
	 * @param clipX Eixo inicial
	 * @param clipY Eixo final
	 * @throws Exception
	 */
	private void createClip(BufferedImage img, Dimension size, int clipX,
			int clipY) throws Exception {

		boolean isClipAreaAdjusted = false;

		if (clipX < 0) {
			clipX = 0;
			isClipAreaAdjusted = true;
		}

		if (clipY < 0) {
			clipY = 0;
			isClipAreaAdjusted = true;
		}

		if ((size.width + clipX) <= img.getWidth()
				&& (size.height + clipY) <= img.getHeight()) {

			clip = new Rectangle(size);
			clip.x = clipX;
			clip.y = clipY;
		} else {

			if ((size.width + clipX) > img.getWidth())
				size.width = img.getWidth() - clipX;

			if ((size.height + clipY) > img.getHeight())
				size.height = img.getHeight() - clipY;

			clip = new Rectangle(size);
			clip.x = clipX;
			clip.y = clipY;

			isClipAreaAdjusted = true;

		}
		if (isClipAreaAdjusted) {
			throw new Exception("Area informada está fora da área da imagem, ajuste suas coordenadas.");
		}
	}
	
	/**
	 * 
	 * @param fileLocation
	 * @return
	 */
	public BufferedImage readImage(String fileLocation) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(fileLocation));
			System.out.println("Image Read. Image Dimension: " + img.getWidth()
					+ "w X " + img.getHeight() + "h");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}
	
	/**
	 * 
	 * @param img
	 * @param fileLocation
	 * @param extension
	 */
	public void writeImage(BufferedImage img, String fileLocation,
			String extension) {
		try {
			BufferedImage bi = img;
			File outputfile = new File(fileLocation);
			ImageIO.write(bi, extension, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}