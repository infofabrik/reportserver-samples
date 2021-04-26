package net.datenwerke.rs.samples.maintenance;

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

import javax.persistence.EntityManager

import net.datenwerke.rs.fileserver.service.fileserver.entities.FileServerFile
import net.datenwerke.rs.fileserver.service.fileserver.FileServerService

/**
 * ExcelPOIRemoveTabsServer.groovy
 * Type: Normal Script
 * Last tested with: ReportServer 3.4.0-6035
 * Reads an Excel file on ReportServer FileServer and deletes a list of given sheets by their names.
 * Writes output into the given Excel file and renames it.
 */

fileServerService = GLOBALS.getInstance(FileServerService.class)

/*
 * Please put in the number of the file as seen on the downloadbutton of RS.
 * In particular the Id so what comes after id=*
 * e.g.: http://localhost:8080/reportserver/fileServerAccess?id=42799
 */
FileServerFile excelFile = GLOBALS.findEntity(FileServerFile.class, 46599l)

def bytArr = excelFile.getData()

def fileInput = new ByteArrayInputStream(bytArr)

def fileOutput = excelFile

def fileOutputStream = new ByteArrayInputStream(bytArr)

//please put in the names of the sheets you want to get deleted!
def sheetNamesToDelete = ["Dynamic list2", 'sheet2', "Dynamic list1"]

fileInput.withCloseable { is ->
    def workBook = WorkbookFactory.create(is)
    
    //delete the sheets
    sheetNamesToDelete.each { sheetNameToDelete ->
      def sheetIndex = workBook.getSheetIndex(sheetNameToDelete)
      if(sheetIndex >= 0) {
         tout.println "Deleting $sheetNameToDelete..."
         workBook.removeSheetAt(sheetIndex)

      } else {
         tout.println "Sheet $sheetNameToDelete does not exist"
      }
   }

   fileOutputStream.withCloseable { os ->
      os = new ByteArrayOutputStream()
      workBook.write os
      def byteArray = os.toByteArray();
      fileOutput.data = byteArray
      def str = fileOutput.getName()
      def ext = fileOutput.getName()
      str = str.substring(0, str.indexOf("."))
      extension = ext.substring(ext.indexOf("."), ext.length())
      fileOutput.setName(str + " - verändert" + extension)
      fileServerService.merge(fileOutput)
   }
}

return 'Sheets were successfully deleted'