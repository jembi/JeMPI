package org.jembi.jempi.bootstrapper.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BootstrapperLogger {
    public static Logger getChildLogger(Logger parentLogger, String childLoggerName) {
        return LogManager.getLogger(String.format("%s > %s", parentLogger.getName(), childLoggerName));
    }
    public static Logger getLogger(final String loggerName) {
        return LogManager.getLogger(loggerName);
    }
}
