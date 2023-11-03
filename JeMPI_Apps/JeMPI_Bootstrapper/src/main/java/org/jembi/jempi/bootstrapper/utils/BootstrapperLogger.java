package org.jembi.jempi.bootstrapper.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
public class BootstrapperLogger {
    public static Logger getChildLogger(Logger parentLogger, String childLoggerName){
        return LogManager.getLogger(String.format("%s > %s", parentLogger.getName(), childLoggerName));
    }
    public static Logger getLogger(String loggerName){
        return LogManager.getLogger(loggerName);
    }
}
