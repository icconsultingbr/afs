package com.anoto.ash;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;


//public class StartupPgcProcessorServlet extends HttpServlet implements Runnable {
public class StartupPgcProcessorServlet extends HttpServlet {
	 
	private static final long serialVersionUID = 1L;
	
	@Override
	public void init() throws ServletException {
		String intervaloMonitoramento = AshProperties.getAfsProperty("intervalo_monitoramento_pgc");
		if (intervaloMonitoramento == null || intervaloMonitoramento.equals("")) {
			intervaloMonitoramento = "60";
		}
		
		int intervalo = Integer.parseInt(intervaloMonitoramento);
		
 		ProcessFileTask processFileTask = new ProcessFileTask();
    	ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    	scheduledExecutorService.scheduleAtFixedRate(processFileTask, 10, intervalo, TimeUnit.SECONDS);
	}
	
	/*
	public void run() {
		int intervalo;
		try {
			String intervaloMonitoramento = AshProperties.getProperty(AshProperties.INTERVALO_MONITORAMENTO_PGC);
			if (intervaloMonitoramento == null || intervaloMonitoramento.equals("")) {
				intervaloMonitoramento = "60";
			}
			
			intervalo = Integer.parseInt(intervaloMonitoramento);
			
			while (true) {
				System.out.println("###Thread: " + thread.getName() + " - " + AshCommons.getCurrentDateAndTime("dd/MM/yyyy HH:mm:ss"));
				readPGC();
				Thread.sleep(intervalo * 1000);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public static synchronized void readPGC() throws ServletException {
		try {
			FilenameFilter filenameFilter = new FilenameFilter() {
				public boolean accept(File b, String name) { 
					return name.endsWith(".pgc"); 
				} 
			}; 
		       
			File dirPgc = new File(AshProperties.PGC_FOLDER_RECEIVED);
			File[] lista = dirPgc.listFiles(filenameFilter);
			
			if (lista != null && lista.length > 0) {
				for (int i = 0; i < lista.length; i++) {
					// Espera de 100 milisegundos.
					Thread.sleep(100L);
					
					try {
						File pgcFile = lista[i];
						
						// Verifica se o arquivo pode ser lido.
						while (!pgcFile.canRead()) {
							Thread.sleep(500L);
						}
						
						// Data de upload do arquivo.
						Date uploadDate = new Date(pgcFile.lastModified());
						// Nome do arquivo PGC.
						String pgcFileName = pgcFile.getName();
						// Referência para o objeto Pen.
						Pen pen = null;
						// Cria um objeto input stream do arquivo PGC.
						FileInputStream fis = new FileInputStream(pgcFile);
						
						boolean success = true;
	
						try {
							// Processa arquivo PGC.
							System.out.println("PGC SERVICE: Processando arquivo " + pgcFileName + " " + AshCommons.getCurrentDateAndTime("dd/MM/yyyy HH:mm:ss"));
							pen = PenHome.read(fis);
							pen.setApplicationName(AshProperties.appName);
							onPGC(pen, uploadDate, pgcFileName);
						
						} catch (Exception e) {
							success = false;
							System.out.println("PGC SERVICE ERROR: Causa: " + e.getCause());
							
						} finally {
							// Fecha o input stream do arquivo PGC.
							fis.close();
							
							if (success) {
								// Copia o arquivo PGC para a pasta de arquivos processados.
								String pgcFileNameProcessed = pgcFileName.substring(0, pgcFileName.lastIndexOf(".")) + "_" + pen.getPenData().getPenSerial() + AshProperties.PGC_FILE_EXT;
								File dirCurrentDate = new File(AshProperties.PGC_FOLDER_PROCESSED + AshCommons.getCurrentDateAndTime("yyyyMMdd"));
					            if (!dirCurrentDate.exists()) {
					            	FileUtils.forceMkdir(dirCurrentDate);
					            }
								pgcFile.renameTo(new File(dirCurrentDate, pgcFileNameProcessed));
								System.out.println("PGC SERVICE: Arquivo processado " + pgcFileNameProcessed + " " + AshCommons.getCurrentDateAndTime("dd/MM/yyyy HH:mm:ss"));
								
							} else {
								// Copia o arquivo PGC para a pasta de arquivos com erro.
								File dirCurrentDate = new File(AshProperties.PGC_FOLDER_ERROR + AshCommons.getCurrentDateAndTime("yyyyMMdd"));
					            if (!dirCurrentDate.exists()) {
					            	FileUtils.forceMkdir(dirCurrentDate);
					            }
					            FileUtils.copyFileToDirectory(pgcFile, dirCurrentDate);
								System.out.println("PGC SERVICE: Erro ao processar arquivo " + pgcFileName + " " + AshCommons.getCurrentDateAndTime("dd/MM/yyyy HH:mm:ss"));
					            FileUtils.forceDelete(pgcFile);
							}
						}
						
					} catch (Exception e) {
						System.out.println("PGC SERVICE ERROR: Causa: " + e.getCause());
						e.printStackTrace();
					}
				}
			}
					  
		} catch (Exception e) {
			System.out.println("PGC SERVICE ERROR: Causa: " + e.getCause());
			e.printStackTrace();
		}
	}
	
	public static synchronized void onPGC(Pen pen, Date uploadDate, String pgcFileName) throws ServletException {
		AshPgcControl pgcControl = new AshPgcControl();
		try {
			pgcControl.processPgc(pen, uploadDate, pgcFileName);
			System.gc();
		      
		} catch (Exception e) {
			try {
				handleException(e);
			} catch (Exception ex) {
				throw new ServletException("ASH got exception: " + e.getMessage());
			}
		}
	}
	
	private static void handleException(Exception e) throws Exception {
		MailControl mailControl = MailControl.getInstance();
	    if (e instanceof AshException) {
	    	AshException ashException = (AshException)e;
	    	Exception wrappedException = ashException.getWrappedException();

	    	//getServletContext().log("AshServlet forced to handle exception: " + wrappedException.toString());
	    	mailControl.sendAdminMail("ASH got exception! Provided error message: " + wrappedException.toString(), AshProperties.adminErrorMailSubject, null);
	    } else {
	    	//getServletContext().log("AshServlet forced to handle exception: " + e.toString());
	    	mailControl.sendAdminMail("ASH got exception! Exception error message: " + e.toString(), AshProperties.adminErrorMailSubject, null);
	    }

	    throw e;
	}
	*/
	
}