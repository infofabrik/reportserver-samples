package net.datenwerke.rs.samples.tools.datasinks

import net.datenwerke.rs.core.service.datasinkmanager.DatasinkTreeService
import net.datenwerke.rs.core.service.reportmanager.ReportService
import net.datenwerke.rs.core.service.reportmanager.ReportExecutorService
import net.datenwerke.rs.core.service.datasinkmanager.DatasinkService

/**
 * sendReportToDatasink.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6062
 * Executes a given report and exports it as PDF.
 * Finally, sends the PDF to a given datasink.
 */

/**** USER SETTINGS ****/

REPORT_ID = 123L
OUTPUT_FORMAT = ReportExecutorService.OUTPUT_FORMAT_PDF
FILE_ENDING = '.pdf'
DATASINK_ID = 456L

// email settings
EMAIL_DATASINK = 'Default Email Datasink'
EMAIL_ATTACHMENT_FILENAME = 'My Report'
EMAIL_SUBJECT = 'Test Email Subject'
EMAIL_TEXT = 'Test Email Message Body'

/***********************/

def reportService = GLOBALS.getInstance(ReportService)
def datasinkTreeService = GLOBALS.getInstance(DatasinkTreeService)
def reportExecutorService = GLOBALS.getInstance(ReportExecutorService)
def datasinkService = GLOBALS.getInstance(DatasinkService)

def report = reportService.getReportById(REPORT_ID)  // get Report by ID
assert report

def compiledReport = reportExecutorService.execute(report, OUTPUT_FORMAT)
def datasink = datasinkTreeService.getDatasinkById(DATASINK_ID)

assert datasink

datasinkService.exportIntoDatasink compiledReport.report, datasink, FILE_ENDING
