package com.anoto.ash;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.anoto.api.CoreAdapter;
import com.anoto.api.FormatException;
import com.anoto.api.IllegalValueException;
import com.anoto.api.MergeException;
import com.anoto.api.NoSuchAreaException;
import com.anoto.api.NoSuchPageException;
import com.anoto.api.NoSuchPermissionException;
import com.anoto.api.NoSuchPropertyException;
import com.anoto.api.Page;
import com.anoto.api.PageArea;
import com.anoto.api.PageAreaException;
import com.anoto.api.PageException;
import com.anoto.api.Pen;
import com.anoto.api.PenCreationException;
import com.anoto.api.PenHome;
import com.anoto.api.PenOwnerData;
import com.anoto.api.PenStroke;
import com.anoto.api.PenStrokes;
import com.anoto.api.RenderException;
import com.anoto.api.util.PadFileUtility;
import com.anoto.api.util.PageAddress;
import com.anoto.ash.database.FormCopyPageArea;
import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.PadFile;
import com.anoto.ash.database.Role;
import com.anoto.ash.database.UserData;
import com.anoto.ash.exporter.document.ResultField;
import com.anoto.ash.portal.AshFormControl;
import com.anoto.ash.portal.AshUserControl;
import com.anoto.ash.portal.result.FormCopyResult;
import com.anoto.ash.portal.result.UserResult;
import com.anoto.ash.vo.ink.VOControl;
import com.anoto.ash.vo.ink.VOInterpretationResult;


@SuppressWarnings({"rawtypes","unchecked"})
public class AshPgcControl implements Serializable {
	private static final long serialVersionUID = 1L;
	private Pen currentPgc;
	private String pgcPenId;
	private String triggerPidgetAddress;
	private UserData penOwner;
	private short triggerPidgetId;
   
	private HashMap undefinedPages = null;
	private HashMap allPages = null;
	private ArrayList assignedPages = null;
	private HashMap formCopies = null;
	private Date uploadDate = null;
	private String pgcFileName = "";
	
	public AshPgcControl() {
		init();
	}
  
	private void init() {
		this.allPages = new HashMap();
		this.assignedPages = new ArrayList();
		this.undefinedPages = new HashMap();
		this.formCopies = new HashMap();
	}

	public HashMap processPgc(Pen pgc) throws com.anoto.ash.AshException {
		try {
			this.currentPgc = pgc;
			this.pgcPenId = this.currentPgc.getPenData().getPenSerial();
			AshLogger.log("PGC submitted by Pen ID: " + this.pgcPenId);
			validatePgc();
			setPenOwner();
			extractPages();
			
			gatherAndHandleForms();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.formCopies;
	}

	public HashMap processPgc(Pen pgc, Date uploadDate, String pgcFileName) throws com.anoto.ash.AshException {
		this.uploadDate = uploadDate;
		this.pgcFileName = pgcFileName;
		return processPgc(pgc);
	}

	public Pen mergePgcs(Pen oldPgc, Pen newPgc) throws MergeException, NoSuchPermissionException {
		Pen mergedPen = null;
		if (oldPgc == null) {
			return newPgc;
		}
		if (newPgc == null) {
			return oldPgc;
		}
		if (!(oldPgc.getPenData().getPenSerial().equals(newPgc.getPenData().getPenSerial()))) {
			mergedPen = CoreAdapter.mergeStrokes(oldPgc, newPgc);
		} else {
			mergedPen = oldPgc.merge(newPgc);
		}
		return mergedPen;
	}

	private void validatePgc() throws IllegalArgumentException, NumberFormatException, PageException, NoSuchPageException, FormatException, IllegalValueException, NoSuchPermissionException {
		this.triggerPidgetId = this.currentPgc.getMagicBoxId();
	  	AshLogger.logFine("PGC trigger pidget: " + this.triggerPidgetId);
		this.triggerPidgetAddress = this.currentPgc.getMagicBoxPage();
		AshLogger.logFine("PGC trigger pidget page Address: " + this.triggerPidgetAddress);
		if ((!(this.currentPgc.getPages().hasNext())) && (!(this.currentPgc.getUndefinedPages().hasNext()))) {
			AshLogger.logSevere("The submitted PGC file contains no pages.");
			throw new IllegalArgumentException("The submitted PGC file contains no pages.");
		}
	}

	private void extractPages() throws PageException {
		Iterator<Page> pageIterator = this.currentPgc.getPages();
		while (pageIterator.hasNext()) {
			Page currentPage = (Page)pageIterator.next();
			String currentPageAddress = currentPage.getPageAddress();
			this.allPages.put(currentPageAddress, currentPage);
		}
		AshLogger.logFine("Found " + this.allPages.size() + " defined pages in the pgc file.");
		Iterator<Page> undefinedPagesIterator = this.currentPgc.getUndefinedPages();
		int unMappedPagesCounter = 0;
		while (undefinedPagesIterator.hasNext()) {
			Page undefinedPage = (Page)undefinedPagesIterator.next();
			String undefinedPagePa = undefinedPage.getPageAddress();
			this.undefinedPages.put(undefinedPagePa, undefinedPage);
			++unMappedPagesCounter;
		}
		AshLogger.logFine("The PGC file contains " + unMappedPagesCounter + " pages not mapped in a loaded PAD file.");
	}

	private void gatherAndHandleForms()
  		throws 	IllegalValueException, 
	  			PenCreationException, 
	  			MergeException, 
	  			PageException, 
	  			FormatException, 
	  			NoSuchPageException, 
	  			MalformedURLException, 
	  			IOException, 
	  			SQLException,
	  			NoSuchPropertyException, 
	  			NoSuchPermissionException, 
	  			RenderException, 
	  			NoSuchFieldException, 
	  			IllegalAccessException {
		
		Iterator<Page> allPagesIterator = this.allPages.values().iterator();
		int formCounter = 0;

		while (allPagesIterator.hasNext()) {
			Page currentPage = (Page) allPagesIterator.next();
			if (!(this.assignedPages.contains(currentPage.getPageAddress())) && !(this.undefinedPages.containsValue(currentPage))) {
				System.gc();
				if (currentPage.hasPenStrokes()) {
					gatherAndHandleForm(currentPage);
				}
				++formCounter;
				AshLogger.logFine(formCounter + " forms handled in this request so far.");
			}
		}
	}

	private void gatherAndHandleForm(Page page)
	    throws 	IllegalValueException, 
	    		PenCreationException, 
	    		MergeException, 
	    		PageException, 
	    		FormatException, 
	    		MalformedURLException, 
	    		NoSuchPageException, 
	    		IOException, 
	    		IllegalStateException, 
	    		SQLException, 
	    		NoSuchPropertyException, 
	    		NoSuchPermissionException, 
	    		RenderException, 
	    		NoSuchFieldException, 
	    		IllegalAccessException {
	
		String[] formPages = AshCommons.getFormPages(page);
		String pageAddressRange = AshCommons.getFormPaRange(formPages);
		AshLogger.logFine("ASH is now handling  a form with page address range: " + pageAddressRange);

		String endPageAddress = formPages[(formPages.length - 1)];
    
	    // Recupera os dados do formulário.
	    FormCopyData fc = getFormCopy(endPageAddress);
	    if (fc == null) {
	    	return;
	    }
	    
		//#############################################################################################################//
		//#############################################################################################################//
		// Executa o merge dos arquivos PGC.
		String endPageAddressComplete = fc.getEndPageAddressComplete();
	    this.assignedPages.addAll(Arrays.asList(formPages));
	    Pen pgcData = splitAndMerge(formPages);
	    FormCopyData previouslySubmittedFormCopy = new FormCopyData();
	    if (formPreviouslySubmitted(endPageAddress)) {
	    	AshLogger.logFine("This form has been previously submitted");
	    	previouslySubmittedFormCopy = getFormCopy(endPageAddress);
	        byte[] bytes = previouslySubmittedFormCopy.getPgc();
	        if ((bytes != null) && (bytes.length > -5803342381377912832L)) {
	        	Pen previouslySubmittedPgc = getPenFromByteArray(bytes);
	        	AshLogger.logFine("About to merge strokes for this form.");
	        	pgcData = mergePgcs(previouslySubmittedPgc, pgcData);
	        }
	    }
	    
	    // Verifica se é um tipo de formulário válido.
	    FormTypeData formTypeData = getFormType(endPageAddress);
	    if (formTypeData == null)  {
	    	AshLogger.logSevere("A form copy with an unknown form type was submitted to the ASH, page address of form copy = " + endPageAddress);
	    	return;
	    }
	    if (!(formTypeData.isCorrect())) {
	    	FormCopyData formCopyData = createFormCopy(endPageAddress, pgcData, pageAddressRange, -1, -1, formTypeData, fc.getNumeroOcorrencia(), endPageAddressComplete);
	    	formCopyData.setIncorrectFormType(true);
	    	formCopyData.setFeedbackMessage("Bad Form type. Please contact System Administrator!");
	    	this.formCopies.put(Long.valueOf(new PageAddress(endPageAddress).longValue()), formCopyData);
	    	MailControl.getInstance().sendAdminMail("Incorrect form type " + formTypeData.getFormTypeName() + ". The form type has to be deleted from the Form Portal and then uploaded again before form copies can be handled", "Error mail from AFS", null);
	    	return;
	    }
	    
	    // Atualiza o número de formulários restantes.
	    if (formPreviouslySubmitted(endPageAddress)) {
	    	FormCopyData tempFormCopy = new FormCopyData();	
	    	tempFormCopy.setEndPageAddress(endPageAddress);
	    	List<FormCopyData> formCopyList = AshFormControl.getInstance().getFormCopy(tempFormCopy);
	    	if ((formCopyList != null) && (formCopyList.size() > 0)) {
	    		tempFormCopy = (FormCopyData) formCopyList.get(0);
		        if ((tempFormCopy.getPgc() == null) && (tempFormCopy.getTemporaryPgc() == null)) {
		        	formTypeData.setRemainingFormCopies(formTypeData.getRemainingFormCopies() - 1);
		        }
	    	}
	    } else {
	    	formTypeData.setRemainingFormCopies(formTypeData.getRemainingFormCopies() - 1);
	    }
	
	    int markedCompleted = 0;
	    int locked = 0;
	    FormCopyData formCopyData = createFormCopy(endPageAddress, pgcData, pageAddressRange, markedCompleted, locked, formTypeData, fc.getNumeroOcorrencia(), endPageAddressComplete);
	    AshLogger.logFine("Remaining form copies for " + formTypeData.getFormTypeName() + " after this submit: " + formTypeData.getRemainingFormCopies());
	   	
	    // Interpretação do form pelo VISION OBJECTS e READSOFT.
	   	List<ResultField> listICRFields = null; //interpretForm(formCopyData, formPages, false, "total", false, true);
	
	    FormCopyResult result = addOrUpdateFormCopy(formCopyData);
	    
	    this.formCopies.put(Long.valueOf(new PageAddress(endPageAddress).longValue()), formCopyData);
	    
		Iterator<Page> iter = pgcData.getPages();
		while (iter.hasNext()) {
			try {
				this.addPageAreasData((Page) iter.next(), result.getFormCopyData());
			} catch (PageAreaException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public PenStrokes getPenStrokesFromPageArea(Pen pgcData, String areaName) throws PageException {
		PenStrokes result = null;
		try {
			Iterator iterator = pgcData.getPages();
			while (iterator.hasNext()) {
				Page pageTmp = (Page) iterator.next();
				
				if (pageTmp.hasPageArea(areaName)) {
					PageArea pageArea = pageTmp.getPageArea(areaName);
					if (pageArea.hasPenStrokes()) {
						return pageArea.getPenStrokes();
					}
				} else {
					AshLogger.log("O campo '" + areaName + "' não foi mapeado.");
				}
			}
		} catch (NoSuchAreaException e) {
			AshLogger.logSevere("O campo '" + areaName + "' não foi encontrado.");
		} catch (PageAreaException e) {
			AshLogger.logSevere("Erro ao acessar o campo '" + areaName + "'.");
		}
		return result;
	}
	
		public FormCopyData mergeFormCopyPGCs(FormCopyData oldFormCopy, FormCopyData newFormCopy) throws MergeException, SQLException, NoSuchPermissionException  {
		byte[] oldBytes = oldFormCopy.getPgc();
		byte[] newBytes = newFormCopy.getPgc();

		if ((oldBytes != null) && (oldBytes.length > -5803341745722753024L) && (newBytes != null) && (newBytes.length > -5803341745722753024L)) {
			Pen oldPgc = getPenFromByteArray(oldBytes);
			Pen newPgc = getPenFromByteArray(newBytes);
			AshLogger.logFine("About to merge strokes for this form.");
			newPgc = mergePgcs(oldPgc, newPgc);
			newFormCopy.setPgc(newPgc);
		}
		return newFormCopy;
	}

	private Pen splitAndMerge(String[] addresses) throws IllegalValueException, PenCreationException, MergeException, PageException, NoSuchPermissionException {
		Iterator pageIterator = this.currentPgc.getPages();
		pageIterator.next();
		if (!(pageIterator.hasNext())) {
			return this.currentPgc;
		}
		Iterator pagePGCs = this.currentPgc.split(addresses);
		Pen allPagesPgc = null;
		if ((pagePGCs != null) && (pagePGCs.hasNext())) {
			allPagesPgc = (Pen)pagePGCs.next();
			while (pagePGCs.hasNext()) {
				Pen pgcToMerge = (Pen)pagePGCs.next();
				allPagesPgc = mergePgcs(allPagesPgc, pgcToMerge);
			}
		}
		return allPagesPgc;
	}

	private void setPenOwner() throws NoSuchPropertyException, NoSuchPermissionException, IllegalStateException {
		AshUserControl userControl = AshUserControl.getInstance();
		UserData user = userControl.getUserByPenId(this.pgcPenId);
		if (user == null) {
			AshLogger.log("This is the first submit for Pen with ID: " + this.pgcPenId);
			this.penOwner = createNewUser();
			UserResult result = addPenUser(this.penOwner);
			if (!(result.isUserOperationSuccessful())) {
				AshLogger.logSevere("SEVERE ERROR. The submitting pen could not be mapped to a user in the system, please contact system administrator.");
				throw new IllegalStateException("SEVERE ERROR. The submitting pen could not be mapped to a user in the system, please contact system administrator.");
			}
			MailControl mailControl = MailControl.getInstance();
			boolean adminMailSent = mailControl.sendAdminMail("A new user was added to the ASH \r\nuser name: " + result.getUserData().getUserName(), "New user credentials", null);

			if (!(adminMailSent)) {
				AshLogger.logSevere("Error when sending mail to admin about new user creation");
			}
		} else {
			this.penOwner = user;
		}
		AshLogger.logFine("Submitting user: " + this.penOwner.toString());
	}

	private UserData createNewUser() throws NoSuchPermissionException {
		AshLogger.logFine("About to add a new user (since first submit from this pen).");
		UserData user = new UserData();
		PenOwnerData ownerData = this.currentPgc.getPenOwnerData();
		String email = "";
		String penOwnerFullName = "";
		try {
			email = ownerData.getEmail();
		} catch (NoSuchPropertyException nspe) { }

		try {
			penOwnerFullName = ownerData.getFullName();
		} catch (NoSuchPropertyException nspe) { }
		penOwnerFullName = replaceIllegalCharacters(penOwnerFullName);
		AshLogger.logFine("Email will be set to: " + email);
	    String firstName = "";
	    String lastName = "";
	    String userName = "";
	    if ((penOwnerFullName != null) && (penOwnerFullName.length() > 0)) {
	    	firstName = penOwnerFullName;
	    	lastName = "";
	    	if (penOwnerFullName.indexOf(" ") != -1)
	    		userName = penOwnerFullName.replace(" ", "").toLowerCase();
	    	else
	    		userName = penOwnerFullName;
	    } else {
	    	firstName = "";
	    	lastName = "";
	    	userName = this.pgcPenId;
	    }
	    if (userName.length() > 15) {
	      userName = userName.substring(0, 14);
	    }
	    AshLogger.logFine("First name will be set to " + firstName);
	    AshLogger.logFine("Last name will be set to " + lastName);
	    AshLogger.logFine("Username will be set to " + userName);
	    String password = Randomizer.generateNewPassword();
	    user.setUserName(userName);
	    user.setFirstName(firstName);
	    user.setLastName(lastName);
	    user.setEmail(email);
	    user.setPassword(password);
	    user.setNbrOfFailedLogins(0);
	    user.setPenId(this.pgcPenId);
	    user.setLocked(0);
	    Role role = AshUserControl.getInstance().getRole("User");
	    user.setRole(role);
	    return user;
	}

	private String replaceIllegalCharacters(String string) {
		string = replaceSwedishLetters(string);
		for (int i = 0; i < string.length(); ++i) {
			String letter = string.substring(i, i + 1);
			if ((!(letter.equals(" "))) && (!(valid(letter))))
				string = string.replaceAll(letter, " ");
		}
		return string;
	}

	private boolean valid(String pString) {
		Pattern vPattern = Pattern.compile("[a-zA-Z0-9-]*");
		Matcher vMatcher = vPattern.matcher(pString);
		boolean vTest = vMatcher.matches();
		return vTest;
	}

	public void handleMandatoryFields(FormCopyData formCopyData, Pen pgcData, String[] formPages) throws IllegalValueException {
		MandatoryFieldsHandler fieldHandler = new MandatoryFieldsHandler(pgcData, formPages);
		ArrayList missingMandatoryFields = fieldHandler.getMissedMandatoryFields();
		Sorter.sortFieldDataByPage(missingMandatoryFields);
	    if (missingMandatoryFields.size() > 0) {
	    	AshLogger.logFine("There are  " + missingMandatoryFields.size() + " neglected mandatory fields on this form.");
	    }
	    ArrayList missingMandatoryOrFieldGroups = fieldHandler.getMissedMandatoryOrFieldGroups();
	    if (missingMandatoryOrFieldGroups.size() > 0) {
	    	AshLogger.logFine("There are  " + missingMandatoryOrFieldGroups.size() + " neglected field-groups where at least one field must be filled.");
	    }
	    int mandatoryFieldsMissing = ((missingMandatoryFields.isEmpty()) && (missingMandatoryOrFieldGroups.isEmpty())) ? 0 : 1;
	    formCopyData.setMandatoryFieldsMissing(mandatoryFieldsMissing);
	    formCopyData.setMissingMandatoryFields(missingMandatoryFields);
	    formCopyData.setMissingMandatoryOrFieldGroups(missingMandatoryOrFieldGroups);
	}

	public boolean checkMandatoryFields(Pen pgcData, String[] formPages) throws IllegalValueException {
	    MandatoryFieldsHandler fieldHandler = new MandatoryFieldsHandler(pgcData, formPages);
	    ArrayList missingMandatoryFields = fieldHandler.getMissedMandatoryFields();
	    Sorter.sortFieldDataByPage(missingMandatoryFields);
	    if (missingMandatoryFields.size() > 0) {
	    	AshLogger.logFine("There are  " + missingMandatoryFields.size() + " neglected mandatory fields on this form.");
	    }
	    ArrayList missingMandatoryOrFieldGroups = fieldHandler.getMissedMandatoryOrFieldGroups();
	    if (missingMandatoryOrFieldGroups.size() > 0) {
	    	AshLogger.logFine("There are  " + missingMandatoryOrFieldGroups.size() + " neglected field-groups where at least one field must be filled.");
	    }
	    boolean mandatoryFieldsMissing = (!(missingMandatoryFields.isEmpty())) || (!(missingMandatoryOrFieldGroups.isEmpty()));
	    return mandatoryFieldsMissing;
	}
	
	private FormCopyData getFormCopy(String endPageAddress) {
		FormCopyData formCopyData = null;
	    FormCopyData formCopy = new FormCopyData();
	    formCopy.setEndPageAddress(endPageAddress);
	    AshFormControl formControl = AshFormControl.getInstance();
	    List matchingFormCopies = formControl.searchFormCopies(formCopy, "");
	
	    Iterator formCopyIterator = matchingFormCopies.iterator();
	    if (formCopyIterator.hasNext()) {
	      formCopyData = (FormCopyData)formCopyIterator.next();
	    }
	
	    return formCopyData;
	}
  
  	private FormCopyResult addOrUpdateFormCopy(FormCopyData formCopyData) {
  		FormCopyResult result = new FormCopyResult();
  		AshFormControl formControl = AshFormControl.getInstance();
  		FormTypeData formTypeData = formCopyData.getFormType();
  		boolean formPreviouslySubmitted = formPreviouslySubmitted(formCopyData.getEndPageAddress());

  		if (formPreviouslySubmitted) {
  			AshLogger.logFine("About to update the previously submitted form.");
  			FormCopyData storedFormCopy = getFormCopy(formCopyData.getEndPageAddress());
  			formCopyData.setFormCopyId(storedFormCopy.getFormCopyId());
  			// Verifica se o formulário possui número de ocorrência.
  			// Se possuir, mantém o número de ocorrencia cadastrado.
  			if (storedFormCopy.getNumeroOcorrencia() != null && !storedFormCopy.getNumeroOcorrencia().trim().equals("")) {
  				formCopyData.setNumeroOcorrencia(storedFormCopy.getNumeroOcorrencia());
  			}
  			result = formControl.updateFormCopy(formCopyData);
  		} else {
  			AshLogger.logFine("About to add a new form copy.");
  			result = formControl.addFormCopy(formCopyData);
  			if (result.isFormCopyOperationSuccessful())
  				sendNotificationLevelMailIfNeeded(formTypeData);
  		}

  		if (!(result.isFormCopyOperationSuccessful())) {
  			AshLogger.logSevere(result.getFormCopyOperationMessage());
  			throw new IllegalStateException(result.getFormCopyOperationMessage());
  		}
  		
  		return result;
  	}
  
  	private void sendNotificationLevelMailIfNeeded(FormTypeData formTypeData) {
  		int updatedRemainingFormCopies = formTypeData.getRemainingFormCopies();
  		int notificationLevel = formTypeData.getNotificationLevel();

  		if ((updatedRemainingFormCopies == 0) || (updatedRemainingFormCopies == notificationLevel)) {
  			MailControl mailControl = MailControl.getInstance();
  			String message = "You now have " + updatedRemainingFormCopies + " unique form copies of formtype " + formTypeData.getFormTypeName() + " remaining on your pattern license.";
  			mailControl.sendNotificationLevelMail(message);
  		}
  	}

  	private String replaceSwedishLetters(String string) {
  		string = string.replaceAll("Ã¥", "a");
  		string = string.replaceAll("Ã¤", "a");
  		string = string.replaceAll("Ã¶", "o");
  		string = string.replaceAll("Ã…", "A");
  		string = string.replaceAll("Ã„", "A");
  		string = string.replaceAll("Ã–", "O");
  		return string;
  	}

  	public VOInterpretationResult getFieldsForVerification(FormCopyData formCopyData, String[] allPageAddresses, boolean updatedFieldsOnly, String additionalInfo, boolean createImages) throws IllegalStateException, MalformedURLException, PageException, FormatException, IllegalValueException, NoSuchPageException, IOException {
  		VOInterpretationResult result = new VOInterpretationResult();
  		try {
  			Pen pgcData = getPenFromByteArray(formCopyData.getPgc());
  			String pageAddressRange = formCopyData.getPageAddressRange();
  			VOControl voControl = new VOControl();
  			result = voControl.submitForm(formCopyData, pageAddressRange, pgcData, updatedFieldsOnly);
  			createImages = !(result.isVerificationNeeded());
  			if ((formCopyData.getFormType().getMultiplePenMode() == 1) || (AshProperties.getProperty("multiplePenMode").equalsIgnoreCase("true"))) {
  				result.setEndUserFieldsForVerification(new ArrayList());
  			}
  		} catch (Exception e) {
  			formCopyData.setVerificationFailed(true);
  			AshLogger.logSevere("Interpretation failed for form " + formCopyData.getPageAddressRange() + " of formtype " + formCopyData.getFormType().getFormTypeName());
  			AshLogger.logSevere("Exception in interpretation module: " + e.getMessage());
  			MailControl.getInstance().sendAdminMail("Got exception when handling form " + formCopyData.getPageAddressRange() + " of formtype " + formCopyData.getFormType().getFormTypeName() + ". Error message: " + e.getMessage(), "Exception in Interpretation module!", null);
  			return null;
  		}

  		return result;
  	}

  	public synchronized List interpretForm(FormCopyData formCopyData, String[] formPages, boolean updatedFieldsOnly, String additionalImageFileNameInfo, boolean createImages, boolean interpretForm) throws MalformedURLException, PageException, FormatException, IllegalValueException, NoSuchPageException, IOException, MergeException, NoSuchPermissionException {
  		String verificationModule = AshProperties.verificationModule;
  		addOrUpdateFormCopy(formCopyData);

  		if (verificationModule.equalsIgnoreCase("VISION OBJECTS")) {
  			if (interpretForm) {
  				formCopyData.setVerificationInProgress(1);
  				addOrUpdateFormCopy(formCopyData);
  				VOInterpretationResult result = getFieldsForVerification(formCopyData, formPages, updatedFieldsOnly, additionalImageFileNameInfo, createImages);
  				if (result != null) {
  					AshLogger.logFine("There are  " + result.getAllFieldsForVerification().size() + " fields that needs verification on this form.");
  					int verificationNeeded = (result.isVerificationNeeded()) ? 1 : 0;
  					formCopyData.setVerificationNeeded(verificationNeeded);
  					formCopyData.setInterpretationResult(result);
				} else {
					String resendFrogs = AshProperties.getProperty("resend_frogs");
					if ((resendFrogs == null) || (!(resendFrogs.equalsIgnoreCase("N")))) {
						AshLogger.log("Interpretation failed!! Trying again!");
						boolean success = retryInterpretForm(formCopyData, formPages, updatedFieldsOnly, additionalImageFileNameInfo, createImages, interpretForm, 0);
						if (!(success)) {
							formCopyData.setVerificationFailed(true);
							interpretForm = false;
							createImages = false;
						}
					} else {
						formCopyData.setVerificationFailed(true);
						interpretForm = false;
						createImages = false;
					}
		        }
		        formCopyData.setVerificationInProgress(0);
		        addOrUpdateFormCopy(formCopyData);
  			} else {
  				VOControl voControl = new VOControl();
		        try {
		        	ArrayList fieldsForVerification = voControl.getFieldsForVerification(formCopyData, formCopyData.getPageAddressRange(), formCopyData.getFormType().getFormTypeName());
		        	if (fieldsForVerification.size() > 0)
		        		createImages = false;
		        	else
		        		createImages = true;
		        } catch (Exception e) {
		        	createImages = false;
		        	interpretForm = false;
		        	AshLogger.logSevere("Exception in interpretation module: " + e.getMessage());
		        	MailControl.getInstance().sendAdminMail("Got exception when handling form " + formCopyData.getPageAddressRange() + " of formtype " + formCopyData.getFormType().getFormTypeName() + ". Error message: " + e.getMessage(), "Exception in Interpretation module!", null);
		        }
  			}
  		} else if (verificationModule.equalsIgnoreCase("READSOFT")) {
  			formCopyData.setVerificationNeeded(0);
  		} else {
  			formCopyData.setVerificationNeeded(0);	
  		}
  		return AshFormControl.getInstance().exportFormCopy(formCopyData, additionalImageFileNameInfo, createImages, interpretForm);
  	}
  
  	public synchronized boolean retryInterpretForm(FormCopyData formCopyData, String[] formPages, boolean updatedFieldsOnly, String additionalImageFileNameInfo, boolean createImages, boolean interpretForm, int retryCounter)
  		throws MalformedURLException, PageException, FormatException, IllegalValueException, NoSuchPageException, IOException, MergeException, NoSuchPermissionException {
  		if (retryCounter < 2) {
  			++retryCounter;
  			VOInterpretationResult result = getFieldsForVerification(formCopyData, formPages, updatedFieldsOnly, additionalImageFileNameInfo, createImages);
  			if (result != null) {
  				AshLogger.logFine("There are  " + result.getAllFieldsForVerification().size() + " fields that needs verification on this form.");
  				int verificationNeeded = (result.isVerificationNeeded()) ? 1 : 0;
  				formCopyData.setVerificationNeeded(verificationNeeded);	
  				formCopyData.setInterpretationResult(result);
  				return true;
  			}
  			AshLogger.log("Interpretation failed!! Trying again...");
  			return retryInterpretForm(formCopyData, formPages, updatedFieldsOnly, additionalImageFileNameInfo, createImages, interpretForm, retryCounter);
  		}
  		return false;
  	}

  	private boolean formPreviouslySubmitted(String endPageAddress) {
  		return (getFormCopy(endPageAddress) != null);
  	}

  	private Pen getPenFromByteArray(byte[] bytes) throws IllegalStateException {
  		Pen pen = null;
  		try {
  			if (bytes != null) {
  				InputStream inputStream = new ByteArrayInputStream(bytes);
  				pen = PenHome.read(inputStream, "Anoto_Forms_Solution_ASH");
  			}
  		} catch (Exception e) {
  			AshLogger.logSevere("Severe Error. " + e.getMessage());
  		}
  		return pen;
  	}

  	private FormCopyData createFormCopy(String endPageAddress, Pen pgcData, String pageAddressRange, int markedCompleted, int locked, FormTypeData formTypeData, String numeroOcorrencia, String endPageAddressComplete) {
  		FormCopyData formCopyData = new FormCopyData();
	    formCopyData.setPageAddressRange(pageAddressRange);
	    formCopyData.setPgc(pgcData);
	    formCopyData.setMarkedCompleted(markedCompleted);
	    formCopyData.setLocked(locked);
	    formCopyData.setVerificationNeeded(1);
	
	    formCopyData.setEndPageAddress(endPageAddress);
	    formCopyData.setOwner(this.penOwner);
	    formCopyData.setFormType(formTypeData);
	    formCopyData.setLatestSubmit(AshCommons.currentTimestamp());
	    formCopyData.setVerificationInProgress(0);
    
	    // Número de atendimento.
	    formCopyData.setNumeroOcorrencia(numeroOcorrencia);
	    
	    formCopyData.setEndPageAddressComplete(endPageAddressComplete);
	    
	    formCopyData.setVerificationNeeded(0);
	    
	    return formCopyData;
  	}

  	private UserResult addPenUser(UserData user) {
  		AshUserControl userControl = AshUserControl.getInstance();
  		UserResult result = userControl.addUser(user);
  		int numberOfFailedAttempts = 0;
  		String newUserName = user.getUserName();
  		while ((!(result.isUserOperationSuccessful())) && (numberOfFailedAttempts <= 4)) {
  			newUserName = user.getUserName() + Randomizer.getRandomPositiveInteger(9);
  			user.setUserName(newUserName);
  			result = userControl.addUser(user);
  			++numberOfFailedAttempts;
  		}
  		if (!(result.isUserOperationSuccessful())) {
  			newUserName = Randomizer.generateRandomString(5, 10, AshProperties.validPasswordCharacters);
  			user.setUserName(newUserName);
  			result = userControl.addUser(user);
  		}
  		return result;
  	}

  	private FormTypeData getFormType(String pageAddress) {
  		String padLicenseAddress = PadFileUtility.getPadLicenseAddress(pageAddress, "Anoto_Forms_Solution_ASH");
  		AshLogger.logFine("This form belongs to a PAD with license: " + padLicenseAddress);
  		PadFile padFile = new PadFile();
  		padFile.setPadLicenseAddress(padLicenseAddress);
  		AshFormControl formControl = AshFormControl.getInstance();
  		padFile = (PadFile)formControl.getPadFile(padFile).get(0);
  		FormTypeData formTypeData = padFile.getFormType();
  		if (formTypeData != null)
  			AshLogger.logFine("This form belongs to formtype " + formTypeData.getFormTypeName());
  		return formTypeData;
  	}

  	public boolean isFormLockedByUserArea(Pen pgc) throws IllegalValueException, PageException {
  		if (pgc != null) {
  			Iterator pageIterator = pgc.getPages();
  			while (pageIterator.hasNext()) {
  				Page currentPage = (Page)pageIterator.next();
  				Iterator pageAreaIterator = currentPage.getPageAreas(256);
  				while (pageAreaIterator.hasNext()) {
  					PageArea userArea = (PageArea)pageAreaIterator.next();
  					String lockTagValue = userArea.getAttribute("lock_form");
  					if ((lockTagValue != null) && (userArea.hasPenStrokes())) {
  						return true;
  					}
  				}
  			}
  		}
  		return false;
  	}
  
  	/**
  	 * Get timestamp data
  	 * @throws PageException 
  	 */
  	private Date getTimestampData(PenStrokes strokes) {
  		Iterator iter = strokes.getIterator();
  		long timestamp = 0;
  		while (iter.hasNext()) {
  			PenStroke stroke = (PenStroke) iter.next();
  			if (timestamp == 0) {
  				timestamp = stroke.getStartTime();
  			} else {
  				if (stroke.getStartTime() < timestamp) {
  					timestamp = stroke.getStartTime();
  				}
  			}
  		}
  		return AshCommons.dateWithTime(timestamp);
  	}
  
	/**
	  * Get last timestamp data
	  * @throws PageException 
	  */
  	private Date getLastTimestampData(PenStrokes strokes) {
  		Iterator iter = strokes.getIterator();
	    long timestamp = 0;
	    while (iter.hasNext()) {
	    	PenStroke stroke = (PenStroke) iter.next();
	    	if (timestamp == 0) {
	    		timestamp = stroke.getStartTime();
	    	} else {
	    		if (stroke.getStartTime() > timestamp) {
	    			timestamp = stroke.getStartTime();
	    		}
	    	}
	    }
	    return AshCommons.dateWithTime(timestamp);
  	}
  
	/**
	  * Grava os pageAreas do formulário.
	  * @throws PageException 
	 * @throws PageAreaException 
	  */
	private void addPageAreasData(Page page, FormCopyData formCopy) throws PageException, PageAreaException {
  		FormCopyPageArea formCopiesPageAreas = new FormCopyPageArea();
  		formCopiesPageAreas.setFormCopyId(formCopy.getFormCopyId());
  		List listPageAreasInDB = AshFormControl.getInstance().searchFormCopiesPageAreas(formCopiesPageAreas);
  		
  		List listPageAreasInForm = new LinkedList<FormCopyPageArea>();
  		
		for (Iterator iterator = page.getPageAreas(); iterator.hasNext();) {
			PageArea pageArea = (PageArea) iterator.next();
			String name = pageArea.getName();
			String dynDataTagValue = pageArea.getAttribute("dynamic_data");
			
			if (dynDataTagValue == null && !name.startsWith("Magic") && !name.startsWith("drawingarea") && !name.startsWith("nopattern") && !name.startsWith("Send")) {
				FormCopyPageArea formCopiesPageAreasNew = new FormCopyPageArea();
				formCopiesPageAreasNew.setName(name);
				formCopiesPageAreasNew.setNumberPenStrokes(pageArea.getNumberOfPenStrokes());
				formCopiesPageAreasNew.setDefaultRenderingHeight(pageArea.getDefaultRenderingHeight());
				formCopiesPageAreasNew.setDefaultRenderingWidth(pageArea.getDefaultRenderingWidth());
				formCopiesPageAreasNew.setFormCopyId(formCopy.getFormCopyId());
				
				PenStrokes penStrokes = pageArea.getPenStrokes();
				if (penStrokes != null) {
					formCopiesPageAreasNew.setTimestampStart(getTimestampData(penStrokes));
					formCopiesPageAreasNew.setTimestampEnd(getLastTimestampData(penStrokes));
				}
				
				if (listPageAreasInDB != null && listPageAreasInDB.size() > 0) {
					for (Iterator iterator2 = listPageAreasInDB.iterator(); iterator2.hasNext();) {
						FormCopyPageArea formCopiesPageAreasOld = (FormCopyPageArea) iterator2.next();
						if (formCopiesPageAreasNew.getName().equals(formCopiesPageAreasOld.getName())) {
							formCopiesPageAreasNew.setId(formCopiesPageAreasOld.getId());
						}
					}
				}
				
				listPageAreasInForm.add(formCopiesPageAreasNew);
			}
		}
		
	  	if (listPageAreasInForm.size() > 0) {
	  		AshFormControl.getInstance().addFormCopiesPageAreas(listPageAreasInForm);
	  	}
  	}
	

	
  	
}