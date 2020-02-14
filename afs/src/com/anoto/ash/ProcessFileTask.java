package com.anoto.ash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.anoto.api.Pen;
import com.anoto.api.PenHome;

public class ProcessFileTask implements Runnable {
	
	public void run() {
		FilenameFilter filenameFilter = new FilenameFilter() {
			public boolean accept(File b, String name) { 
				return name.endsWith(".pgc"); 
			} 
		}; 
		File dirPgc = new File(AshProperties.PGC_FOLDER_RECEIVED);
		File[] listFiles = dirPgc.listFiles(filenameFilter);
		
		for (int i = 0; i < listFiles.length; i++) {
			try {
				File pgcFile = listFiles[i];
				if (pgcFile != null && pgcFile.exists()) {
					long UM_MEGA = 1000 * 1024;
					if (pgcFile.length() > (UM_MEGA)) {
						//NAO PROCESSAR ARQUIVOS ACIMA DE 1MB
						File dirCurrentDate = new File(AshProperties.PGC_FOLDER_ERROR + AshCommons.getCurrentDateAndTime("yyyyMMdd"));
			            if (!dirCurrentDate.exists()) {
			            	FileUtils.forceMkdir(dirCurrentDate);
			            }
			            pgcFile.renameTo(new File(dirCurrentDate, pgcFile.getName()));
					}else{						
						// Data de upload do arquivo.
						Date uploadDate = new Date(pgcFile.lastModified());
						// Nome do arquivo PGC.
						String pgcFileName = pgcFile.getName();
						// Cria um objeto input stream do arquivo PGC.
						FileInputStream fis = new FileInputStream(pgcFile);
						
						// Processa arquivo PGC.
						try {
							System.out.println("PGC PROCESSANDO: " + pgcFileName + " " + AshCommons.getCurrentDateAndTime("dd/MM/yyyy HH:mm:ss"));
							Pen pen = PenHome.read(fis);
							fis.close();
							pen.setApplicationName(AshProperties.appName);
							onPGC(pen, uploadDate, pgcFileName);
							
							// Copia o arquivo PGC para a pasta de arquivos processados.
							if (AshProperties.PGC_FOLDER_PROCESSED != null && !("").equals(AshProperties.PGC_FOLDER_PROCESSED)) {
								File dirCurrentDate = new File(AshProperties.PGC_FOLDER_PROCESSED + AshCommons.getCurrentDateAndTime("yyyyMMdd"));
					            if (!dirCurrentDate.exists()) {
					            	FileUtils.forceMkdir(dirCurrentDate);
					            }
								pgcFile.renameTo(new File(dirCurrentDate, pgcFileName));
								System.out.println("PGC PROCESSADO: " + pgcFileName + " " + AshCommons.getCurrentDateAndTime("dd/MM/yyyy HH:mm:ss"));
							}
							
						} catch (Exception e) {
							if (fis != null) fis.close();
							
							// Copia o arquivo PGC para a pasta de arquivos com erro.
							if (AshProperties.PGC_FOLDER_ERROR != null && !("").equals(AshProperties.PGC_FOLDER_ERROR)) {
								File dirCurrentDate = new File(AshProperties.PGC_FOLDER_ERROR + AshCommons.getCurrentDateAndTime("yyyyMMdd"));
					            if (!dirCurrentDate.exists()) {
					            	FileUtils.forceMkdir(dirCurrentDate);
					            }
					            pgcFile.renameTo(new File(dirCurrentDate, pgcFileName));
								System.out.println("PGC ERRO: " + pgcFileName + " " + AshCommons.getCurrentDateAndTime("dd/MM/yyyy HH:mm:ss"));
							}
						}						
					}
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onPGC(Pen pen, Date uploadDate, String pgcFileName) throws AshException {
		AshPgcControl pgcControl = new AshPgcControl();
		System.gc();
		pgcControl.processPgc(pen, uploadDate, pgcFileName);
	}

}