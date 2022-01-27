package net.datenwerke.rs.samples

import java.util.logging.Level
import java.util.logging.Logger

/**
 * logger.groovy
 * Version: 1.0.1
 * Type: Normal script
 * Last tested with: ReportServer 4.0.0-6053
 * Prints output into the Tomcat ReportServer logs using different logging levels. 
 * Depending on your log configuration, some log messages may/may not appear.
 * You can adapt the level in this file: logging-rs.properties
 * SEVERE messages should always appear, as SEVERE level is highest.
 * More information here: https://docs.oracle.com/javase/8/docs/api/java/util/logging/Level.html
 */

def logger = Logger.getLogger(getClass().name)

/* Prefix for logger messages. 
 * Allows to quickly find messages of this specific script */
def prefix = "logger.groovy"

tout.println "Executing logger.groovy"

//in descending order
logger.log(Level.SEVERE, "$prefix SEVERE-level message")
logger.log(Level.WARNING, "$prefix WARNING-level message")
logger.log(Level.INFO, "$prefix INFO-level message")
logger.log(Level.CONFIG, "$prefix CONFIG-level message")
logger.log(Level.FINE, "$prefix FINE-level message")
logger.log(Level.FINER, "$prefix FINER-level message")
logger.log(Level.FINEST, "$prefix FINEST-level message")
