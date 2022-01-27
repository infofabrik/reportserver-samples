package net.datenwerke.rs.samples.tools.excel.excel2csv

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import java.text.NumberFormat

/**
 * excel2csv.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.0.0-6053
 * Demonstrates how to read a given Excel file with help of Apache POI library, 
 * transform the data and write the results into a given CSV file.
 */

// settings start ==========================================================================
INPUT = '/Users/eduardo/Desktop/excelFile.xlsx'
OUTPUT = '/path/to/your/output.csv'

/* separator between individual entries in csv file */
CSV_SEPARATOR = ';'

/* wraps all entries in the csv file */
TEXT_ENCLOSER = '"'

/* if the excel file contains irrelevant rows, you can completely skip them */
SKIP_ROWS = 0

/* the sheet name the script should transform */
EXCEL_TAB_NAME = 'mySheet'

/* 
 * only relevant for normal modus. In Transpose modus ignored.
 * Print headers in resulting csv file
 */
HEADER = true

/* how to format dates */
DATE_FORMAT = 'dd.MM.yyyy HH:mm:ss'

/* used for number formatting */
LOCALE = new Locale('en', 'US')
/* how to format numbers */
NUMBER_FORMAT = '###.0#'

/*
 * if the input excel file contains these chars, they will
 * be replaced by the chars in the map before transformation happens
 */
REPLACEMENT_MAP = [
   '\n' :     ' ',
   '\r' :     ' ',
   ';'  :     ' ',
   '"'  :     ' '
]

/* linebreak between csv lines */
LINE_BREAK = '\n';

/* if true, the results are transposed */
TRANSPOSE = true
/* line ids start with 0 if true, else with the original line id */
TRANSPOSE_SHIFT_LINE_IDS = true

/* headers in transpose mode */
TRANSPOSE_LINE_ID = 'LineId'
TRANSPOSE_COLUMN_ID = 'ColumnId'
TRANSPOSE_COLUMN_NAME = 'ColumnName'
TRANSPOSE_VALUE = 'Value'
// settings end ==========================================================================

input = new File(INPUT)
output = new File(OUTPUT)

numFormat = NumberFormat.getNumberInstance(LOCALE)
numFormat.applyPattern NUMBER_FORMAT

new XSSFWorkbook(new FileInputStream(input)).withCloseable { workbook ->
   def sheet = workbook.getSheet(EXCEL_TAB_NAME)

   output.newWriter().withCloseable { csv ->
      if (TRANSPOSE)
         convertTransposeModus sheet, csv 
      else
         convertNormalModus sheet, csv 

      csv.flush()
   }
   tout.println "CSV was successfully created here: $OUTPUT"
}

def convertText(text) {
   "$TEXT_ENCLOSER$text$TEXT_ENCLOSER"
}

def convertNumericCell(cell) {
   if (DateUtil.isCellDateFormatted(cell))
      convertText(cell.dateCellValue.format(DATE_FORMAT))
   else
      convertText(numFormat.format(cell.numericCellValue))
}

def convertTextCell(cell) {
   convertText(REPLACEMENT_MAP.inject(cell.stringCellValue) { s, k, v -> s.replace(k,v) })
}

def convertCell(cell, rowVals) {
   switch (cell.cellType) {
      case CellType.NUMERIC:
         rowVals << convertNumericCell(cell)
         break
      case CellType.STRING:
         rowVals << convertTextCell(cell)
         break

   }
}

def shiftedRowNumber(rowNum) {
   rowNum - SKIP_ROWS
}

def readHeaders(row, headers) {
   // read headers from input
   def cellIt = row.cellIterator()
   cellIt.each { cell -> convertCell cell, headers }
}

def convertNormalModus(sheet, csv) {
   def rowIt = sheet.iterator()
   def headers = []
   rowIt.each { row ->
      if (row.rowNum >= SKIP_ROWS) {
         if (0  == shiftedRowNumber(row.rowNum)) {
            readHeaders row, headers
         } else if(shiftedRowNumber(row.rowNum) > 0) {
            if (1 == shiftedRowNumber(row.rowNum) && HEADER) {
               csv << headers.join(CSV_SEPARATOR)
               csv << LINE_BREAK
            }
            def cellIt = row.cellIterator()
            def rowVals = []
            cellIt.each { cell -> convertCell cell, rowVals }
            csv << rowVals.join(CSV_SEPARATOR)
            csv << LINE_BREAK
         }
      }
   }
}

def convertTransposeModus(sheet, csv) {
   def newHeaders = []
   newHeaders << convertText(TRANSPOSE_LINE_ID)
      << convertText(TRANSPOSE_COLUMN_ID)
      << convertText(TRANSPOSE_COLUMN_NAME)
      << convertText(TRANSPOSE_VALUE)

   csv << newHeaders.join(CSV_SEPARATOR)
   csv << LINE_BREAK

   def rowIt = sheet.iterator()
   def headers = []
   rowIt.each { row ->
      if (0  == shiftedRowNumber(row.rowNum)) {
         readHeaders row, headers
      } else if(shiftedRowNumber(row.rowNum) > 0) {
         def rowIndex = TRANSPOSE_SHIFT_LINE_IDS? shiftedRowNumber(row.rowNum): row.rowNum

         def cellIt = row.cellIterator()

         cellIt.each { cell ->
            def rowVals = []
            def colIndex = cell.columnIndex

            rowVals << convertText(rowIndex)
               << convertText(colIndex+1)

            rowVals << headers[colIndex]

            convertCell cell, rowVals

            csv << rowVals.join(CSV_SEPARATOR)
            csv << LINE_BREAK
         }
      }
   }
}