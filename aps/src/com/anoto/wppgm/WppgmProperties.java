package com.anoto.wppgm;

public class WppgmProperties
{
  public static String TEMPFOLDER = WppgmCommons.getProperty("printer_profiles_folder") + "/anoto/aps/";
  public static String LOGFOLDER = TEMPFOLDER + "logs/";
  public static String PRINTFOLDER = TEMPFOLDER + "print/";
  static long splitEpsSleepIntervalPerPage = 1000L;
}