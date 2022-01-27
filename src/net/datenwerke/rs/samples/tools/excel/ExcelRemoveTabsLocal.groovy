package net.datenwerke.rs.samples.tools.excel

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.nio.file.Paths
import java.nio.file.Files

/**
 * ExcelRemoveTabsLocal.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.0.0-6053
 * Reads an Excel file and deletes a list of given sheets by their names.
 * Writes output into a new Excel file.
 */

def fileInput = '/path/to/yourfile.xlsx'
def fileOutput = '/path/to/yourfile_output.xlsx'
// the names of the sheets you want to get deleted
def sheetNamesToDelete = [
   'sheet1',
   'sheet2',
   'sheet3'
]

def excelFile = Paths.get(fileInput)
def excelOutputFile = Paths.get(fileOutput)

assert Files.exists(excelFile)
assert !Files.exists(excelOutputFile)
// matches only Excel files
def pattern = /(?ix)    # case insensitive(i), ignore space(x)
^                       # start of line               
(\w*\s*)*               # any number of word characters followed by any number of spaces
\.xlsx?$                # ending in .xls or .xlsx
/
assert excelFile.fileName.toString() ==~ pattern
assert excelOutputFile.fileName.toString() ==~ pattern

Files.newInputStream(excelFile).withCloseable { is ->
   def workbook = WorkbookFactory.create(is)

   // delete the sheets
   sheetNamesToDelete.each { sheetNameToDelete ->
      def sheetIndex = workbook.getSheetIndex(sheetNameToDelete)
      if(sheetIndex >= 0) {
         tout.println "Deleting $sheetNameToDelete..."
         workbook.removeSheetAt sheetIndex

      } else {
         tout.println "Sheet $sheetNameToDelete does not exist"
      }
   }

   Files.newOutputStream(excelOutputFile).withCloseable { os -> workbook.write os }
}

return 'Sheets were successfully deleted'