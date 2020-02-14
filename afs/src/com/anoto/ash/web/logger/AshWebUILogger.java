package com.anoto.ash.web.logger;

import org.apache.log4j.Logger;


@SuppressWarnings("rawtypes")
public class AshWebUILogger
{
  
  public static void debug(Class loggerClass, String message)
  {
    Logger logger = Logger.getLogger(loggerClass);
    logger.debug(message);
  }

  public static void error(Class loggerClass, String message) {
    Logger logger = Logger.getLogger(loggerClass);
    logger.error(message);
  }

  public static void fatal(Class loggerClass, String message) {
    Logger logger = Logger.getLogger(loggerClass);
    logger.fatal(message);
  }

  public static void warn(Class loggerClass, String message) {
    Logger logger = Logger.getLogger(loggerClass);
    logger.warn(message);
  }

  public static void info(Class loggerClass, String message) {
    Logger logger = Logger.getLogger(loggerClass);
    logger.info(message);
  }
}