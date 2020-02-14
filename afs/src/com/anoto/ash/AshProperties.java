package com.anoto.ash;

import com.anoto.api.renderer.RenderLogger;
import com.anoto.ash.database.SettingsDBHandler;
import com.anoto.ash.database.SettingsData;
import com.anoto.ash.portal.AshSettingsControl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
public class AshProperties extends Exception
{
  public static String logLevel = "FINE";
  public static String logFolder = "";
  public static String adminErrorMailSubject = "";
  public static int failedLoginsLevel = 4;
  public static String verificationModule = "";
  public static String VOFormsProcessorURL = "";
  public static String VOFormModelerURL = "";
  public static String bundledPadLocation = "";
  //public static String EXPORT_FOLDER = "";
  public static String EXPORT_FOLDER = "\\anoto";
  public static String EXPORT_FORMAT_NONE = "NONE";
  public static String EXPORT_FORMAT_XML = "XML";
  public static String EXPORT_FORMAT_CSV = "CSV";
  public static String IMAGE_FORMAT_NONE = "NONE";
  public static String IMAGE_FORMAT_PNG = "PNG";
  public static String IMAGE_FORMAT_JPG = "JPG";
  public static String IMAGE_FORMAT_TIF = "TIF";
  public static String IMAGE_FORMAT_PDF = "PDF";
  public static String EXPORT_FORM_TYPES_TO_XML_FUNCTION = "/golge";
  public static String ASH_VERSION = "";
  public static boolean testMode = false;
  public static String rootFolderLocation;
  public static String ashRootFolderPath = "";
  public static String ashRootFolderPathString = "ASHRootFolderPath";
  //public static final String ashUserPropertiesFile = "\\Program Files\\Anoto\\Anoto Forms Solution\\ash_dont_change.sys";
  public static final String ashUserPropertiesFile = "\\apps\\rep\\anoto\\afs\\ash_dont_change.sys";
  public static final String ashTempFolder = "rendered\\";
  //public static final String rsTempDirectory = "\\Program Files\\Anoto\\Anoto Forms Solution\\temp";
  public static final String rsTempDirectory = "\\apps\\rep\\anoto\\afs\\temp";
  public static final String AFS_WEB_APPLICATION_ROOT = "/";
  public static final int[] ALLOWED_PATTERN_SEGMENTS = { 169, 175, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 2208}; // IMPORTANTE! HABILITA OS ENDEREÇOS DO PATTERN!!!
  public static final String appName = "Anoto_Forms_Solution_ASH";
  //public static final String rootFolderName = "Anoto_Forms_Solution_ASH_Root\\";
  public static final String rootFolderName = "\\apps\\rep\\anoto\\afs\\";
  public static final String userInfoMailSubject = "New user credentials";
  public static final String notificationLevelMailSubject = "Notifiation level reached.";
  public static final String MANDATORY_USER_AREA_TAG = "mandatory";
  public static final String DYNAMIC_DATA_USER_AREA_TAG = "dynamic_data";
  public static final String LOCK_USER_AREA_TAG = "lock_form";
  public static final String TRIGGER_USER_AREA_TAG = "trigger";
  public static final String TRIGGER_USER_AREA_AND_NOTATION = "#AND#";
  public static final String TRIGGER_USER_AREA_OR_NOTATION = "#OR#";
  public static final String VO_HWR_FIELD_IDENTIFIER = "HWR";
  public static final String VO_MKR_FIELD_IDENTIFIER = "MKR";
  public static final String VO_UNKNOWN_FIELD_IDENTIFIER = "UNKNOWN";
  public static final String VO_FORM_MODELER_END_USER_TAG = "end-user";
  public static final String VO_FORM_MODELER_VERIFICATION_TRESHOLD_TAG = "threshold";
  public static final String VO_FORMS_PROCESSOR_INTERPRETATION_STATUS_FAILED = "FAILED";
  public static final String VO_FORMS_PROCESSOR_INTERPRETATION_STATUS_UNINTERPRETED = "UNINTERPRETED";
  public static final String VO_FORMS_PROCESSOR_INTERPRETATION_STATUS_UNSAFE = "UNSAFE";
  public static final String VO_FORMS_PROCESSOR_INTERPRETATION_STATUS_SAFE = "SAFE";
  public static final String VO_FORMS_PROCESSOR_VALIDATION_STATUS_VALID = "VALID";
  public static final String VO_FORMS_PROCESSOR_VALIDATION_STATUS_INVALID = "INVALID";
  public static final String VO_FORMS_PROCESSOR_VALIDATION_STATUS_NONE = "VALID";
  public static final String adminErrorMessage = "Setup wrong. Please contact system administrator.";
  public static final String INTERPRETATION_MODULE_VO = "VISION OBJECTS";
  public static final String INTERPRETATION_MODULE_RS = "READSOFT";
  public static final String PAD_LICENSE_ADDRESSES_DELIMITER = "|";
  public static final int minAllowedResolution = 1;
  public static final int defaultResolution = 200;
  public static final int maxAllowedResolution = 1000;
  public static final int defaultNotificationLevelPercent = 10;
  public static String[] validPasswordCharacters = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
  public static String[] validUserNameCharacters = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
  public static final int WRONG_SAVE_OPERATION_RETURN_VALUE = -1;
  public static final String WRONG_SAVE_OPERATION_RETURN_VALUE_MESSAGE = "Operation failed.";
  public static final int EMPTY_PASSWORD = -2;
  public static final String EMPTY_PASSWORD_MESSAGE = "Empty password.";
  public static final int TRYING_TO_USE_EXISTING_USERNAME = -3;
  public static final String TRYING_TO_USE_EXISTING_USERNAME_MESSAGE = "User name not unique.";
  public static final int NO_SUCH_USER = -4;
  public static final String NO_SUCH_USER_MESSAGE = "No Such User..";
  public static final int WRONG_USERNAME_OR_PASSWORD = -5;
  public static final String WRONG_USERNAME_OR_PASSWORD_MESSAGE = "Wrong user name or password.";
  public static final int FAILED_LOGINS_LEVEL_EXCEEDED = -6;
  public static final String FAILED_LOGINS_LEVEL_EXCEEDED_MESSAGE = "Maximum number of failed logins exceeded for this user. Account has been locked, please contact system administrator.";
  public static final int LOCKED_USER = -13;
  public static final String LOCKED_USER_MESSAGE = "This account has been locked, please contact system administrator.";
  public static final int TRYING_TO_USE_EXISTING_PEN_ID = -7;
  public static final String TRYING_TO_USE_EXISTING_PEN_ID_MESSAGE = "The submitted pen serial already belongs to another user.";
  public static final int TRYING_TO_CHANGE_PENID_WITH_FORMS = -8;
  public static final String TRYING_TO_CHANGE_PENID_WITH_FORMS_MESSAGE = "The pen serial may not be changed since form data has been submitted by the pen.";
  public static final String COULD_NOT_MAP_SUBMITTING_PEN_TO_USER_MESSAGE = "SEVERE ERROR. The submitting pen could not be mapped to a user in the system, please contact system administrator.";
  public static final String UPDATE_OK_BUT_NO_USER_MAIL_SET_MESSAGE = "The update went well but no mail with the new login info could be sent to the user. ";
  public static int ONE_ADMIN_MUST_EXIST = -9;
  public static String ONE_ADMIN_MUST_EXIST_MESSAGE = "Operation not permitted. There must always exist at least one admin in the system";
  public static final int TRYING_TO_USE_EXISTING_FORMTYPENAME = -10;
  public static final String TRYING_TO_USE_EXISTING_FORMTYPENAME_MESSAGE = "Form type name not unique.";
  public static final int NO_SUCH_FORM_TYPE = -11;
  public static final String NO_SUCH_FORM_TYPE_MESSAGE = "No form type exists with the proposed form type name.";
  public static final int padFileAlreadySubmitted = -12;
  public static final String padFileAlreadySubmittedMessage = "The PAD file has already been submitted to the ASH.";
  public static final int NO_SUCH_FORM_COPY = -14;
  public static final String NO_SUCH_FORM_COPY_MESSAGE = "No formcopy exists with the proposed formcopy id.";
  public static final int NO_SUCH_PAD_FILE = -15;
  public static final String NO_SUCH_PAD_FILE_MESSAGE = "No pad file exists with the proposed id";
  public static final String PEN_ID_ASC = "penId_asc";
  public static final String PEN_ID_DESC = "penId_desc";
  public static final String FORM_ASC = "form_asc";
  public static final String FORM_DESC = "form_desc";
  public static final String DATE_ASC = "date_asc";
  public static final String DATE_DESC = "date_desc";
  public static final String COMPLETED_ASC = "completed_asc";
  public static final String COMPLETED_DESC = "completed_desc";
  public static final String FIRST_NAME_ASC = "firstName_asc";
  public static final String FIRST_NAME_DESC = "firstName_desc";
  public static final String LAST_NAME_ASC = "lastName_asc";
  public static final String LAST_NAME_DESC = "lastName_desc";
  public static final String USER_NAME_ASC = "userName_asc";
  public static final String USER_NAME_DESC = "userName_desc";
  public static final String LOCKED_ASC = "locked_asc";
  public static final String LOCKED_DESC = "locked_desc";
  //static String defaultAdminMailSender = "ash_no_reply@anoto.com";
  static String defaultAdminMailSender = "admin@icconsulting.com.br";
  //static String defaultFont = "Times-Roman";
  static String defaultFont = "Arial";
  public static String NO_INTERACTION = "0";
  public static String VERIFICATION = "1";
  public static int USER_STATUS_ALIVE_AND_WELL = 1;
  public static int USER_STATUS_REMOVED = -1;
  static String[] supportedFonts = { "Arial", "Arial,Bold", "Arial,Italic", "Arial,BoldItalic", "AvantGarde-Book", "AvantGarde-BookOblique", "AvantGarde-Demi", "AvantGarde-DemiOblique", "Bookman-Demi", "Bookman-DemiItalic", "Bookman-Light", "Bookman-LightItalic", "Courier", "Courier-Oblique", "Courier-Bold", "Courier-BoldOblique", "AGaramond-Regular", "AGaramond-Italic", "AGaramond-Bold", "AGaramond-BoldItalic", "Helvetica", "Helvetica-Oblique", "Helvetica-Bold", "Helvetica-BoldOblique", "Helvetica-Narrow", "Helvetica-Narrow-Oblique", "Helvetica-Narrow-Bold", "Helvetica-Narrow-BoldOblique", "Palatino-Roman", "Palatino-Italic", "Palatino-Bold", "Palatino-BoldItalic", "Times-Roman", "TimesNewRoman", "TimesNewRoman,Bold", "TimesNewRoman,Italic", "TimesNewRoman,BoldItalic" };

  public static final String NUMERO_OCORRENCIA = "ident_campo_numero_ocorrencia";
  public static final String IDENT_CAMPO_ASSINATURA = "ident_campo_assinatura";
  public static String PGC_FOLDER = "";
  public static String PGC_FOLDER_PROCESSED = "";
  public static final String PGC_FILE_EXT = ".pgc";
  public static final String INTERVALO_MONITORAMENTO_PGC = "intervalo_monitoramento_pgc";
  public static String PGC_FOLDER_ERROR = "";
  public static String PGC_FOLDER_RECEIVED = "";
  
  // AIT
  // Inicio
  public static final String IDENT_CAMPO_ABORDAGEM = "ident_campo_abordagem";
  public static final String IDENT_CAMPO_CANCELAR = "ident_campo_cancelar";
  
  public static String IDENT_CAMPO_PLACA_DIGITO_01 = "ident_campo_placa_digito_01";
  public static String IDENT_CAMPO_PLACA_DIGITO_02 = "ident_campo_placa_digito_02";
  public static String IDENT_CAMPO_PLACA_DIGITO_03 = "ident_campo_placa_digito_03";
  public static String IDENT_CAMPO_PLACA_DIGITO_04 = "ident_campo_placa_digito_04";
  public static String IDENT_CAMPO_PLACA_DIGITO_05 = "ident_campo_placa_digito_05";
  public static String IDENT_CAMPO_PLACA_DIGITO_06 = "ident_campo_placa_digito_06";
  public static String IDENT_CAMPO_PLACA_DIGITO_07 = "ident_campo_placa_digito_07";
  
  public static final String IDENT_CAMPO_MARCA_VEICULO = "ident_campo_marca_veiculo";
  public static final String IDENT_CAMPO_ESPECIE_VEICULO = "ident_campo_especie_veiculo";
  public static final String IDENT_CAMPO_SEXO = "ident_campo_sexo";
  public static final String IDENT_CAMPO_PROPRIETARIO = "ident_campo_proprietario";
    
  //public static String IDENT_CAMPO_INFRACAO_DIGITO_01 = "ident_campo_infracao_digito_01";
  //public static String IDENT_CAMPO_INFRACAO_DIGITO_02 = "ident_campo_infracao_digito_02";
  //public static String IDENT_CAMPO_INFRACAO_DIGITO_03 = "ident_campo_infracao_digito_03";
  //public static String IDENT_CAMPO_INFRACAO_DIGITO_04 = "ident_campo_infracao_digito_04";
  //public static String IDENT_CAMPO_DESDOBRAMENTO = "ident_campo_desdobramento";
  
  public static final String IDENT_CAMPO_SITUACAO_VEICULO = "ident_campo_situacao_veiculo";
  public static final String IDENT_CAMPO_DOCUMENTO_RECOLHIDO = "ident_campo_documento_recolhido";
  public static final String IDENT_CAMPO_CODIGO_INFRACAO = "ident_campo_codigo_infracao";
  public static final String IDENT_CAMPO_UNIDADE_MEDIDA = "ident_campo_unidade_medida";
  public static final String IDENT_CAMPO_PLACA = "ident_campo_placa";
  public static final String IDENT_CAMPO_LOGRADOURO = "ident_campo_logradouro";
  public static final String IDENT_CAMPO_NUMERO = "ident_campo_numero";
  public static final String IDENT_CAMPO_DATA = "ident_campo_data";
  public static final String IDENT_CAMPO_HORA = "ident_campo_hora";
  public static final String IDENT_CAMPO_MUNICIPIO = "ident_campo_municipio";
  public static final String IDENT_CAMPO_INFRACAO_DESDOBRAMENTO = "ident_campo_infracao_desdobramento";
  public static final String IDENT_CAMPO_TIPIFICACAO = "ident_campo_tipificacao";
  public static final String IDENT_CAMPO_MATRICULA = "ident_campo_matricula";
    
  public static final String IDENT_CAMPO_PREENCHIMENTO_OBRIGATORIO = "ident_campo_preenchimento_obrigatorio";
  
  public static final Integer FLAG_SITUACAO_DISPONIVEL = new Integer("0");
  public static final Integer FLAG_SITUACAO_SUBSISTENTE = new Integer("1");
  public static final Integer FLAG_SITUACAO_INSUBSISTENTE = new Integer("2");
  public static final Integer FLAG_SITUACAO_CANCELADO = new Integer("3");
  
  public static final Integer FLAG_PREENCHIMENTO_COMPLETO = new Integer("0");
  public static final Integer FLAG_PREENCHIMENTO_INCOMPLETO = new Integer("1");

  public static final String HORA_INFRACAO = "HORA_INFRACAO";
  public static final String DATA = "DATA";
  public static final String CNH = "CNH";
  public static final String CONDUTOR_CPF = "CONDUTOR_CPF"; //349753688 11
  public static final String PAIS = "PAIS"; //BR
  public static final String CONDUTOR_UF = "CONDUTOR_UF"; //SP
  public static final String PLACA_ALFA = "PLACA_ALFA"; //EBZ
  public static final String PLACA_NUMERICO = "PLACA_NUMERICO"; //3994
  public static final String CONDUTOR_RG = "CONDUTOR_RG";
  public static final String COD_INFRACAO = "COD_INFRACAO"; //5010
  public static final String DESD_01 = "DESD_01";
  public static final String DESD_02 = "DESD_02"; 
  public static final String DESD_03 = "DESD_03"; 
  public static final String VEICULO_UF = "VEICULO_UF"; //SP
  public static final String CONDUTOR_PAIS = "CONDUTOR_PAIS"; //BR
  public static final String RE_MATRICULA = "RE_MATRICULA";
  public static final String INFRACAO_MUNICIPIO = "INFRACAO_MUNICIPIO";
  public static final String INFRACAO_COD_MUNICIPIO = "INFRACAO_COD_MUNICIPIO";
  public static final String VEICULO_MUNICIPIO = "VEICULO_MUNICIPIO";
  public static final String VEICULO_COD_MUNICIPIO = "VEICULO_COD_MUNICIPIO"; 
  public static final String TERMO_DE_CONSTATACAO = "TERMO_DE_CONSTATACAO"; //345678
  public static final String TAMA = "TAMA";
  public static final String EMBARCADOR_CNPJ = "EMBARCADOR_CNPJ";
  public static final String NUMERO_AIT = "AIT_NRO";
  
  public static final String IDENT_TAMA = "T";
  public static final String IDENT_TERMO_CONSTATACAO = "C";
  
  public static final String URL_RENACH = "url_renach";
  
  public static final String RENACH_CAMPO_NOME_KEY = "NOME";
  public static final String RENACH_CAMPO_CPF_KEY = "CPF";
  public static final String RENACH_CAMPO_RENACH_KEY = "RENACH";
  public static final String RENACH_CAMPO_NUMERO_CNH_KEY = "NUMERO_CNH";
  public static final String RENACH_CAMPO_REGISTRO_CNH_KEY = "REGISTRO_CNH";
  public static final String RENACH_CAMPO_DATA_EMISSAO_KEY = "DATA_EMISSAO";
  public static final String RENACH_CAMPO_DATA_VALIDADE_KEY = "DATA_VALIDADE";
  public static final String RENACH_CAMPO_DATA_HABILITACAO_KEY = "DATA_HABILITACAO";
  public static final String RENACH_CAMPO_UF_KEY = "UF";
  public static final String RENACH_CAMPO_CATEGORIA_KEY = "CATEGORIA";
  
  public static final String URL_RENAVAM = "url_renavam";
  public static final String URL_RENAVAM_WEB = "url_renavam_web";
  
  public static final String RENAVAM_PLACA_KEY = "PLACA";
  public static final String RENAVAM_CHASSI_KEY = "CHASSI";
  public static final String RENAVAM_MUNICIPIO_KEY = "MUNICIPIO";
  public static final String RENAVAM_MARCA_MODELO_KEY = "MARCA_MODELO";
  public static final String RENAVAM_ESPECIE_KEY = "ESPECIE";
  public static final String RENAVAM_UF_KEY = "UF";
  
  public static final int SIT_CAMPO_NAO_IDENTIFICADO = 0;
  public static final int SIT_CAMPO_CONFERE = 1;
  public static final int SIT_CAMPO_NAO_CONFERE = 2;
  public static final int SIT_CAMPO_NAO_INFORMADO = 3;
  public static final int SIT_CAMPO_CNH_VENCIDA = 4;
  
  public static final String RENAVAM_PLACA_NAO_ENCONTRADA_KEY = "PLACA_NAO_ENCONTRADA";
  public static final String RENAVAM_CHASSI_NAO_ENCONTRADO_KEY = "CHASSI_NAO_ENCONTRADO";
  public static final String RENAVAM_MENSAGEM_PLACA_NAO_ENCONTRADA = "mensagem_placa_nao_encontrada_renavam";
  public static final String RENAVAM_MENSAGEM_CHASSI_NAO_ENCONTRADO = "mensagem_chassi_nao_encontrado_renavam";
  
  public static final String RENACH_CPF_NAO_ENCONTRADO_KEY = "CPF_NAO_ENCONTRADO";
  public static final String RENACH_CNH_NAO_ENCONTRADA_KEY = "CNH_NAO_ENCONTRADA";
  public static final String RENACH_MENSAGEM_CPF_NAO_ENCONTRADO = "mensagem_cpf_nao_encontrado_renach";
  public static final String RENACH_MENSAGEM_CNH_NAO_ENCONTRADA = "mensagem_cnh_nao_encontrada_renach";
  
  public static final String SIGLA_UF_PADRAO = "sigla_uf_padrao";
  public static final String ID_UF_PADRAO = "id_uf_padrao";
  
  public static final String IDENT_CAMPO_DOCUMENTO_RECOLHIDO_CNH = "ident_campo_documento_recolhido_cnh";
  public static final String IDENT_CAMPO_DOCUMENTO_RECOLHIDO_PPD = "ident_campo_documento_recolhido_ppd";
  public static final String IDENT_CAMPO_DOCUMENTO_RECOLHIDO_ACC = "ident_campo_documento_recolhido_acc";
  public static final String IDENT_CAMPO_DOCUMENTO_RECOLHIDO_CRLV = "ident_campo_documento_recolhido_crlv";
  public static final String IDENT_CAMPO_DOCUMENTO_RECOLHIDO_CRV = "ident_campo_documento_recolhido_crv";
  public static final String IDENT_CAMPO_DOCUMENTO_RECOLHIDO_OUTROS = "ident_campo_documento_recolhido_outros";
  
  public static final String FORM_TYPE_SC_ATEND = "SC-ATENDIMENTO";
  
  public static final String TAMA_IDENT_CAMPO_OBJETO = "tama_ident_campo_objeto";
  public static final String TAMA_IDENT_CAMPO_PINTURA = "tama_ident_campo_pintura";
  public static final String TAMA_IDENT_CAMPO_RECOLHIDO = "tama_ident_campo_recolhido";
  public static final String TAMA_IDENT_CAMPO_VEICULO = "tama_ident_campo_veiculo";
  public static final String TAMA_IDENT_CAMPO_REALIZADO = "tama_ident_campo_realizado";
  public static final String TAMA_IDENT_CAMPO_MEDIDA = "tama_ident_campo_medida";
  
  public static final String TAMA_IDENT_CAMPO_ASSINATURA_AGENTE = "tama_ident_campo_assinatura_agente";
  public static final String TAMA_IDENT_CAMPO_ASSINATURA_CONDUTOR = "tama_ident_campo_assinatura_condutor";
  public static final String TAMA_IDENT_CAMPO_ASSINATURA_COMISSARIO = "tama_ident_campo_assinatura_comissario";
  
  public static final String TC_IDENT_CAMPO_CONDUTOR_A = "tc_ident_campo_condutor_a";
  public static final String TC_IDENT_CAMPO_CONDUTOR_B = "tc_ident_campo_condutor_b";
  public static final String TC_IDENT_CAMPO_CONDUTOR_C = "tc_ident_campo_condutor_c";
  public static final String TC_IDENT_CAMPO_CONDUTOR_D = "tc_ident_campo_condutor_d";
  public static final String TC_IDENT_CAMPO_CONDUTOR_E = "tc_ident_campo_condutor_e";
  
  public static final String TC_IDENT_CAMPO_CONCLUSAO_ESTA_NAOESTA = "tc_ident_campo_conclusao_esta_naoesta";
  public static final String TC_IDENT_CAMPO_CONCLUSAO_ALCOOL = "tc_ident_campo_conclusao_alcool";
  public static final String TC_IDENT_CAMPO_CONCLUSAO_SUBSTANCIA = "tc_ident_campo_conclusao_substancia";
  public static final String TC_IDENT_CAMPO_CONCLUSAO_ETILOMETRO = "tc_ident_campo_conclusao_etilometro";
  public static final String TC_IDENT_CAMPO_CONCLUSAO_EXAMES = "tc_ident_campo_conclusao_exames";
  
  public static final String TC_IDENT_CAMPO_ASSINATURA_AGENTE = "tc_ident_campo_assinatura_agente";
  // Fim
  
  
  public static void initialize()
  {
    //if (!(new File("\\Program Files\\Anoto\\Anoto Forms Solution\\temp").exists()))
    if (!(new File(rsTempDirectory).exists()))
      try {
    	  //FileUtils.forceMkdir(new File("\\Program Files\\Anoto\\Anoto Forms Solution\\temp"));
    	  FileUtils.forceMkdir(new File(rsTempDirectory));
      } catch (IOException ex) {
        ex.printStackTrace();
      }


    File logFolderParentFile = new File(System.getProperty("user.dir"));

    //bundledPadLocation = System.getenv("SystemDrive") + "\\Program Files\\Anoto\\";
    bundledPadLocation = "\\apps\\rep\\anoto\\";

    if (logFolderParentFile.exists()) {
      if ((logFolderParentFile.getPath().endsWith("bin")) && (logFolderParentFile.getParentFile().exists()))
        logFolderParentFile = logFolderParentFile.getParentFile();

    }
    else {
      logFolderParentFile = new File(System.getenv("SystemDrive"));
    }

    //logFolder = logFolderParentFile.getPath() + "\\logs\\";
    logFolder = rootFolderName + "logs\\";
    try {
      FileUtils.forceMkdir(new File(logFolder));
	} catch (IOException ex) {
	  ex.printStackTrace();
	}
	
    SettingsData settingsData = AshSettingsControl.getInstance().getSettings();

    if (settingsData != null) {
      if ((settingsData.getLogLevel() == null) || (settingsData.getLogLevel().equals("")))
        logLevel = "FINE";
      else {
        logLevel = settingsData.getLogLevel().toString();
      }

      try
      {
        RenderLogger.setLogFolder(logFolder, logLevel);
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }

      adminErrorMailSubject = "Error mail from AFS system";

      failedLoginsLevel = settingsData.getMaxFailedPortalLogins();

      if ((settingsData.getVerificationModule() == null) || (settingsData.getVerificationModule().equals("")))
        verificationModule = "READSOFT";
      else {
        verificationModule = settingsData.getVerificationModule().toString();
      }

      VOFormsProcessorURL = settingsData.getFppUrl();

      if (VOFormsProcessorURL == null) {
        VOFormsProcessorURL = "";
        settingsData.setFppUrl("");
      }

      rootFolderLocation = settingsData.getRootFolder();

      if ((rootFolderLocation == null) || (rootFolderLocation.trim().length() == 0)) {
        rootFolderLocation = System.getenv("SystemDrive") + "\\";
      }

      if (!(rootFolderLocation.endsWith("\\"))) {
        rootFolderLocation += "\\";
      }

      settingsData.setRootFolder(rootFolderLocation);

      //ashRootFolderPath = rootFolderLocation + "Anoto_Forms_Solution_ASH_Root\\";
      ashRootFolderPath = rootFolderLocation + rootFolderName;

      //EXPORT_FOLDER = ashRootFolderPath + "Data\\export\\";
      EXPORT_FOLDER = ashRootFolderPath + "data\\export\\";

      VOFormModelerURL = VOFormsProcessorURL;

      SettingsDBHandler.updateSettings(settingsData);

      System.out.println(settingsData.toString());
    }

    ASH_VERSION = getProperty("version");
    changeProperty(ashRootFolderPathString, ashRootFolderPath);
    testMode = getProperty("specialAnotoMode").equalsIgnoreCase("true");
    
	// Wagner - Inclui
    // Início
    File assignFolder = new File(EXPORT_FOLDER + "\\ASSIGN\\");
    try {
      FileUtils.forceMkdir(assignFolder);
    } catch (IOException ex) {
   	  ex.printStackTrace();
    }
    
    PGC_FOLDER = ashRootFolderPath + "data\\pgc\\";
    File pgcFolder = new File(PGC_FOLDER);
    try {
      FileUtils.forceMkdir(pgcFolder);
    } catch (IOException ex) {
   	  ex.printStackTrace();
    }
    
    PGC_FOLDER_PROCESSED = PGC_FOLDER + "processed\\";
    File pgcFolderProcessed = new File(PGC_FOLDER_PROCESSED);
    try {
      FileUtils.forceMkdir(pgcFolderProcessed);
    } catch (IOException ex) {
   	  ex.printStackTrace();
    }
    
    PGC_FOLDER_ERROR = PGC_FOLDER + "error\\";
    File pgcFolderError = new File(PGC_FOLDER_ERROR);
    try {
      FileUtils.forceMkdir(pgcFolderError);
    } catch (IOException ex) {
   	  ex.printStackTrace();
    }
    
    PGC_FOLDER_RECEIVED = PGC_FOLDER + "received\\";
    File pgcFolderReceived = new File(PGC_FOLDER_RECEIVED);
    try {
      FileUtils.forceMkdir(pgcFolderReceived);
    } catch (IOException ex) {
   	  ex.printStackTrace();
    }
    // Fim
  }

  public static void changeProperty(String propertyName, String propertyValue)
  {
    InputStream is = null;
    try {
      Properties ashProperties = new Properties();
      //is = new FileInputStream(System.getenv("SystemDrive") + "\\Program Files\\Anoto\\Anoto Forms Solution\\ash_dont_change.sys");
      is = new FileInputStream(System.getenv("SystemDrive") + ashUserPropertiesFile);
      
      ashProperties.load(is);
      ashProperties.put(propertyName, propertyValue);
      //OutputStream os = new FileOutputStream(System.getenv("SystemDrive") + "\\Program Files\\Anoto\\Anoto Forms Solution\\ash_dont_change.sys");
      OutputStream os = new FileOutputStream(System.getenv("SystemDrive") + ashUserPropertiesFile);
      ashProperties.store(os, "Don't do any changes to this file!");
    } catch (Exception ioex) {
    } finally {
      try {
        is.close();
      } catch (Exception ex) {
      }
    }
  }

  public static String getProperty(String propertyName) {
    InputStream is = null;
    String propertValue = "";
    try
    {
      Properties ashProperties = new Properties();
      //is = new FileInputStream(System.getenv("SystemDrive") + "\\Program Files\\Anoto\\Anoto Forms Solution\\ash_dont_change.sys");
      is = new FileInputStream(System.getenv("SystemDrive") + ashUserPropertiesFile);
      ashProperties.load(is);
      propertValue = ashProperties.getProperty(propertyName).trim();
    } catch (Exception ioex) {
    } finally {
      try {
        is.close();
      }
      catch (Exception ex) {
      }
    }
    return propertValue;
  }
  
  // Incluido por Wagner
  // Inicio
  public static String getAfsProperty(String propertyName) {
      InputStream is = null;
      String propertyValue = "";

      try {
          Properties properties = new Properties();
          is = AshProperties.class.getResourceAsStream("/afs.properties");
          properties.load(is);
          propertyValue = properties.getProperty(propertyName);
      } catch (Exception ioex) {
      } finally {
          try {
              is.close();
          } catch (Exception ex) {
          }
      }

      return propertyValue;
  }
  // Fim
}