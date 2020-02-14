package com.anoto.ash;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.anoto.api.NoSuchPermissionException;
import com.anoto.api.Pen;
import com.anoto.api.PenHome;
import com.anoto.api.util.PageAddress;
import com.anoto.ash.database.BackgroundFile;
import com.anoto.ash.database.FieldData;
import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.FormTypeToXMLExporter;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.portal.result.FormCopyResult;
import com.anoto.ash.vo.ink.VOControl;
import com.anoto.ash.vo.ink.VOInterpretationResult;
import com.anoto.javax.servlet.http.AnotoRouterServlet;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AshBluetoothServlet extends AnotoRouterServlet {

	private static final long serialVersionUID = 1L;
	private static Map formCopiesLists;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		//ServletContext servletContext = config.getServletContext();
/*
		try {
			System.setProperty(
					"javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
					"org.apache.xerces.jaxp.validation.XMLSchemaFactory");

			AshSetupControl.startAll();
			String webRoot = config.getServletContext().getRealPath("");
			FileInputStream bundledPadStream = new FileInputStream(webRoot
					+ "\\storage\\bundled.pad");
			try {
				PenHome.loadPad(AshProperties.appName, bundledPadStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			servletContext
					.log("SEVERE ERROR: The ASH environment is not configured correctly, reason: "
							+ e.getMessage());
			throw new IllegalStateException(
					"SEVERE ERROR: The ASH environment is not configured correctly.reason: ");
		}
		*/
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String feedBackType = request.getParameter("ft");
		getServletContext().log("context doGet " + feedBackType);
		if (feedBackType != null) {
			super.doGet(request, response);
		} else {
			if (!(AshSetupControl.propertiesInitialized)) {
				AshSetupControl.initializeProperties();
			}

			String contextPath = request.getContextPath();
			String servletPath = request.getServletPath();
			String path = contextPath + servletPath;

			String requestURI = request.getRequestURI();
			String pdfName = request.getParameter("pdf");
			String backgroundName = request.getParameter("img");
			String formTypeName = request.getParameter("formtype");

			String relativeRequestURI = requestURI.substring(path.length());

			if ((relativeRequestURI
					.equals(AshProperties.EXPORT_FORM_TYPES_TO_XML_FUNCTION))
					&& (((pdfName == null) || (pdfName.equals(""))))
					&& (((backgroundName == null) || (backgroundName.equals(""))))) {
				String root = getServletContext().getRealPath("/") + "xml\\";
				File f = new File(root);

				if (!(f.exists())) {
					f.mkdir();
				}

				FormTypeToXMLExporter.getInstance().exportToXML(root);
				File xml = new File(getServletContext().getRealPath("/")
						+ "\\xml\\formTypes.xml");
				FileInputStream fis = new FileInputStream(xml);

				response.setContentType("application/xml");
				response.setContentLength((int) xml.length());
				int tmp = 0;

				while ((tmp = fis.read()) > -1)
					response.getOutputStream().write(tmp);
			} else {
				FormTypeData formTypeData;
				if ((pdfName != null) && (!(pdfName.equals("")))) {
					try {
						formTypeData = new FormTypeData();
						formTypeData.setFormTypeName(formTypeName);
						formTypeData = (FormTypeData) AshFormControl
								.getInstance().getFormType(formTypeData).get(0);

						InputStream is = new ByteArrayInputStream(
								formTypeData.getPdfFile());

						if ((is != null) && (is.available() > 0)) {
							response.setContentType("application/pdf");
							response.setContentLength(is.available());
							int tmp = 0;
							while ((tmp = is.read()) > -1)
								response.getOutputStream().write(tmp);
						} else {
							processError(
									request,
									response,
									"Could not find the PDF file with the name "
											+ pdfName
											+ ", please contact the system administrator.");
						}
					} catch (Exception e) {
						processError(
								request,
								response,
								"Could not find a PDF file for the form type, please contact the system administrator.");
					}
				} else if ((backgroundName != null)
						&& (!(backgroundName.equals("")))
						&& (formTypeName != null)
						&& (!(formTypeName.equals(""))))
					try {
						formTypeData = new FormTypeData();
						formTypeData.setFormTypeName(formTypeName);
						formTypeData = (FormTypeData) AshFormControl
								.getInstance().getFormType(formTypeData).get(0);

						BackgroundFile bgFile = new BackgroundFile();
						bgFile.setFileName(backgroundName);
						bgFile.setFormType(formTypeData);

						bgFile = (BackgroundFile) AshFormControl.getInstance()
								.getBackgroundFile(bgFile).get(0);

						InputStream is = new ByteArrayInputStream(
								bgFile.getBackgroundFile());

						if ((is != null) && (is.available() > 0)) {
							response.setContentType("image/png");
							response.setContentLength(is.available());
							int tmp = 0;

							while ((tmp = is.read()) > -1)
								response.getOutputStream().write(tmp);
						} else {
							AshLogger
									.logSevere("Error when processing a request in AshBluetoothServlet, request URI = "
											+ requestURI);
							processError(
									request,
									response,
									"There was an error when processing your request, please contact the system administrator.");
						}
					} catch (Exception e) {
						processError(
								request,
								response,
								"Could not find the background image with the name, please contact the system administrator.");
					}
				else
					processError(
							request,
							response,
							"The was an error processing your request, please contact the system administrator.");
			}
		}
	}

	private void handleIncorrectFormType(HttpServletRequest request,
			FormCopyData formCopyData, long activityKey, String timestamp) {
		String serviceName = formCopyData.getFormType().getFormTypeName() + "|"
				+ formCopyData.getPageAddressRange();
		String instruction = "Bad Form type";
		String defaultValue = formCopyData.getFeedbackMessage();
		String feedBackType = AshProperties.NO_INTERACTION;
		String actions = formatAction(request, encode("x") + "&ft="
				+ encode(feedBackType) + "&listKey=" + encode(timestamp));
		String imagePath = "";
		String constraints = "";

		HashMap formCopiesList = (HashMap) formCopiesLists.get(timestamp);
		formCopiesList.remove(Long.valueOf(activityKey));

		Iterator formCopyIterator = formCopiesList.values().iterator();

		if (!(formCopyIterator.hasNext())) {
			formCopiesLists.remove(timestamp);
		}

		setActivityInteraction(request, activityKey, serviceName, instruction,
				actions, imagePath, constraints, defaultValue);
	}

	private void handleIllegalPen(HttpServletRequest request,
			FormCopyData formCopyData, long activityKey, String timestamp) {
		String serviceName = formCopyData.getFormType().getFormTypeName() + "|"
				+ formCopyData.getPageAddressRange();
		String instruction = "Illegal Pen";
		String defaultValue = formCopyData.getFeedbackMessage();
		String feedBackType = AshProperties.NO_INTERACTION;
		String actions = formatAction(request, encode("x") + "&ft="
				+ encode(feedBackType) + "&listKey=" + encode(timestamp));
		String imagePath = "";
		String constraints = "";

		HashMap formCopiesList = (HashMap) formCopiesLists.get(timestamp);
		formCopiesList.remove(Long.valueOf(activityKey));

		Iterator formCopyIterator = formCopiesList.values().iterator();

		if (!(formCopyIterator.hasNext())) {
			formCopiesLists.remove(timestamp);
		}

		setActivityInteraction(request, activityKey, serviceName, instruction,
				actions, imagePath, constraints, defaultValue);
	}

	private void processError(HttpServletRequest request,
			HttpServletResponse response, String errorMessage)
			throws ServletException, IOException {
		AshLogger.logSevere("Error in AshBluetoothServlet: " + errorMessage);

		HttpSession session = request.getSession();

		session.setAttribute("errorMessage", errorMessage);

		response.sendRedirect("/afs/ErrorMessage.jsp");
	}

	public void onPGC(HttpServletRequest request, Pen pen, String itemName)
			throws ServletException {
		getServletContext().log("Inside onPgc");

		AshPgcControl pgcControl = new AshPgcControl();
		try {
			HashMap formCopies = pgcControl.processPgc(pen);
			System.gc();
			handleFormCopies(request, formCopies, pen);
		} catch (Exception e) {
			// System.out.println("###Mensagem: " + e.getMessage());
			// System.out.println("###Causa: " + e.getCause());
			// e.printStackTrace();

			try {
				handleException(e);
			} catch (Exception ex) {
				throw new ServletException("ASH got exception: "
						+ e.getMessage());
			}
		}
	}

	private void handleFeedbackFormCopies(HttpServletRequest request,
			HashMap formCopies, String timestamp) {
		if (formCopies.size() > 0) {
			HashMap formCopiesClone = new HashMap();
			formCopiesClone.putAll(formCopies);
			Iterator feedBackFormCopiesIterator = formCopiesClone.values()
					.iterator();

			while (feedBackFormCopiesIterator.hasNext()) {
				FormCopyData formCopyData = (FormCopyData) feedBackFormCopiesIterator
						.next();
				handleFormCopy(request, formCopyData, timestamp);
			}
		} else {
			setActivityFeedback(request, -1L, "Thank you!", "",
					Integer.valueOf(2), "", false);
		}
	}

	private void handleFormCopy(HttpServletRequest request,
			FormCopyData formCopyData, String timestamp) {
		String formTypeName = formCopyData.getFormType().getFormTypeName();
		String endPageAddress = formCopyData.getEndPageAddress();
		String pageAddressRange = formCopyData.getPageAddressRange();
		ArrayList missingMandatoryFields = formCopyData
				.getMissingMandatoryFields();
		ArrayList missingMandatoryOrFieldGroups = formCopyData
				.getMissingMandatoryOrFieldGroups();
		VOInterpretationResult interpretationResult = formCopyData
				.getInterpretationResult();

		String serviceName = formTypeName + "|" + pageAddressRange;
		long key = new PageAddress(endPageAddress).longValue();

		if (formCopyData.getIncorrectFormType()) {
			handleIncorrectFormType(request, formCopyData, key, timestamp);
		} else if (formCopyData.getIllegalPen()) {
			handleIllegalPen(request, formCopyData, key, timestamp);
		} else if (formCopyData.verificationFailed()) {
			handleFailedInterpretation(request, formCopyData, key, timestamp);
		} else if (formCopyData.isPostCompletionDataNotCompleted()) {
			handlePBVNotCompleted(request, formCopyData, key, timestamp);
		} else if (formCopyData.getMandatoryFieldsMissing() == 1) {
			handleMandatoryFieldsFeedback(request, missingMandatoryFields,
					missingMandatoryOrFieldGroups, formTypeName,
					pageAddressRange, key, timestamp);
		} else if ((interpretationResult.isVerificationNeeded())
				&& (interpretationResult.getEndUserFieldsForVerification()
						.size() > 0)
				&& (formCopyData.getFormType().getAllowPaperBasedVerification() != 1)) {
			startFormVerification(request, interpretationResult, formTypeName,
					pageAddressRange, key, timestamp);
		} else if ((interpretationResult.isVerificationNeeded())
				&& (interpretationResult.getEndUserFieldsForVerification()
						.size() > 0)
				&& (formCopyData.getFormType().getAllowPaperBasedVerification() == 1)) {
			handlePaperVerifiedFeedback(request, interpretationResult,
					formTypeName, pageAddressRange, key, timestamp);
		} else {
			String message = "";
			String formStatus = (formCopyData.getMarkedCompleted() == 1) ? "completed"
					: "non-completed";

			if (!(formCopyData.getFeedbackMessage()
					.contains("No more data can be added."))) {
				message = "Form handled as " + formStatus + ".";
			}

			if ((formCopyData.getFeedbackMessage() != null)
					&& (formCopyData.getFeedbackMessage().length() > 0)) {
				message = message + formCopyData.getFeedbackMessage();
			}

			setActivityFeedback(request, key, serviceName, message,
					Integer.valueOf(2), "", false);
		}
	}

	private void handleFailedInterpretation(HttpServletRequest request,
			FormCopyData formCopyData, long activityKey, String timestamp) {
		String serviceName = formCopyData.getFormType().getFormTypeName() + "|"
				+ formCopyData.getPageAddressRange();
		String instruction = "Interpretation failed!";
		String defaultValue = "Please contact System Administrator";
		String feedBackType = AshProperties.NO_INTERACTION;
		String actions = formatAction(request, encode("x") + "&ft="
				+ encode(feedBackType) + "&listKey=" + encode(timestamp));
		String imagePath = "";
		String constraints = "";

		HashMap formCopiesList = (HashMap) formCopiesLists.get(timestamp);
		formCopiesList.remove(Long.valueOf(activityKey));

		Iterator formCopyIterator = formCopiesList.values().iterator();

		if (!(formCopyIterator.hasNext())) {
			formCopiesLists.remove(timestamp);
		}

		setActivityInteraction(request, activityKey, serviceName, instruction,
				actions, imagePath, constraints, defaultValue);
	}

	private void handlePBVNotCompleted(HttpServletRequest request,
			FormCopyData formCopyData, long activityKey, String timestamp) {
		String serviceName = formCopyData.getFormType().getFormTypeName() + "|"
				+ formCopyData.getPageAddressRange();
		String instruction = "The pidget must be ticked!";
		String defaultValue = "Your information is saved but the pidget must be ticked";
		String feedBackType = AshProperties.NO_INTERACTION;
		String actions = formatAction(request, encode("x") + "&ft="
				+ encode(feedBackType) + "&listKey=" + encode(timestamp));
		String imagePath = "";
		String constraints = "";

		HashMap formCopiesList = (HashMap) formCopiesLists.get(timestamp);
		formCopiesList.remove(Long.valueOf(activityKey));

		Iterator formCopyIterator = formCopiesList.values().iterator();

		if (!(formCopyIterator.hasNext())) {
			formCopiesLists.remove(timestamp);
		}

		setActivityInteraction(request, activityKey, serviceName, instruction,
				actions, imagePath, constraints, defaultValue);
	}

	private void handleMandatoryFieldsFeedback(HttpServletRequest request,
			ArrayList missingMandatoryFields,
			ArrayList missingMandatoryOrFieldGroups, String formTypeName,
			String pageAddressRange, long activityKey, String timestamp) {
		getServletContext().log("Inside handleMandatoryFieldsFeedback.");
		StringBuffer feedbackMessage = new StringBuffer();

		for (int j = 0; j < missingMandatoryFields.size(); ++j) {
			FieldData neglectedMandatoryField = (FieldData) missingMandatoryFields
					.get(j);
			String fieldName = neglectedMandatoryField.getFieldName();
			int pageNumber = neglectedMandatoryField.getPage();
			feedbackMessage.append(fieldName + " p" + pageNumber + ",");
		}

		for (int i = 0; i < missingMandatoryOrFieldGroups.size(); ++i) {
			ArrayList missingOrFieldGroup = (ArrayList) missingMandatoryOrFieldGroups
					.get(i);

			feedbackMessage.append("|One of: ");

			for (int k = 0; k < missingOrFieldGroup.size(); ++k) {
				FieldData fieldInCurrentGroup = (FieldData) missingOrFieldGroup
						.get(k);
				String fieldName = fieldInCurrentGroup.getFieldName();
				int pageNumber = fieldInCurrentGroup.getPage();
				feedbackMessage.append(fieldName + " p" + pageNumber + ",");
			}
		}

		String serviceName = formTypeName + "|" + pageAddressRange;
		String instruction = "Missing fields";
		String defaultValue = feedbackMessage.toString();
		String feedBackType = AshProperties.NO_INTERACTION;
		String actions = formatAction(request, encode("x") + "&ft="
				+ encode(feedBackType) + "&listKey=" + encode(timestamp));
		String imagePath = "";
		String constraints = "";

		HashMap formCopiesList = (HashMap) formCopiesLists.get(timestamp);
		formCopiesList.remove(Long.valueOf(activityKey));

		Iterator formCopyIterator = formCopiesList.values().iterator();

		if (!(formCopyIterator.hasNext())) {
			formCopiesLists.remove(timestamp);
		}

		setActivityInteraction(request, activityKey, serviceName, instruction,
				actions, imagePath, constraints, defaultValue);
	}

	private void handlePaperVerifiedFeedback(HttpServletRequest request,
			VOInterpretationResult interpretationResult, String formTypeName,
			String pageAddressRange, long activityKey, String timestamp) {
		StringBuffer feedbackMessage = new StringBuffer();
		List allFieldsForVer = interpretationResult
				.getEndUserFieldsForVerification();

		for (Iterator i$ = allFieldsForVer.iterator(); i$.hasNext();) {
			FieldData field = (FieldData) i$.next();
			feedbackMessage
					.append(field.getFieldName())
					.append(" p")
					.append(field.getPage())
					.append(": ")
					.append(field.getInterpretationResult()
							.getInterpretationValue()).append(",");
		}

		String serviceName = formTypeName + "|" + pageAddressRange;
		String instruction = "Verification needed";
		String defaultValue = feedbackMessage.toString();
		String feedBackType = AshProperties.NO_INTERACTION;
		String actions = formatAction(request, encode("x") + "&ft="
				+ encode(feedBackType) + "&listKey=" + encode(timestamp));
		String imagePath = "";
		String constraints = "";

		HashMap formCopiesList = (HashMap) formCopiesLists.get(timestamp);
		formCopiesList.remove(Long.valueOf(activityKey));

		Iterator formCopyIterator = formCopiesList.values().iterator();

		if (!(formCopyIterator.hasNext())) {
			formCopiesLists.remove(timestamp);
		}

		setActivityInteraction(request, activityKey, serviceName, instruction,
				actions, imagePath, constraints, defaultValue);
	}

	private void startFormVerification(HttpServletRequest request,
			VOInterpretationResult interpretationResult, String formTypeName,
			String pageAddressRange, long activityKey, String timestamp) {
		ArrayList formVerificationFields = interpretationResult
				.getEndUserFieldsForVerification();

		FieldData firstVerificationField = (FieldData) formVerificationFields
				.get(0);

		String fieldValue = firstVerificationField.getFieldValue();
		int pageNumber = firstVerificationField.getPage();
		String fieldName = firstVerificationField.getFieldName();
		String invalidFieldsFound = "false";

		String[] possibleValues = null;

		if ((firstVerificationField.getPossibleValues() != null)
				&& (firstVerificationField.getPossibleValues().size() > 0)) {
			List possibleValuesList = firstVerificationField
					.getPossibleValues();
			possibleValues = (String[]) (String[]) possibleValuesList
					.toArray(new String[0]);
		}

		int fieldCounter = 1;

		String serviceName = formTypeName + "|" + pageAddressRange;
		String instruction = "Confirm " + fieldName + ", page " + pageNumber;
		String defaultValue = fieldValue;
		String feedBackType = AshProperties.VERIFICATION;
		String actions = formatAction(request, encode("x") + "&ft="
				+ encode(feedBackType) + "&fieldCounter="
				+ encode(fieldCounter) + "&listKey=" + encode(timestamp)
				+ "&invalidFieldsFound=" + encode(invalidFieldsFound));

		String imagePath = "";
		String constraints = "";

		if ((possibleValues == null) || (possibleValues.length == 0))
			setActivityInteraction(request, activityKey, serviceName,
					instruction, actions, imagePath, constraints, defaultValue);
		else
			setActivityMultipleChoice(request, activityKey, serviceName,
					instruction, actions, imagePath, constraints,
					possibleValues);
	}

	public void onInteraction(HttpServletRequest request, long key,
			String action, String value) throws ServletException, IOException {
		getServletContext().log("Inside onInteraction.");
		String feedBackType = request.getParameter("ft");
		if ((feedBackType != null)
				&& (feedBackType.equalsIgnoreCase(AshProperties.VERIFICATION))) {
			String listKey = request.getParameter("listKey");

			HashMap formCopiesList = (HashMap) formCopiesLists.get(listKey);

			FormCopyData formCopyData = (FormCopyData) formCopiesList.get(Long
					.valueOf(key));

			String formTypeName = formCopyData.getFormType().getFormTypeName();

			String pageAddressRange = formCopyData.getPageAddressRange();

			String serviceName = formTypeName + "|" + pageAddressRange;

			int fieldCounter = Integer.parseInt(request
					.getParameter("fieldCounter"));
			String invalidFieldsFound = request
					.getParameter("invalidFieldsFound");

			VOInterpretationResult interpretationResult = formCopyData
					.getInterpretationResult();

			ArrayList fieldsToVerify = interpretationResult
					.getEndUserFieldsForVerification();

			FieldData verifiedField = (FieldData) fieldsToVerify
					.get(fieldCounter - 1);

			verifiedField.setFieldValue(value);

			VOControl control = new VOControl();

			boolean fieldInvalid = !(control
					.submitCorrectedFieldValue(verifiedField));

			if (!(invalidFieldsFound.equalsIgnoreCase("true"))) {
				invalidFieldsFound = "" + fieldInvalid;
			}

			int nbrOfVerificationFields = fieldsToVerify.size();

			int nbrOfFieldsLeftToVerify = nbrOfVerificationFields
					- fieldCounter;

			if (nbrOfFieldsLeftToVerify > 0) {
				++fieldCounter;

				int fieldIndex = fieldCounter - 1;

				FieldData nextVerificationField = (FieldData) fieldsToVerify
						.get(fieldIndex);
				String nextFieldValue = nextVerificationField.getFieldValue();
				int nextPageNumber = nextVerificationField.getPage();
				String nextFieldName = nextVerificationField.getFieldName();

				String[] possibleValues = null;

				if ((nextVerificationField.getPossibleValues() != null)
						&& (nextVerificationField.getPossibleValues().size() > 0)) {
					List possibleValuesList = nextVerificationField
							.getPossibleValues();
					possibleValues = (String[]) (String[]) possibleValuesList
							.toArray(new String[0]);
				}

				String defaultValue = nextFieldValue;
				String actions = formatAction(request, encode("x") + "&ft="
						+ encode(feedBackType) + "&fieldCounter="
						+ encode(fieldCounter) + "&listKey=" + encode(listKey)
						+ "&invalidFieldsFound=" + encode(invalidFieldsFound));

				String imagePath = "";
				String constraints = "";
				String instruction = "Confirm " + nextFieldName + ", page "
						+ nextPageNumber;

				if ((possibleValues == null) || (possibleValues.length == 0))
					setActivityInteraction(request, key, serviceName,
							instruction, actions, imagePath, constraints,
							defaultValue);
				else
					setActivityMultipleChoice(request, key, serviceName,
							instruction, actions, imagePath, constraints,
							possibleValues);

			} else {
				AshFormControl formControl = AshFormControl.getInstance();

				if (invalidFieldsFound.equalsIgnoreCase("false")) {
					if (interpretationResult.isOnlyEndUserFields()) {
						formCopyData.setVerificationNeeded(0);

						FormCopyResult result = formControl
								.updateFormCopy(formCopyData);
						try {
							formControl.exportFormCopy(formCopyData, "total",
									true, true);
						} catch (Exception e) {
							throw new ServletException(e.getMessage());
						}

						if (!(result.isFormCopyOperationSuccessful()))
							throw new IllegalStateException(
									"Severe database error. Please contact System administrator.");

					}

				} else {
					throw new ServletException(
							"Something went wrong when sending verified results to the interpretation engine");
				}

				setActivityFeedback(request, key, serviceName,
						"Form verification complete", Integer.valueOf(2), "",
						false);
			}
		} else {
			setAshConfirmation(request);
		}
	}

	private void handleFormCopies(HttpServletRequest request,
			HashMap formCopies, Pen pen) {
		String listKey = System.currentTimeMillis() + "";
		try {
			listKey = System.currentTimeMillis()
					+ pen.getPenData().getPenSerial().replaceAll("-", "");
		} catch (NoSuchPermissionException nspe) {
		}
		boolean ashConfirmationGiven = false;

		Iterator keyIterator = formCopies.keySet().iterator();
		HashMap feedbackFormCopies = new HashMap();

		while (keyIterator.hasNext()) {
			Long currentKey = (Long) keyIterator.next();
			FormCopyData formCopyData = (FormCopyData) formCopies
					.get(currentKey);
			String serviceName = "|"
					+ formCopyData.getFormType().getFormTypeName() + "|"
					+ formCopyData.getPageAddressRange();

			if ((formCopyData.getMandatoryFieldsMissing() == 0)
					&& (formCopyData.getVerificationNeeded() == 0)
					&& (!(formCopyData.getIllegalPen()))
					&& (formCopyData.getLocked() == 0)
					&& (!(formCopyData.getIncorrectFormType()))
					&& (!(formCopyData.getFeedbackMessage()
							.contains("No more data can be added.")))) {
				ashConfirmationGiven = true;

				String formStatus = (formCopyData.getMarkedCompleted() == 1) ? "completed"
						: "non-completed";
				String message = "Form handled as " + formStatus;

				if ((formCopyData.getFeedbackMessage() != null)
						&& (formCopyData.getFeedbackMessage().length() > 0))
					message = message + ". "
							+ formCopyData.getFeedbackMessage();

				try {
					Thread.sleep(50L);
				} catch (Exception e) {
				}
				long feedbackKey = new PageAddress(
						formCopyData.getEndPageAddress()).longValue();
				try {
					setActivityFeedback(request, feedbackKey, serviceName,
							message, Integer.valueOf(2), "", false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ((formCopyData.getMandatoryFieldsMissing() == 1)
					|| (formCopyData.getMarkedCompleted() != 0)
					|| (formCopyData.isPostCompletionDataNotCompleted())
					|| (formCopyData.getIncorrectFormType())) {
				feedbackFormCopies.put(currentKey, formCopyData);
			}

		}

		formCopiesLists.put(listKey, feedbackFormCopies);

		if (feedbackFormCopies.size() > 0)
			handleFeedbackFormCopies(request, feedbackFormCopies, listKey);
		else if (!(ashConfirmationGiven)) {
			setActivityFeedback(request, -1L, " Thank you!", "",
					Integer.valueOf(2), "", false);
		}
	}

	private void handleException(Exception e) throws Exception {
		MailControl mailControl = MailControl.getInstance();
		if (e instanceof AshException) {
			AshException ashException = (AshException) e;
			Exception wrappedException = ashException.getWrappedException();

			getServletContext().log(
					"AshServlet forced to handle exception: "
							+ wrappedException.toString());
			mailControl.sendAdminMail(
					"ASH got exception! Provided error message: "
							+ wrappedException.toString(),
					AshProperties.adminErrorMailSubject, null);
		} else {
			getServletContext().log(
					"AshServlet forced to handle exception: " + e.toString());
			mailControl.sendAdminMail(
					"ASH got exception! Exception error message: "
							+ e.toString(),
					AshProperties.adminErrorMailSubject, null);
		}

		throw e;
	}

	public void onException(HttpServletResponse response, Exception e)
			throws ServletException, IOException {
		response.sendError(400, e.getMessage());
		logger.error("HTTP 400", e);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String pgcFileName = "";
		File pgcFile = null;
		FileInputStream fis = null;

		try {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			List<FileItem> fileItems = upload.parseRequest(req);
			Iterator itr = fileItems.iterator();
			while (itr.hasNext()) {
				FileItem item = (FileItem) itr.next();
				if (item.isFormField()) {
					System.out.println("Campo = " + item.getFieldName()
							+ ", Valor = " + item.getString());
				} else {
					// Write the file to disk on webserver.
					pgcFileName = getPgcFileName(req);
					pgcFile = new File(AshProperties.PGC_FOLDER, pgcFileName
							+ AshProperties.PGC_FILE_EXT);
					item.write(pgcFile);
				}
			}
			fis = new FileInputStream(pgcFile);
			Pen pen = PenHome.read(fis, AshProperties.appName);
			String penId = pen.getPenData().getPenSerial();
			Short version = pen.getPenData().getSoftwareVersion();
			fis.close();

			// if (AshFormControl.getInstance().isCanetaAtiva(penId)) {
			pgcFileName += "_" + penId + "_" + version
					+ AshProperties.PGC_FILE_EXT;
			pgcFile.renameTo(new File(AshProperties.PGC_FOLDER_RECEIVED,
					pgcFileName));
			res.setStatus(HttpServletResponse.SC_OK);
			ServletOutputStream sos = res.getOutputStream();
			sos.print("OK");
			sos.close();
			// } else {
			// pgcFile.delete();
			// String message = "A caneta '" + penId +
			// "' não está ativa no sistema!";
			// AshLogger.logSevere(message);
			// res.sendError(400, message);
			// }

		} catch (Exception e) {
			if (pgcFile != null && pgcFile.canRead())
				pgcFile.delete();
			AshLogger.logSevere("ERRO ao armazenar o arquivo PGC: " + pgcFileName );
			//Aldisney
			logger.error("ERRO ao armazenar o arquivo PGC: " + pgcFileName , e);
			//O arquivo foi salvo, mas deve estar baleado, gerar um log... e liberar o tablet para reenviar novos arquivos
			res.setStatus(HttpServletResponse.SC_OK);
			ServletOutputStream sos = res.getOutputStream();
			sos.print("OK");
			sos.close();
			
			// Aldisney onException(res, e);
		}
	}

	// Wagner - Inclui
	private static synchronized String getPgcFileName(HttpServletRequest req)
			throws Exception {
		// Pausa de 1ms
		Thread.sleep(1L);

		String pgcFileName = AshCommons
				.getCurrentDateAndTime("yyyy-MM-dd_HH.mm.ss(SSS)")
				+ "_"
				+ (req.getRemoteAddr().replaceAll(":", "."));

		return pgcFileName;
	}

	/**
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo() {
		return "PGC file handling servlet";
	}

	private String encode(String value) {
		StringBuffer resultBuffer = new StringBuffer();
		String preserveChars = "";
		super.encode(resultBuffer, value, preserveChars);
		return resultBuffer.toString();
	}

	private String encode(int intValue) {
		StringBuffer resultBuffer = new StringBuffer();
		String preserveChars = "";
		super.encode(resultBuffer, Integer.toString(intValue), preserveChars);
		return resultBuffer.toString();
	}

	protected String getAshName() {
		return AshProperties.appName;
	}

	public void destroy() {
		UnsentMailSender.getInstance().stopRunning();
		super.destroy();
	}

	static {
		formCopiesLists = Collections.synchronizedMap(new HashMap());
	}
}