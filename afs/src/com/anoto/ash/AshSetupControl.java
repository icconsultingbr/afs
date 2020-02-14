package com.anoto.ash;

//import com.anoto.ash.database.DBHandler;
import com.anoto.ash.database.PadFile;
import com.anoto.ash.portal.AshFormControl;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
//import org.hibernate.Session;

@SuppressWarnings("rawtypes")
public class AshSetupControl
{
  public static boolean foldersCreated;
  public static boolean propertiesInitialized;

  public static void createFolders()
  {
    if (foldersCreated) {
      return;
    }

    AshLogger.logFine("Creating needed folders for the ASH.");

    if (!(propertiesInitialized)) {
      initializeProperties();
    }

    try
    {
      File ashRootFolder = new File(AshProperties.ashRootFolderPath);
      AshLogger.logFine("Creating, if not already exists, the folder: " + ashRootFolder.getPath());

      if (!(ashRootFolder.exists())) {
        FileUtils.forceMkdir(ashRootFolder);
      }

      if (!(ashRootFolder.exists())) {
        AshLogger.log("There was an error when trying to create needed folder for the ASH");
        throw new IllegalStateException("SEVERE ERROR: The ASH environment is not configured correctly.");
      }

      File dataFolder = new File(AshProperties.ashRootFolderPath + "\\Data");
      AshLogger.logFine("Creating, if not already exists, the folder: " + dataFolder.getPath());

      if (!(dataFolder.exists())) {
        FileUtils.forceMkdir(dataFolder);
      }

      if (!(dataFolder.exists())) {
        AshLogger.log("There was an error when trying to create folder " + dataFolder.getPath() + " for the ASH");
        throw new IllegalStateException("SEVERE ERROR: The ASH environment is not configured correctly.");
      }

      File exportFolder = new File(AshProperties.EXPORT_FOLDER);
      AshLogger.logFine("Creating, if not already exists, the folder: " + exportFolder.getPath());

      if (!(exportFolder.exists())) {
        FileUtils.forceMkdir(exportFolder);
      }

      if (!(exportFolder.exists())) {
        AshLogger.log("There was an error when trying to create needed folder for the ASH");
        throw new IllegalStateException("SEVERE ERROR: The ASH environment is not configured correctly.");
      }

      foldersCreated = true;
    } catch (IOException ioe) {
      ioe.printStackTrace();
      AshLogger.logSevere("Error when creating the comleted forms folder for the ASH.");
      throw new IllegalStateException("SEVERE ERROR: The ASH environment is not configured correctly.");
    }
  }

  public static void initializeProperties()
  {
    AshProperties.initialize();
    AshLogger.logFine("Initializing the AshProperties.");
    propertiesInitialized = true;
  }

  public static void loadAllPads()
  {
    if (!(foldersCreated)) {
      createFolders();
    }

    AshFormControl formControl = AshFormControl.getInstance();
    List padFiles = formControl.getAllPadFiles();

    for (Iterator iter = padFiles.iterator(); iter.hasNext(); ) { 
      PadFile padFile = (PadFile) iter.next();
      String padLicenseAddress = padFile.getPadLicenseAddress();
      AshLogger.log("About to load pad with license address: " + padLicenseAddress);
      AshCommons.loadPadFromByteArray(padFile.getPadFile());
    }
  }

  public static void startAll() throws IOException
  {
    initializeProperties();
    AshLogger.setLogLevel(AshProperties.logLevel);
    createFolders();
    loadAllPads();
    startUnsentMailSender();
  }

  public static boolean moveAshRoot(String folderToMovePath, String newLocationPath)
  {
    if (!(foldersCreated))
      return false;

    try
    {
      moveFolder(new File(folderToMovePath), new File(newLocationPath));
      AshProperties.changeProperty(AshProperties.ashRootFolderPathString, newLocationPath);
      AshLogger.logFine("Moved the ASH Root folder, old location: " + folderToMovePath + ", new location: " + newLocationPath);
    } catch (IOException ioe) {
      AshLogger.log("Error when trying to move the ASH Root folder, reason: " + ioe.getMessage());
      throw new IllegalStateException("Severe Error. Please Contact System Administrator.");
    }

    return true;
  }

  public static void createDefaultValuesForDB(boolean createDefaultAdmin) {
    Connection conn = null;
    Statement statement = null;
    try
    {
      Properties props = new Properties();
      InputStream is = AshSetupControl.class.getResourceAsStream("/hibernate.properties");
      props.load(is);

      String[] logLevels = { "FINE", "INFO" };
      String[] verificationModules = { "READSOFT", "VISION OBJECTS", "NONE" };
      String[] exportFormats = { "NONE", "CSV", "XML" };
      String[] imageFormats = { "NONE", "PNG", "JPG", "TIF", "PDF" };

      Double[] verify_all_values = { Double.valueOf(1.5D), Double.valueOf(1.5D), Double.valueOf(1.5D) };
      Double[] highest_values = { Double.valueOf(0.78000000000000003D), Double.valueOf(0.98999999999999999D), Double.valueOf(1D) };
      Double[] high_values = { Double.valueOf(0.52000000000000002D), Double.valueOf(0.85999999999999999D), Double.valueOf(0.69999999999999996D) };
      Double[] lagom_values = { Double.valueOf(0.5D), Double.valueOf(0.80000000000000004D), Double.valueOf(0.5D) };
      Double[] low_values = { Double.valueOf(0.45000000000000001D), Double.valueOf(0.66000000000000003D), Double.valueOf(0.29999999999999999D) };
      Double[] lowest_values = { Double.valueOf(0.45000000000000001D), Double.valueOf(0.53000000000000003D), Double.valueOf(0.10000000000000001D) };
      Double[] no_verification_values = { Double.valueOf(0D), Double.valueOf(0D), Double.valueOf(0D) };

      // Pega a conexão com o banco de dados a partir do data source do Tomcat.
      InitialContext cxt = new InitialContext();
      DataSource ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/afs");
      conn = ds.getConnection();

      statement = conn.createStatement();
      
      try {
    	  statement.execute("CREATE SEQUENCE seq_ficha_contingencia AS bigint START WITH 1 INCREMENT BY 1 ");
      }catch (Exception e) {
		//ignora pois a sequence pode já existir
      }

      String[] array = logLevels; 
      int length = array.length; 
      for (int i = 0; i < length; ++i) { 
    	  String logLvl = array[i];
    	  statement.execute("INSERT INTO tb_log_levels(name) values ('" + logLvl + "')");
      }

      array = verificationModules; 
      length = array.length; 
      for (int i = 0; i < length; ++i) {
    	  String verModule = array[i];
    	  statement.execute("INSERT INTO tb_verification_module(name) values ('" + verModule + "')");
      }

      array = exportFormats;
      length = array.length;
      for (int i = 0; i < length; ++i) { 
    	  String expFormat = array[i];
    	  statement.execute("INSERT INTO tb_export_formats(name) values ( '" + expFormat + "')");
      }

      array = imageFormats; 
      length = array.length; 
      for (int i = 0; i < length; ++i) { 
    	  String imgFormat = array[i];
    	  statement.execute("INSERT INTO tb_image_formats(name) values ( '" + imgFormat + "')");
      }

      statement.execute("INSERT INTO tb_predefined_thresholds(predefined_name,normalized_hwr,resemblance_hwr,resemblance_mkr) values ('Verify all'," + verify_all_values[0] + "," + verify_all_values[1] + "," + verify_all_values[2] + ")");
      statement.execute("INSERT INTO tb_predefined_thresholds(predefined_name,normalized_hwr,resemblance_hwr,resemblance_mkr) values ('Very strict'," + highest_values[0] + "," + highest_values[1] + "," + highest_values[2] + ")");
      statement.execute("INSERT INTO tb_predefined_thresholds(predefined_name,normalized_hwr,resemblance_hwr,resemblance_mkr) values ('Strict'," + high_values[0] + "," + high_values[1] + "," + high_values[2] + ")");
      statement.execute("INSERT INTO tb_predefined_thresholds(predefined_name,normalized_hwr,resemblance_hwr,resemblance_mkr) values ('Medium'," + lagom_values[0] + "," + lagom_values[1] + "," + lagom_values[2] + ")");
      statement.execute("INSERT INTO tb_predefined_thresholds(predefined_name,normalized_hwr,resemblance_hwr,resemblance_mkr) values ('Tolerant'," + low_values[0] + "," + low_values[1] + "," + low_values[2] + ")");
      statement.execute("INSERT INTO tb_predefined_thresholds(predefined_name,normalized_hwr,resemblance_hwr,resemblance_mkr) values ('Very tolerant'," + lowest_values[0] + "," + lowest_values[1] + "," + lowest_values[2] + ")");
      statement.execute("INSERT INTO tb_predefined_thresholds(predefined_name,normalized_hwr,resemblance_hwr,resemblance_mkr) values ('No verification'," + no_verification_values[0] + "," + no_verification_values[1] + "," + no_verification_values[2] + ")");

      statement.execute("INSERT INTO tb_thresholds(field_name, fk_predefined_id) VALUES ('All', (SELECT id FROM tb_predefined_thresholds WHERE predefined_name = 'Medium'))");

      statement.execute("INSERT INTO tb_roles(role_name) VALUES ('Admin')");
      statement.execute("INSERT INTO tb_roles(role_name) VALUES ('Verifier')");
      statement.execute("INSERT INTO tb_roles(role_name) VALUES ('User')");

      if (createDefaultAdmin) {
    	  statement.execute("INSERT INTO tb_users(user_name,password,number_of_failed_logins,email,fk_role_id,locked,user_status) VALUES ('admin','21232f297a57a5a743894a0e4a801fc3',0,'',1,0,1)");
      }
      
      statement.execute("INSERT INTO tb_users(user_name,password,number_of_failed_logins,email,fk_role_id,locked,user_status) VALUES ('viewer','21232f297a57a5a743894a0e4a801fc3',0,'',1,0,1)");
  
      int idForVM = 1;

      if ((props.getProperty("com.anoto.verificationmodule") != null) && (props.getProperty("com.anoto.verificationmodule").equalsIgnoreCase("VO"))) {
    	  idForVM = 2;
      }
      
      if ((props.getProperty("com.anoto.verificationmodule") != null) && (props.getProperty("com.anoto.verificationmodule").equalsIgnoreCase("NONE"))) {
    	  idForVM = 3;
      }

      statement.execute("INSERT INTO tb_ash_settings(max_failed_portal_logins,root_folder,fpp_url,session_timeout,fk_verification_module_id,fk_log_level_id) VALUES (4,'','http://localhost/myscript-formprocessor/',900," + idForVM + ",2)");

      array = AshProperties.supportedFonts; 
      length = array.length; 
      for (int i = 0; i < length; ++i) { 
    	  String supportedFont = array[i];
    	  statement.execute("INSERT INTO tb_fonts(name) VALUES ('" + supportedFont + "')");
      }
      
    } catch (SQLException sqlex) {
      sqlex.printStackTrace();
      
    } catch (IOException ioex) {
      ioex.printStackTrace();
      
    } catch (Exception e) {
      e.printStackTrace();
      
    } finally {
      try {
        if (statement != null) {
          statement.close();
        }

        if (conn != null)
          conn.close();
      }
      catch (SQLException ex)
      {
      }
    }
  }

  /*
  private static boolean checkIfTableExists() {
    boolean result = false;
    DBHandler dbHandler = new DBHandler();
    Session session = DBHandler.getCurrentSession();
    DBHandler.beginTransaction(session);

    if (session.createSQLQuery("SELECT * FROM tb_ash_settings where id = ?").setInteger(0, 1).uniqueResult() == null) {
      result = true;
    }

    DBHandler.commitTransaction(session);
    DBHandler.closeSession();
    return result;
  }
  */

  private static void moveFolder(File folderToMove, File destinationFolder)
    throws IOException
  {
    FileUtils.copyDirectoryToDirectory(folderToMove, destinationFolder);
    FileUtils.forceDelete(folderToMove);
  }

  private static void startUnsentMailSender() {
    UnsentMailSender.getInstance().startRunning();
  }
}