package net.datenwerke.rs.samples.tools.printer

import javax.print.PrintService
import javax.print.PrintServiceLookup

/**
 * printers.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6062
 * Lists all available printers.
 */

def printServices = PrintServiceLookup.lookupPrintServices(null, null)
tout.println "Number of printers: $printServices.length"

printServices.each{ tout.println it.name }

""

