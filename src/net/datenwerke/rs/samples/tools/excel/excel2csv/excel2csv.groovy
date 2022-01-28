package net.datenwerke.rs.samples.tools.excel.excel2csv

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import net.datenwerke.rs.terminal.service.terminal.TerminalSession
import net.datenwerke.dbpool.DbPoolService
import java.text.NumberFormat
import groovy.sql.Sql
import java.nio.file.Files
import java.nio.file.Paths

/**
 * excel2csv.groovy
 * Version: 2.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.0.0-6053
 * Demonstrates how to read Excel files with help of Apache POI library, 
 * transforms the data and write the results into CSV files or into a given DB table (if transposing data).
 */

// settings start ==========================================================================
INPUT_FILES_DIR = '/path/to/your/inputs'
OUTPUT_FILES_DIR = '/path/to/your/outputs'
/* we move input files after processing into this directory */
ARCHIVE_FILES_DIR = '/path/to/your/archive'

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

/* headers/table names in transpose mode */
TRANSPOSE_FIELDS_DEF = [
   TIMESTAMP: 'timestamp',
   FILENAME: 'excel_file_name',
   LINE_ID: 'excel_line_id',
   COLUMN_ID: 'excel_col_id',
   COLUMN_NAME: 'excel_column_name',
   VALUE: 'excel_value'
   ]

/* table settings for transposed mode */
/* if true, saves the results into a db table instead of a file */
TRANSPOSE_RESULTS_TO_DB = true

TRANSPOSE_DATASOURCE_ID = 58L
TRANSPOSE_TABLE = 'table_name'


// the size of the batch for the batch-insert
TRANSPOSE_BATCH_SIZE = 100

// settings end ==========================================================================

// matches only Excel files
def pattern = ~/(?ix)        # case insensitive(i), ignore space(x)
^                           # start of line
(\w*\s*)*                   # any number of word characters followed by any number of spaces
\.xlsx$                     # ending .xlsx
/

numFormat = NumberFormat.getNumberInstance(LOCALE)
numFormat.applyPattern NUMBER_FORMAT

def inputDir = new File(INPUT_FILES_DIR)
inputDir.eachFileMatch(pattern) { input -> 
   try {
      new XSSFWorkbook(new FileInputStream(input)).withCloseable { workbook ->
         def sheet = workbook.getSheet(EXCEL_TAB_NAME)
      
         if (TRANSPOSE && TRANSPOSE_RESULTS_TO_DB)
            insertIntoTable sheet, input
         else 
            createCsv sheet, input
      }
   } finally {
      Files.move(input.toPath(), Paths.get("${ARCHIVE_FILES_DIR}/$input.name"))
   }
}

def insertIntoTable(sheet, input) {
   tout.println "Inserting $input.name into DB table..."
   def session = GLOBALS.getInstance(TerminalSession)
   def objectResolver = session.objectResolver
   def dbPoolService = GLOBALS.getInstance(DbPoolService)
   
   def datasource = objectResolver.getObjects("id:DatabaseDatasource:$TRANSPOSE_DATASOURCE_ID")
   
   assert datasource
   
   dbPoolService.getConnection(datasource.connectionConfig).get().withCloseable { conn ->
      assert conn
      
      def sql = new Sql(conn)

      def insertStmt = "INSERT INTO $TRANSPOSE_TABLE (${TRANSPOSE_FIELDS_DEF.values().join(',')}) "
         << "values (${('?'*TRANSPOSE_FIELDS_DEF.size() as List).join(',')})"
      
      sql.withTransaction {
         sql.withBatch(TRANSPOSE_BATCH_SIZE, insertStmt as String) { stmt ->
            def addToTableClosure = { stmt.addBatch it }
            convertTransposeModus sheet, input, {}, addToTableClosure
         }
      }
   }
}

def createCsv(sheet, input) {
   def output = new File("${OUTPUT_FILES_DIR}/${input.name}.csv")
   output.newWriter().withCloseable { csv ->
      if (TRANSPOSE) {
         def addToCsvClosure = {addToCsv it, csv}
         convertTransposeModus sheet, input, addToCsvClosure, addToCsvClosure
      } else
         convertNormalModus sheet, csv

      csv.flush()
   }
   tout.println "CSVs was successfully created here: $output.name"
}

def addToCsv(vals, csv) {
   csv << vals.join(CSV_SEPARATOR)
   csv << LINE_BREAK
}

def convertText(text) {
   if (TRANSPOSE_RESULTS_TO_DB) 
      text
   else 
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
   assert !TRANSPOSE_RESULTS_TO_DB, 'Saving into table only supported in transposed modus'
   
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

def convertTransposeModus(sheet, input, Closure addHeaders, Closure addBody) {
   def newHeaders = []
   newHeaders << convertText(TRANSPOSE_FIELDS_DEF.TIMESTAMP)
      << convertText(TRANSPOSE_FIELDS_DEF.FILENAME)
      << convertText(TRANSPOSE_FIELDS_DEF.LINE_ID)
      << convertText(TRANSPOSE_FIELDS_DEF.COLUMN_ID)
      << convertText(TRANSPOSE_FIELDS_DEF.COLUMN_NAME)
      << convertText(TRANSPOSE_FIELDS_DEF.VALUE)

   addHeaders newHeaders // do something with the headers

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

            rowVals << (TRANSPOSE_RESULTS_TO_DB? new Date(): convertText((new Date()).format(DATE_FORMAT))) // TIMESTAMP
               << convertText(input.name) // FILENAME
               << convertText(rowIndex) // LINE_ID
               << convertText(colIndex+1) // COLUMN_ID

            rowVals << headers[colIndex] // COLUMN_NAME

            convertCell cell, rowVals // VALUE

            addBody rowVals // do something with the row values
         }
      }
   }
}