package net.datenwerke.rs.samples.maintenance

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.nio.file.Paths
import java.nio.file.Files

/**
 * ExcelRemoveTabsLocal.groovy
 * Type: Normal Script
 * Last tested with: ReportServer 3.5.0-6037
 * Reads an Excel file and deletes a list of given sheets by their names.
 * Writes output into a new Excel file.
 */

def fileInput = '/path/to/yourfile.xlsx'
def fileOutput = '/path/to/yourfile_output.xlsx'
// please put in the names of the sheets you want to get deleted
def sheetNamesToDelete = ['Dynamic list2', 'sheet2']

def excelFile = Paths.get(fileInput)
def excelOutputFile = Paths.get(fileOutput)

assert Files.exists(excelFile)
assert !Files.exists(excelOutputFile)
// matches only .xls or .xlsx
def pattern = /.*\.xlsx+$/
assert excelFile.fileName.toString().toLowerCase() ==~ pattern
assert excelOutputFile.fileName.toString().toLowerCase() ==~ pattern

Files.newInputStream(excelFile).withCloseable { is ->
   def workbook = WorkbookFactory.create(is)

   // delete the sheets
   sheetNamesToDelete.each { sheetNameToDelete ->
      def sheetIndex = workbook.getSheetIndex(sheetNameToDelete)
      if(sheetIndex >= 0) {
         tout.println "Deleting $sheetNameToDelete..."
         workbook.removeSheetAt(sheetIndex)

      } else {
         tout.println "Sheet $sheetNameToDelete does not exist"
      }
   }

   Files.newOutputStream(excelOutputFile).withCloseable { os ->
      workbook.write os
   }
}

return 'Sheets were successfully deleted'