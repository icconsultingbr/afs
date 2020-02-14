package com.anoto.ash.portal;

import com.anoto.api.FormatException;
import com.anoto.api.IllegalValueException;
import com.anoto.api.MergeException;
import com.anoto.api.NoSuchPageException;
import com.anoto.api.NoSuchPermissionException;
import com.anoto.api.Page;
import com.anoto.api.PageException;
import com.anoto.api.Pen;
import com.anoto.api.util.PadFileUtility;
import com.anoto.ash.AshCommons;
import com.anoto.ash.AshLogger;
import com.anoto.ash.AshPgcControl;
import com.anoto.ash.AshProperties;
//import com.anoto.ash.ExportMailer;
import com.anoto.ash.MailControl;
import com.anoto.ash.Sorter;
import com.anoto.ash.database.BackgroundFile;
import com.anoto.ash.database.DynamicDataDefinition;
import com.anoto.ash.database.DynamicDataEntry;
import com.anoto.ash.database.ExportFormat;
import com.anoto.ash.database.ExportMethod;
import com.anoto.ash.database.FieldData;
import com.anoto.ash.database.Font;
import com.anoto.ash.database.FormCopyPageArea;
import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.database.FormDBHandler;
import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.ImageFormat;
import com.anoto.ash.database.PadFile;
import com.anoto.ash.database.UserData;
import com.anoto.ash.exporter.VOFormProcessorToResultDocumentFactory;
//import com.anoto.ash.exporter.document.Attributes;
import com.anoto.ash.exporter.document.ResultDocument;
//import com.anoto.ash.exporter.writers.Writer;
//import com.anoto.ash.exporter.writers.impl.CSVWriter;
//import com.anoto.ash.exporter.writers.impl.XMLWriter;
import com.anoto.ash.portal.result.FieldResult;
import com.anoto.ash.portal.result.FormCopyResult;
import com.anoto.ash.portal.result.FormTypeResult;
import com.anoto.ash.rs.AshMailHandler;
import com.anoto.ash.vo.ink.VOControl;
import com.anoto.ash.vo.ink.VOInterpretationResult;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
//import java.nio.channels.FileChannel;
import java.sql.SQLException;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Calendar;
import java.util.Date;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
//import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
//import org.apache.commons.io.FileUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.xml.sax.SAXException;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AshFormControl implements Serializable {
	private static final long serialVersionUID = 1L;
	private static AshFormControl instance = null;

	public static AshFormControl getInstance() {
		if (instance == null) {
			instance = new AshFormControl();
		}

		return new AshFormControl();
	}

	public FormCopyResult addFormCopy(FormCopyData formCopyData) {
		AshLogger.logFine("About to add form copy");
		FormCopyResult result = new FormCopyResult();
		try {
			int operationResult = FormDBHandler.addFormCopy(formCopyData);

			if (operationResult == -11) {
				AshLogger
						.logSevere("No form type exists with the proposed form type name.");
				throw new IllegalStateException(
						"Severe Error. Please contact system administrator.");
			}

			if (operationResult == -1) {
				AshLogger.logSevere("Operation failed.");
				throw new IllegalStateException(
						"Severe Error. Please contact system administrator.");
			}
			result.setFormCopyOperationSuccessful(true);
			formCopyData.setFormCopyId(operationResult);
		} catch (HibernateException he) {
			result.setFormCopyOperationSuccessful(false);
			result.setFormCopyOperationMessage("Add Operation failed.");
			AshLogger
					.logSevere("Add failed. Got exception: " + he.getMessage());
		}

		result.setFormCopyData(formCopyData);

		return result;
	}

	public int addPadFile(PadFile padFile) {
		AshLogger.logFine("About to add pad file, address:"
				+ padFile.getPadLicenseAddress());
		int operationResult = -1;
		try {
			operationResult = FormDBHandler.addPadFile(padFile);
		} catch (HibernateException he) {
			operationResult = -1;
			AshLogger.logSevere("Error when adding pad file, reason: "
					+ he.getMessage());
		}

		return operationResult;
	}

	public int addBackgroundFile(BackgroundFile backgroundFile) {
		AshLogger.logFine("About to add background file"
				+ backgroundFile.getFileName());
		int operationResult = -1;
		try {
			operationResult = FormDBHandler.addBackgroundFile(backgroundFile);
		} catch (HibernateException he) {
			operationResult = -1;
			AshLogger.logSevere("Error when adding pad file, reason: "
					+ he.getMessage());
		}

		return operationResult;
	}

	public List getAllFormTypes() throws IllegalStateException {
		AshLogger.logFine("About to get all Formtypes");
		ArrayList formTypes = new ArrayList();
		try {
			formTypes = FormDBHandler.getAllFormTypes();
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return formTypes;
	}

	public List getAllFormCopies() throws IllegalStateException {
		AshLogger.logFine("About to get all formcopies");
		ArrayList formCopies = new ArrayList();
		try {
			formCopies = FormDBHandler.getAllFormCopies(false, false, "", 3);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return formCopies;
	}

	public List getAllFormCopiesFiltered() throws IllegalStateException {
		AshLogger.logFine("About to get all formcopies");
		ArrayList formCopies = new ArrayList();
		try {
			formCopies = FormDBHandler.getAllFormCopies(true, false, "", 3);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return formCopies;
	}

	public List getAllPadFiles() throws IllegalStateException {
		AshLogger.logFine("About to get all pad files");
		ArrayList padFiles = new ArrayList();
		try {
			padFiles = FormDBHandler.getAllPadFiles();
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return padFiles;
	}

	public List getFormType(FormTypeData formTypeData) {
		List result;
		AshLogger.logFine("About to get formtype" + formTypeData.toString());
		try {
			result = FormDBHandler.searchFormType(formTypeData);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return result;
	}

	public FormTypeData getFormType(int formTypeId)
			throws IllegalStateException {
		AshLogger.logFine("About to get formtype");
		FormTypeData formTypeData = null;
		try {
			formTypeData = FormDBHandler.getFormType(formTypeId);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return formTypeData;
	}

	public List getPadFile(PadFile padFile) {
		List result;
		AshLogger
				.logFine("About to get Pad files from DB based on search criteria");
		try {
			result = FormDBHandler.searchPadFile(padFile);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return result;
	}

	public List getBackgroundFile(BackgroundFile backgroundFile) {
		List result;
		AshLogger
				.logFine("About to get background files from DB based on search criteria");
		try {
			result = FormDBHandler.searchBackgroundFile(backgroundFile);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return result;
	}

	public List getFont(Font font) {
		List result;
		AshLogger.logFine("About to get font from DB based on search criteria");
		try {
			result = FormDBHandler.searchFont(font);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return result;
	}

	public DynamicDataDefinition getDynamicDataDefinition(String fieldName,
			FormTypeData formTypeData) {
		DynamicDataDefinition definition = null;

		AshLogger
				.logFine("About to get dynamic data definition from DB based on field name and form type");
		try {
			int id = FormDBHandler.getDynamicDataDefinitionId(fieldName,
					formTypeData);

			if (id != -1)
				definition = FormDBHandler.getDynamicDataDefinition(id);

		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return definition;
	}

	public List<FormCopyData> getFormCopy(FormCopyData formCopy)
			throws IllegalStateException {
		AshLogger.logFine("About to get form copy");
		try {
			List formCopiesList = null;
			formCopiesList = FormDBHandler.searchFormCopy(formCopy, -1, "", 3);

			return formCopiesList;
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}
	}

	public List<FormCopyData> getFormCopyFiltered(FormCopyData formCopy,
			int startPos, String orderProperty, int order)
			throws IllegalStateException {
		AshLogger.logFine("About to get form copy");
		boolean filterOnDisplayPeriod = false;

		if (formCopy.getFormStatus() == 3)
			filterOnDisplayPeriod = true;

		try {
			List formCopiesList = null;

			formCopiesList = FormDBHandler.searchFormCopyFiltered(formCopy,
					filterOnDisplayPeriod, startPos, orderProperty, order);

			return formCopiesList;
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}
	}

	public Integer getNumberOfFormCopyFiltered(FormCopyData formCopy,
			int startPos) throws IllegalStateException {
		boolean filterOnDisplayPeriod = false;

		if (formCopy.getFormStatus() == 3)
			filterOnDisplayPeriod = true;

		try {
			return FormDBHandler.searchNumberOfFormCopyFiltered(formCopy,
					filterOnDisplayPeriod, startPos);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}
	}

	public FormCopyData getFormCopy(int formCopyId)
			throws IllegalStateException {
		AshLogger.logFine("About to get form copy");
		FormCopyData formCopyData = null;
		try {
			formCopyData = FormDBHandler.getFormCopy(formCopyId);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe Error. Please contact system administrator.");
		}

		return formCopyData;
	}

	public int updateDynamicDataDefinition(DynamicDataDefinition dynamicDataDef) {
		AshLogger.logFine("About to update form copy");
		int operationResult = -1;
		try {
			operationResult = FormDBHandler
					.updateDynamicDataDefinition(dynamicDataDef);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return operationResult;
	}

	public FormCopyResult updateFormCopy(FormCopyData formCopyData) {
		AshLogger.logFine("About to update form copy");

		FormCopyResult result = new FormCopyResult();
		try {
			FormCopyData oldFormCopy = FormDBHandler.getFormCopy(formCopyData
					.getFormCopyId());

			if ((((formCopyData.getDynamicDataEntries() == null) || (formCopyData
					.getDynamicDataEntries().size() == 0)))
					&& (oldFormCopy.getDynamicDataEntries() != null)
					&& (oldFormCopy.getDynamicDataEntries().size() > 0)) {
				formCopyData.setDynamicDataEntries(oldFormCopy
						.getDynamicDataEntries());
			}

			if (formCopyData.getPgc() == null) {
				formCopyData.setPgc(oldFormCopy.getPgc());
			}

			if (formCopyData.getMarkedCompleted() == -10) {
				formCopyData.setMarkedCompleted(oldFormCopy
						.getMarkedCompleted());
			}

			if (formCopyData.getVerificationNeeded() == -10) {
				formCopyData.setVerificationNeeded(oldFormCopy
						.getVerificationNeeded());
			}

			if (formCopyData.getMandatoryFieldsMissing() == -10) {
				formCopyData.setMandatoryFieldsMissing(oldFormCopy
						.getMandatoryFieldsMissing());
			}

			if (formCopyData.getLocked() == -10) {
				formCopyData.setLocked(oldFormCopy.getLocked());
			}

			if (formCopyData.getExportMailFailed() == -10) {
				formCopyData.setExportMailFailed(oldFormCopy
						.getExportMailFailed());
			}

			if (formCopyData.getImageMailFailed() == -10) {
				formCopyData.setImageMailFailed(oldFormCopy
						.getImageMailFailed());
			}

			if (formCopyData.getFormType().getRemainingFormCopies() - 1 == oldFormCopy
					.getFormType().getRemainingFormCopies()) {
				formCopyData.getFormType().setRemainingFormCopies(
						oldFormCopy.getFormType().getRemainingFormCopies());
			}

			int operationResult = FormDBHandler.updateFormCopy(formCopyData);

			if (operationResult == -14) {
				result.setFormCopyOperationSuccessful(false);
				result.setFormCopyOperationMessage("No formcopy exists with the proposed formcopy id.");
				AshLogger.logSevere("Get operation failed. No such form copy");
			} else {
				result.setFormCopyOperationSuccessful(true);
			}
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
			result.setFormCopyOperationSuccessful(false);
			result.setFormCopyOperationMessage("Severe Database Failure");
		} catch (Exception e) {
			e.printStackTrace();
			AshLogger.logSevere("Update failed. Got exception: "
					+ e.getMessage());
			result.setFormCopyOperationSuccessful(false);
			result.setFormCopyOperationMessage(e.getMessage());
		}

		result.setFormCopyData(formCopyData);

		return result;
	}

	public int addFormType(FormTypeData formTypeData) {
		AshLogger.logFine("About to add form type");
		return FormDBHandler.addFormType(formTypeData);
	}

	public FormTypeResult updateFormType(FormTypeData formTypeData) {
		AshLogger.logFine("About to update form type");

		FormTypeResult result = new FormTypeResult();
		try {
			Iterator i$;
			int maxNbrOfFormCopies = formTypeData.getMaxNbrOfFormCopies();
			AshLogger.logFine("Max number of form copies: "
					+ maxNbrOfFormCopies);

			double notificationLevelPercent = formTypeData
					.getNotificationLevelPercent() * 1D;
			AshLogger.logFine("Notification level percent: "
					+ notificationLevelPercent);

			if ((formTypeData.getExportMethod() != null)
					&& (formTypeData.getExportMethod().getExportFormat() != null)
					&& (formTypeData.getExportMethod().getExportFormat()
							.getId() < 0)
					&& (formTypeData.getExportMethod().getExportFormat()
							.getName() != null)) {
				List exportFormats = AshSettingsControl.getInstance()
						.getExportFormats();

				for (i$ = exportFormats.iterator(); i$.hasNext();) {
					ExportFormat ef = (ExportFormat) i$.next();
					if (ef.getName().equalsIgnoreCase(
							formTypeData.getExportMethod().getExportFormat()
									.getName())) {
						formTypeData.getExportMethod().setExportFormat(ef);
					}

				}

			}

			if ((formTypeData.getExportMethod() != null)
					&& (formTypeData.getExportMethod().getImageFormat() != null)
					&& (formTypeData.getExportMethod().getImageFormat().getId() < 0)
					&& (formTypeData.getExportMethod().getImageFormat()
							.getName() != null)) {
				List imageFormats = AshSettingsControl.getInstance()
						.getImageFormats();

				for (i$ = imageFormats.iterator(); i$.hasNext();) {
					ImageFormat imgf = (ImageFormat) i$.next();
					if (imgf.getName().equalsIgnoreCase(
							formTypeData.getExportMethod().getImageFormat()
									.getName())) {
						formTypeData.getExportMethod().setImageFormat(imgf);
					}

				}

			}

			int operationResult = FormDBHandler.updateFormType(formTypeData);

			if (operationResult == -11) {
				result.setFormTypeOperationSuccessful(false);
				result.setFormTypeOperationMessage("No form type exists with the proposed form type name.");
				AshLogger
						.logSevere("No form type exists with the proposed form type name.");
			} else {
				result.setFormTypeOperationSuccessful(true);
			}
		} catch (HibernateException he) {
			result.setFormTypeOperationSuccessful(false);
			result.setFormTypeOperationMessage("Severe Database Error. Please contact System Administrator.");
			AshLogger.logSevere("Update failed. Got exception: "
					+ he.getMessage());
		}

		result.setFormTypeData(formTypeData);

		return result;
	}

	public boolean deletePad(PadFile pad) {
		return FormDBHandler.deletePad(pad);
	}

	public FormTypeResult deleteFormType(FormTypeData formTypeData) {
		AshLogger.logFine("About to delete form type");

		FormTypeResult result = new FormTypeResult();
		try {
			PadFile pad = new PadFile();
			pad.setFormType(formTypeData);
			List pads = getInstance().getPadFile(pad);

			for (Iterator i$ = pads.iterator(); i$.hasNext();) {
				PadFile currentPad = (PadFile) i$.next();
				deletePad(currentPad);
			}

			BackgroundFile background = new BackgroundFile();
			background.setFormType(formTypeData);
			List backgrounds = getInstance().getBackgroundFile(background);

			for (Iterator i$ = backgrounds.iterator(); i$.hasNext();) {
				BackgroundFile currentBackground = (BackgroundFile) i$.next();
				deleteBackground(currentBackground);
			}

			FormCopyData formCopy = new FormCopyData();
			formCopy.setFormType(formTypeData);

			List formcopies = getFormCopy(formCopy);

			if (formcopies.size() > 0)
				for (Iterator i$ = formcopies.iterator(); i$.hasNext();) {
					FormCopyData currentFormCopy = (FormCopyData) i$.next();
					deleteFormCopy(currentFormCopy);
				}

			int operationResult = FormDBHandler.deleteFormType(formTypeData);

			if (operationResult == -1) {
				result.setFormTypeOperationSuccessful(false);
				result.setFormTypeOperationMessage("Severe Database Error. Please contact System Administrator.");
				AshLogger.logSevere("Delete failed.");
			}

			result.setFormTypeOperationSuccessful(true);
		} catch (HibernateException he) {
			result.setFormTypeOperationSuccessful(false);
			result.setFormTypeOperationMessage("Severe Database Error. Please contact System Administrator.");
			AshLogger.logSevere("Update failed. Got exception: "
					+ he.getMessage());
		}

		result.setFormTypeData(formTypeData);

		return result;
	}

	public int updatePadFile(PadFile padFile) {
		AshLogger.logFine("About to update pad file");
		int operationResult = -1;
		try {
			operationResult = FormDBHandler.updatePadFile(padFile);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Get operation failed. Got exception: "
					+ he.getMessage());
		}

		return operationResult;
	}

	public List searchFormCopies(FormCopyData formCopyData, String sortOrder) {
		AshLogger.logFine("About to get form copies by searching");
		List formList = new ArrayList();
		try {
			formList = FormDBHandler.searchFormCopy(formCopyData, -1, "", 3);
		} catch (HibernateException he) {
			he.printStackTrace();
			AshLogger.logSevere("Search form copies failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe error when searching for form copies. Please contact System Administrator.");
		}

		if ((sortOrder != null)
				&& (((sortOrder.equals("form_desc")) || (sortOrder
						.equals("form_asc"))))) {
			Object[] objects;
			if (sortOrder.equals("form_asc")) {
				objects = formList.toArray();
				Sorter.sortByPageAddressAsc(objects);
				formList = new ArrayList();
				formList.addAll(Arrays.asList(objects));
			} else if (sortOrder.equals("form_desc")) {
				objects = formList.toArray();
				Sorter.sortByPageAddressDesc(objects);
				formList = new ArrayList();
				formList.addAll(Arrays.asList(objects));
			}
		}

		return formList;
	}

	public int getNumberUsedFormCopies(UserData user, FormTypeData formType) {
		int result = 0;

		AshLogger
				.logFine("About to get number of submitted form copies for formtype and/or user");

		List users = AshUserControl.getInstance().getUser(user);
		List formTypes = getInstance().getFormType(formType);

		if ((users.size() != 1) || (formTypes.size() != 1)) {
			return -1;
		}

		FormCopyData formCopy = new FormCopyData();
		formCopy.setOwner((UserData) users.get(0));
		formCopy.setFormType((FormTypeData) formTypes.get(0));

		List formcopies = searchFormCopies(formCopy, "");

		result = formcopies.size();
		AshLogger.logFine("Result: " + result);

		return result;
	}

	/*
	 * private void copyFile(File source, File dest) throws IOException { String
	 * sourceFile = source.getPath(); String destFile = dest.getPath();
	 * 
	 * FileChannel srcChannel = new FileInputStream(sourceFile).getChannel();
	 * 
	 * FileChannel dstChannel = new FileOutputStream(destFile).getChannel();
	 * 
	 * dstChannel.transferFrom(srcChannel, -1054183981748060160L,
	 * srcChannel.size());
	 * 
	 * srcChannel.close(); dstChannel.close();
	 * 
	 * dest.createNewFile(); }
	 */

	private boolean deleteBackground(BackgroundFile currentBackground) {
		return FormDBHandler.deleteBackground(currentBackground);
	}

	private void deleteFormCopy(FormCopyData currentFormCopy) {
		FormDBHandler.deleteFormCopy(currentFormCopy);
	}

	public List<FieldData> getVerificationStatus(FormCopyData formCopyData)
			throws IllegalStateException {
		ArrayList fieldsForVerification = new ArrayList();
		try {
			fieldsForVerification = getFieldsForVerification(formCopyData,
					AshCommons.getPenFromByteArray(formCopyData.getPgc()),
					formCopyData.getFormType(),
					formCopyData.getPageAddressRange());
		} catch (Exception e) {
			MailControl.getInstance().sendAdminMail(
					"Got exception when handling form "
							+ formCopyData.getPageAddressRange()
							+ " of formtype "
							+ formCopyData.getFormType().getFormTypeName()
							+ ". Error message: " + e.getMessage(),
					"Exception in Interpretation module!", null);

			throw new IllegalStateException(
					"Something went wrong. Please contact system administrator!");
		}

		return fieldsForVerification;
	}

	public FieldResult setVerificationStatus(FormCopyData formCopyData,
			FieldData correctedField) throws MalformedURLException {
		VOControl control = new VOControl();
		control.submitCorrectedFieldValue(correctedField);

		FieldResult vResult = new FieldResult();
		vResult.setFieldOperationSuccessful(true);

		return vResult;
	}

	public void getMandatoryFieldsStatus(FormCopyData formCopyData) {
		Pen pgcData = AshCommons.getPenFromByteArray(formCopyData.getPgc());
		String endPageAddress = formCopyData.getEndPageAddress();

		AshPgcControl pgcControl = new AshPgcControl();
		try {
			pgcControl.handleMandatoryFields(formCopyData, pgcData,
					getFormPages(endPageAddress));
		} catch (IllegalValueException ive) {
			AshLogger.logSevere(ive.getMessage());
			throw new IllegalStateException(
					"Severe Error! Please contact System Administrator.");
		}
	}

	public FormCopyResult forceCompleteFormCopy(FormCopyData formCopyData,
			boolean overrideMandatoryFields) {
		AshLogger.logFine("About to force completion of form copy "
				+ formCopyData.getPageAddressRange());
		FormCopyResult result = new FormCopyResult();

		AshLogger.logFine("Form copy page address range: "
				+ formCopyData.getPageAddressRange());
		AshLogger.logFine("Form copy owner: "
				+ formCopyData.getOwner().getUserName());
		try {
			AshPgcControl control = new AshPgcControl();

			Pen temporaryPgc = AshCommons.getPenFromByteArray(formCopyData
					.getTemporaryPgc());
			Pen pgc = AshCommons.getPenFromByteArray(formCopyData.getPgc());
			Pen mergedPgc = control.mergePgcs(temporaryPgc, pgc);

			int locked = (control.isFormLockedByUserArea(mergedPgc)) ? 1 : 0;
			formCopyData.setLocked(locked);

			formCopyData = AshCommons.removeTemporaryPgc(formCopyData);

			Page page = (Page) mergedPgc.getPages().next();
			String[] pages = getFormPages(page);

			formCopyData.setMarkedCompleted(1);

			if (overrideMandatoryFields) {
				formCopyData.setMandatoryFieldsMissing(0);
			} else {
				control.handleMandatoryFields(formCopyData, mergedPgc, pages);

				if (formCopyData.getMandatoryFieldsMissing() == 1)
					formCopyData.setMarkedCompleted(0);

			}

			AshLogger.logFine("Form is handled as completed");
			String verificationModule = AshProperties.verificationModule;

			if ((formCopyData.getMarkedCompleted() == 1)
					&& (formCopyData.getMandatoryFieldsMissing() == 0)) {
				AshLogger.logFine("About to send form to verification");
				if (verificationModule.equalsIgnoreCase("VISION OBJECTS")) {
					List fieldsForVerification = null;
					formCopyData.setVerificationInProgress(1);
					updateFormCopy(formCopyData);

					Pen pgcToInterpret = (temporaryPgc != null) ? temporaryPgc
							: pgc;
					formCopyData.setPgc(pgcToInterpret);

					VOInterpretationResult voResult = control
							.getFieldsForVerification(formCopyData, pages,
									false, "completed_in_gui", true);
					fieldsForVerification = voResult
							.getAllFieldsForVerification();

					int verificationNeeded = (fieldsForVerification.size() > 0) ? 1
							: 0;

					formCopyData.setVerificationInProgress(0);
					formCopyData.setVerificationNeeded(verificationNeeded);
				} else {
					formCopyData.setVerificationNeeded(0);

					if (temporaryPgc != null) {
						formCopyData.setPgc(temporaryPgc);
						exportFormCopy(formCopyData, "delta", true, false);
					}
				}

				boolean createImages = formCopyData.getVerificationNeeded() == 0;

				formCopyData.setPgc(mergedPgc);
				exportFormCopy(formCopyData, "total", createImages, true);
			}
		} catch (Exception ex) {
			result.setFormCopyOperationSuccessful(false);
			result.setFormCopyOperationMessage("Severe Error. Please Contact System administrator.");
			AshLogger.logSevere("Severe error. Got exception: "
					+ ex.getMessage());
		}

		updateFormCopy(formCopyData);
		result.setFormCopyData(formCopyData);
		result.setFormCopyOperationSuccessful(true);

		return result;
	}

	private ArrayList<FieldData> getFieldsForVerification(
			FormCopyData formCopyData, Pen pgc, FormTypeData formTypeData,
			String pageAddressRange) throws IllegalStateException,
			MalformedURLException, PageException, FormatException,
			IllegalValueException, NoSuchPageException, IOException {
		VOControl voControl = new VOControl();
		ArrayList fieldsForVerification = new ArrayList();

		fieldsForVerification = voControl.getFieldsForVerification(
				formCopyData, pageAddressRange, formTypeData.getFormTypeName());

		return fieldsForVerification;
	}

	private static String[] getFormPages(String submittedPage) {
		ArrayList pagesCurrentForm = new ArrayList();

		String paSubmittedPage = submittedPage;

		int pageNumberSubmittedPage = Integer.valueOf(
				submittedPage.substring(submittedPage.lastIndexOf(".") + 1,
						submittedPage.length())).intValue();
		String paSubmittedPageNoPage = paSubmittedPage.substring(0,
				paSubmittedPage.lastIndexOf(".")).trim();

		int numberOfPages = PadFileUtility.getPageAddresses(paSubmittedPage,
				"Anoto_Forms_Solution_ASH").length;
		AshLogger.logFine("Page " + paSubmittedPage
				+ " belongs to a form with " + numberOfPages + " pages");

		String additionalPageAddress = "";
		int pageNumberCurrentPage = pageNumberSubmittedPage;
		int index = pageNumberSubmittedPage % numberOfPages;

		for (int i = index; i > index - numberOfPages; --i) {
			pageNumberCurrentPage = pageNumberSubmittedPage - i;
			additionalPageAddress = paSubmittedPageNoPage + "."
					+ pageNumberCurrentPage;
			pagesCurrentForm.add(additionalPageAddress);
		}

		String[] formPageAddresses = (String[]) (String[]) pagesCurrentForm
				.toArray(new String[0]);
		Sorter.sortByPageAddressAsc(formPageAddresses);

		return formPageAddresses;
	}

	private String[] getFormPages(Page submittedPage) {
		ArrayList pagesCurrentForm = new ArrayList();

		String paSubmittedPage = submittedPage.getPageAddress();

		int pageNumberSubmittedPage = submittedPage.getPageNumber();
		String paSubmittedPageNoPage = paSubmittedPage.substring(0,
				paSubmittedPage.lastIndexOf(".")).trim();

		int numberOfPages = PadFileUtility.getPageAddresses(paSubmittedPage,
				"Anoto_Forms_Solution_ASH").length;

		String additionalPageAddress = "";
		int pageNumberCurrentPage = pageNumberSubmittedPage;
		int index = pageNumberSubmittedPage % numberOfPages;

		for (int i = index; i > index - numberOfPages; --i) {
			pageNumberCurrentPage = pageNumberSubmittedPage - i;
			additionalPageAddress = paSubmittedPageNoPage + "."
					+ pageNumberCurrentPage;
			pagesCurrentForm.add(additionalPageAddress);
		}

		String[] formPageAddresses = (String[]) (String[]) pagesCurrentForm
				.toArray(new String[0]);
		Sorter.sortByPageAddressAsc(formPageAddresses);

		return formPageAddresses;
	}

	// public void exportFormCopy(FormCopyData formCopy, String
	// additionalImageFileNameInfo, boolean createImages, boolean createExport)
	// throws MergeException, NoSuchPermissionException, IOException {
	public List exportFormCopy(FormCopyData formCopy,
			String additionalImageFileNameInfo, boolean createImages,
			boolean createExport) throws MergeException,
			NoSuchPermissionException, IOException {
		if ((!(formCopy.getFormType().getExportMethod().getExportFormat()
				.getName().equalsIgnoreCase(AshProperties.EXPORT_FORMAT_NONE)))
				|| (!(formCopy.getFormType().getExportMethod().getImageFormat()
						.getName()
						.equalsIgnoreCase(AshProperties.EXPORT_FORMAT_NONE)))) {
			AshLogger.logFine("About to export formcopy "
					+ formCopy.getPageAddressRange());

			FormCopyData oldFormCopy = null;
			try {
				if ((!(formCopy.getFormType().getExportMethod()
						.getExportFormat().getName()
						.equalsIgnoreCase(AshProperties.EXPORT_FORMAT_NONE)))
						&& (createExport)) {
					// Comentado por Wagner
					// Writer resultWriter;
					ResultDocument resultDoc = null;
					if (AshProperties.verificationModule
							.equalsIgnoreCase("VISION OBJECTS")) {
						resultDoc = VOFormProcessorToResultDocumentFactory
								.getDocument(new URL(
										AshProperties.VOFormsProcessorURL),
										formCopy.getPageAddressRange());
						resultDoc.getDocumentAttributes().setAttribute(
								"PageAddress", formCopy.getPageAddressRange());
						resultDoc.getDocumentAttributes().setAttribute("Date",
								formCopy.getLatestSubmit().toString());
						resultDoc.getDocumentAttributes().setAttribute(
								"PenSerial", formCopy.getOwner().getPenId());
						resultDoc.getDocumentAttributes().setAttribute(
								"Username", formCopy.getOwner().getUserName());
						resultDoc.getDocumentAttributes().setAttribute(
								"FormTypeName",
								formCopy.getFormType().getFormTypeName());
					} else if (AshProperties.verificationModule
							.equalsIgnoreCase("READSOFT")) {
						resultDoc = VOFormProcessorToResultDocumentFactory
								.getEmptyDocument();
					}

					FormCopyData tempFormCopy = new FormCopyData();
					tempFormCopy
							.setEndPageAddress(formCopy.getEndPageAddress());
					List oldFormCopyList = getInstance().searchFormCopies(
							tempFormCopy, "");

					if ((oldFormCopyList != null)
							&& (oldFormCopyList.size() > 0)) {
						oldFormCopy = (FormCopyData) oldFormCopyList.get(0);

						AshCommons.addDynamicDataToExport(resultDoc,
								oldFormCopy);
					}

					if ((resultDoc.getResultFields() != null)
							&& (resultDoc.getResultFields().size() > 0)) {
						return resultDoc.getResultFields();
					} else {
						AshLogger.logFine("Did not find any fields to export.");
					}

				}

				if ((!(formCopy.getFormType().getExportMethod()
						.getImageFormat().getName()
						.equalsIgnoreCase(AshProperties.IMAGE_FORMAT_NONE)))
						&& (createImages)) {
					FormCopyData merged = formCopy;
					AshPgcControl ashPgcControl = new AshPgcControl();

					if (oldFormCopy != null) {
						merged = ashPgcControl.mergeFormCopyPGCs(oldFormCopy,
								formCopy);
					}

					if ((formCopy.getFormType().getExportMethod() != null)
							&& (formCopy.getFormType().getExportMethod()
									.getType()
									.equalsIgnoreCase(ExportMethod.MAIL_EXPORT))) {
						String[] allPageAddresses = new String[0];
						try {
							Page writtenPage = (Page) AshCommons
									.getPenFromByteArray(formCopy.getPgc())
									.getPages().next();
							allPageAddresses = AshCommons
									.getFormPages(writtenPage);
						} catch (PageException ex) {
							Logger.getLogger(AshFormControl.class.getName())
									.log(Level.SEVERE, null, ex);
						}

						AshLogger
								.logFine("About to send png images to address: "
										+ formCopy.getFormType()
												.getExportMethod().getMailTo());

						AshMailHandler.handleForm(formCopy,
								Arrays.asList(allPageAddresses), true,
								additionalImageFileNameInfo);
					} else {
						AshCommons.createImages(merged,
								additionalImageFileNameInfo);
					}
				}
			} catch (MergeException ex) {
				AshLogger.logSevere("Error when merging pgc's, reason: "
						+ ex.getMessage());
			} catch (SQLException ex) {
				AshLogger
						.logSevere("Error when connecting to the FormProcessor, reason: "
								+ ex.getMessage());
			} catch (MalformedURLException ex) {
				AshLogger
						.logSevere("Error when connecting to the FormProcessor, reason: "
								+ ex.getMessage());
			}
			/*
			 * Comentado por Wagner } catch (FileNotFoundException fex) {
			 * AshLogger
			 * .logSevere("Error when creating the file for export, reason: " +
			 * fex.getMessage()); }
			 */
		}
		// Incluido por Wagner
		return null;
	}

	public void generateExampleXml(FormTypeData formType) {
		try {
			AshCommons.generateExampleXml(3, "myPrinter", formType,
					AshProperties.ashRootFolderPath);
		} catch (IOException ex) {
			Logger.getLogger(AshFormControl.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (JAXBException ex) {
			Logger.getLogger(AshFormControl.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (SAXException ex) {
			Logger.getLogger(AshFormControl.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (SQLException ex) {
			Logger.getLogger(AshFormControl.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public void addDynamicDataEntry(DynamicDataEntry dynamicDataEntry) {
		AshLogger.logFine("About to add dynamic data entry");
		FormCopyResult result = new FormCopyResult();

		try {
			int operationResult = FormDBHandler
					.addDynamicDataEntry(dynamicDataEntry);

			if (operationResult == -1) {
				AshLogger.logSevere("Operation failed.");
				throw new IllegalStateException(
						"Severe Error. Please contact system administrator.");
			}

		} catch (HibernateException he) {
			result.setFormCopyOperationSuccessful(false);
			result.setFormCopyOperationMessage("Add Operation failed.");
			AshLogger
					.logSevere("Add failed. Got exception: " + he.getMessage());
		}
	}

	public List searchDynamicDataEntry(DynamicDataEntry dynamicDataEntry) {
		AshLogger.logFine("About to get dynamic data entry by searching");
		List list = new ArrayList();
		try {
			list = FormDBHandler.searchDynamicDataEntry(dynamicDataEntry);
		} catch (HibernateException he) {
			AshLogger
					.logSevere("Search dynamic data entry failed. Got exception: "
							+ he.getMessage());
			throw new IllegalStateException(
					"Severe error when searching for dynamic data entry. Please contact System Administrator.");
		}

		return list;
	}

	public void addFormCopiesPageAreas(List listFormCopiesPageAreas) {
		AshLogger.logFine("About to add formcopies page areas");

		try {
			FormDBHandler.addFormCopiesPageAreas(listFormCopiesPageAreas);

		} catch (HibernateException he) {
			AshLogger
					.logSevere("Add failed. Got exception: " + he.getMessage());
		}
	}

	public List searchFormCopiesPageAreas(
			FormCopyPageArea formCopiesPageAreas) {
		AshLogger.logFine("About to get formcopies page areas by searching");
		List list = new ArrayList();

		try {
			list = FormDBHandler.searchFormCopiesPageAreas(formCopiesPageAreas);
		} catch (HibernateException he) {
			AshLogger
					.logSevere("Search formcopies page areas failed. Got exception: "
							+ he.getMessage());
			throw new IllegalStateException(
					"Severe error when searching for formcopies page areas. Please contact System Administrator.");
		}

		return list;
	}

	public List searchFormCopies(FormCopyData formCopies) {
		AshLogger.logFine("About to get formcopies by searching");
		List list = new ArrayList();

		try {
			list = FormDBHandler.searchFormCopies(formCopies);
		} catch (HibernateException he) {
			AshLogger.logSevere("Search formcopies failed. Got exception: "
					+ he.getMessage());
			throw new IllegalStateException(
					"Severe error when searching for formcopies. Please contact System Administrator.");
		}

		return list;
	}

	public String getMaxPageAddress(String formTypeName) {
		AshLogger.logFine("About to get max page address by searching");
		String result = null;

		try {
			result = FormDBHandler.getMaxPageAddress(formTypeName);
		} catch (HibernateException he) {
			AshLogger
					.logSevere("Search max page address failed. Got exception: "
							+ he.getMessage());
			throw new IllegalStateException(
					"Severe error when searching for max page address. Please contact System Administrator.");
		}

		return result;
	}

	public int getCountPageAddress(String pageAddress) {
		AshLogger.logFine("About to get count page address by searching");
		int result = 0;

		try {
			result = FormDBHandler.getCountPageAddress(pageAddress);
		} catch (HibernateException he) {
			AshLogger
					.logSevere("Search count page address failed. Got exception: "
							+ he.getMessage());
			throw new IllegalStateException(
					"Severe error when searching for count page address. Please contact System Administrator.");
		}

		return result;
	}

	public BigInteger getNextValSeq(String nmSequence) {
		AshLogger.logFine("About to get next value sequence");
		BigInteger result = null;

		try {
			result = FormDBHandler.getNextValSeq(nmSequence);
		} catch (HibernateException he) {
			AshLogger
					.logSevere("Get next value sequence failed. Got exception: "
							+ he.getMessage());
			throw new IllegalStateException(
					"Severe error when get next value sequence. Please contact System Administrator.");
		}

		return result;
	}
	
}