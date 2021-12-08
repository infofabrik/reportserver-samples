package net.datenwerke.rs.samples.tools.excel

import org.apache.poi.ss.usermodel.WorkbookFactory

import net.datenwerke.rs.fileserver.service.fileserver.FileServerService
import net.datenwerke.rs.fileserver.service.fileserver.entities.FileServerFile
import org.apache.commons.io.FilenameUtils

/**
 * ExcelRemoveTabsServer.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 3.4.0-6035
 * Reads an Excel file on ReportServer FileServer and deletes a list of given sheets by their names.
 * Writes output into the same Excel file renaming it.
 */

/*
 * Please put in the number file ID as seen on the download-button of RS.
 * E.g.: http://localhost:8080/reportserver/fileServerAccess?id=123
 * Pass it as a long (L).
 */
FileServerFile excelFile = GLOBALS.findEntity(FileServerFile.class, 123L)

// we make sure the file exists
assert excelFile

fileServerService = GLOBALS.getInstance(FileServerService.class)

// the names of the sheets you want to get deleted
def sheetNamesToDelete = [
   'sheet1',
   'sheet2',
   'sheet3'
]

(new ByteArrayInputStream(excelFile.data)).withCloseable { is ->
   def workbook = WorkbookFactory.create(is)

   //delete the sheets
   sheetNamesToDelete.each { sheetNameToDelete ->
      def sheetIndex = workbook.getSheetIndex(sheetNameToDelete)
      if(sheetIndex >= 0) {
         tout.println "Deleting $sheetNameToDelete..."
         workbook.removeSheetAt(sheetIndex)
      } else {
         tout.println "Sheet $sheetNameToDelete does not exist"
      }
   }

   (new ByteArrayOutputStream()).withCloseable { os ->
      workbook.write os
      //override old data with modified workbook
      excelFile.data = os.toByteArray()
   }

   def extension = FilenameUtils.getExtension(excelFile.name)
   def baseFilename = FilenameUtils.getBaseName(excelFile.name)

   //rename filename
   excelFile.name = "$baseFilename (changed).$extension"

   //save into db
   fileServerService.merge(excelFile)
}

return 'Sheets were successfully deleted'