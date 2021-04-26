package net.datenwerke.rs.samples.maintenance;

import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

import javax.persistence.EntityManager

import net.datenwerke.rs.fileserver.service.fileserver.entities.FileServerFile
import net.datenwerke.rs.fileserver.service.fileserver.FileServerService

/**
 * ExcelRemoveTabsServer.groovy
 * Type: Normal Script
 * Last tested with: ReportServer 3.4.0-6035
 * Reads in a *.xls or *.xlsx file and deletes single/multiple tabs by their names 
 * and writes as output a new *.xls or *.xlsx file.
 */

fileServerService = GLOBALS.getInstance(FileServerService.class)

/*
 * Please put in the number of the file as seen on the downloadbutton of RS.
 * In particular the Id so what comes after id=*
 * e.g.: http://localhost:8080/reportserver/fileServerAccess?id=42799
 */
FileServerFile excelFile = GLOBALS.findEntity(FileServerFile.class, 45856l)

def bytArr = excelFile.getData()

def fileInput = new ByteArrayInputStream(bytArr)

def fileOutput = excelFile

def isXLSFile = false

if(excelFile.getContentType().endsWith("sheet")){
    fileInput.withCloseable{
       workBook = new XSSFWorkbook(it)
    }
    if(!isXLSFile){
      addition = ".xlsx"
        } else {
      addition = ".xls"
    }
} else if(excelFile.getContentType().endsWith("ms-excel")){
    fileInput.withCloseable{
       workBook = new HSSFWorkbook(it)
    }
    if(isXLSFile){
      addition = ".xlsx"
        } else {
      addition = ".xls"
    }
} else {
    tout.println "No valid format for the processed files!"  
}

def sheetIndex = 0

//please put in the names of the sheets you want to get deleted!
//def sheetNames = ["EFRE-Teil1", "EU-Ma�nahmen_1"]
def sheetNames = ["Dynamic list2", "Dynamic list1"]

sheetNames.each { sheetName ->
    
    def pSheet = workBook.getSheet(sheetName)
    
    if(null != pSheet) {
       sheetIndex = workBook.getSheetIndex(pSheet)
       workBook.removeSheetAt sheetIndex 
       def outputStream = new ByteArrayOutputStream()
       workBook.write(outputStream)
       def byteArray = outputStream.toByteArray();
       fileOutput.data = byteArray
    }
}

def str = fileOutput.getName()

str = str.substring(0, str.indexOf("."))

fileOutput.setName(str + " - ver�ndert" + addition)

fileServerService.merge(fileOutput)