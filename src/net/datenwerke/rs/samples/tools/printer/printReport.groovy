package net.datenwerke.rs.samples.tools.printer

import java.awt.print.PrinterJob

import javax.print.PrintServiceLookup

import net.datenwerke.rs.core.service.reportmanager.ReportExecutorService
import net.datenwerke.rs.core.service.reportmanager.ReportService
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable

/**
 * printers.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6062
 * Exports a given report as PDF and sends it to a given printer.
 * Note that you can list all available printers with printers.groovy.
 */

/**** USER SETTINGS ****/

REPORT_ID = 13666L
OUTPUT_FORMAT = ReportExecutorService.OUTPUT_FORMAT_PDF
PRINTER = 'Brother MFC-250C'

/***********************/

def reportService = GLOBALS.getInstance(ReportService)
def reportExecutorService = GLOBALS.getInstance(ReportExecutorService)

def report = reportService.getReportById(REPORT_ID)  // get Report by ID
assert report
def compiledReport = reportExecutorService.execute(report, OUTPUT_FORMAT)

def printServices = PrintServiceLookup.lookupPrintServices(null, null)
def printer = printServices.find{ it.name == PRINTER }
assert printer

tout.println "Printing to $printer.name..."

PDDocument.load(compiledReport.report).withCloseable { document -> 
   PrinterJob job = PrinterJob.getPrinterJob()
   job.pageable = new PDFPageable(document)
   job.printService = printer
   job.print()
}
