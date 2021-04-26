package net.datenwerke.rs.samples.maintenance;

import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.nio.file.Paths
import java.nio.file.Files

/**
 * XLSRemoveTabsLocal.groovy
 * Type: Normal Script
 * Last tested with: ReportServer 3.4.0-6035
 * Reads in a *.xls or *.xlsx file and deletes single/multiple tabs by their names 
 * and writes as output a new *.xls or *.xlsx file.
 */

def fileInput = "C:/veränderbare Dateien/202104211707_ANW_CUSTOMERS - ANW_CUSTOMERS.xlsx"

def fileOutput = "C:/veränderbare Dateien/202104211707_ANW_CUSTOMERS - ANW_CUSTOMERS_verändert.xlsx"

if(fileInput.endsWith("xlsx")){
    def xlsxFile = Paths.get(fileInput)
    Files.newInputStream(xlsxFile).withCloseable {
       workBook = new XSSFWorkbook(it)
    }
}
else if(fileInput.endsWith("xls")){
    def xlsFile = Paths.get(fileInput)
    Files.newInputStream(xlsFile).withCloseable{
       workBook = new HSSFWorkbook(it)
    }
}
else{
    tout.println "No valid format for the processed files!"  
}

def sheetIndex = 0

//please put in the names of the sheets you want to get deleted!
def sheetNames = ["Dynamic list2", "Dynamic_list3"]

sheetNames.each { sheetName ->

    def pSheet = workBook.getSheet(sheetName)

    if(null != pSheet) {
       sheetIndex = workBook.getSheetIndex(pSheet)
       workBook.removeSheetAt(sheetIndex)
       def FileOutputStream = new FileOutputStream(fileOutput)
       workBook.write(FileOutputStream)
    }
}

return FileOutputStream