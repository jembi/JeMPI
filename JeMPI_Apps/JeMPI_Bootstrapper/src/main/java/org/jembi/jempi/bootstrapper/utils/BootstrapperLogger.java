package org.jembi.jempi.bootstrapper.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BootstrapperLogger {
   protected BootstrapperLogger() { }
   public static Logger getChildLogger(
         final Logger parentLogger,
         final String childLoggerName) {
      return LogManager.getLogger(String.format("%s > %s", parentLogger.getName(), childLoggerName));
   }

   public static Logger getLogger(final String loggerName) {
      return LogManager.getLogger(loggerName);
   }
}
