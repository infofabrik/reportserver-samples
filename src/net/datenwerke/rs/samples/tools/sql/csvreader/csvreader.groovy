import net.datenwerke.rs.terminal.service.terminal.TerminalSession
import net.datenwerke.dbpool.DbPoolService
import org.supercsv.cellprocessor.constraint.NotNull
import org.supercsv.cellprocessor.constraint.UniqueHashCode
import org.supercsv.cellprocessor.ift.CellProcessor
import org.supercsv.cellprocessor.ParseDate
import org.supercsv.io.CsvMapReader
import org.supercsv.prefs.CsvPreference
import org.supercsv.cellprocessor.ParseInt
import org.supercsv.cellprocessor.ParseBool
import org.supercsv.cellprocessor.ParseBigDecimal
import org.supercsv.cellprocessor.Optional
import groovy.sql.Sql
import groovy.sql.InParameter
import java.sql.Types

/**
 * csvreader.groovy
 * Version: 1.0.2
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6062
 * Reads an input csv file and writes it into a DB table.
 */

/**** USER SETTINGS ****/
CSV_FILENAME = '/path/to/your/input.csv'
CSV_FILE_HAS_HEADER = true // true or false
DATE_FORMAT = 'dd.MM.yyyy'

DATASOURCE_ID = 58L // the datasource where the table is found
TABLE_NAME = 'your_table' // the name of the table where the data should be inserted to
SQL_COLS = [
   A:       Types.INTEGER,
   B:       Types.VARCHAR,
   C:       Types.VARCHAR,
   D:       Types.DATE,
   E:       Types.DOUBLE,
   F:       Types.BIT
]
/** If the order of entries of SQL_COLS corresponds to the order of csv columns 
 *  use it as it is. 
 *  You can define your own mapping by using the keys of SQL_COLS in a String[].
 *  CSV_TO_SQL_COL_MAPPER[0] corresponds to the first column CSV_TO_SQL_COL_MAPPER[1] 
 *  to the 2nd and so on. 
 *  CSV_TO_SQL_COL_MAPPER.length has to be equal to the number of columns in the csv.
 *  You can skip columns by using null instead of a key from SQL_COLS
 *  E.g. CSV_TO_SQL_COL_MAPPER = ['B, 'A', 'C', 'D', null, 'E', 'F']
 */
CSV_TO_SQL_COL_MAPPER = SQL_COLS.keySet() as String[]
BATCH_SIZE = 100

PROCESSORS = [
   new UniqueHashCode(new ParseInt()), // A: (must be a unique integer)
   new NotNull(), // B: required string
   new Optional(), // C: optional string
   new ParseDate(DATE_FORMAT), // D: date
   new ParseBigDecimal(), // E: big decimal
   new ParseBool() // F: boolean
]

/***********************/

readCsv()

def readCsv() {
   def session = GLOBALS.getInstance(TerminalSession)
   def objectResolver = session.objectResolver
   def dbPoolService = GLOBALS.getInstance(DbPoolService)

   def datasource = objectResolver.getObjects("id:DatabaseDatasource:$DATASOURCE_ID")

   assert datasource

   new CsvMapReader(new FileReader(CSV_FILENAME), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE).withCloseable{ mapReader ->
      if(CSV_FILE_HAS_HEADER) mapReader.getHeader(true)

      dbPoolService.getConnection(datasource.connectionConfig).get().withCloseable { conn ->
         assert conn

         def insertStmt = "INSERT INTO $TABLE_NAME (${SQL_COLS.keySet().join(',')}) VALUES (${SQL_COLS.collect{'?'}.join(',')})"

         def sql = new Sql(conn)
         sql.withTransaction {
            sql.withBatch(BATCH_SIZE, insertStmt) { stmt ->
               def csvRow
               while( (csvRow = mapReader.read(CSV_TO_SQL_COL_MAPPER, PROCESSORS as CellProcessor[])) != null )
                  insertRow stmt, csvRow
            }
         }
      }
   }
   tout.println 'CSV values successfully written into DB'
}

def insertRow(stmt, csvRow) {
   def typed = SQL_COLS.keySet().collect { k ->
      [
         getType: { -> SQL_COLS[k] },
         getValue: { -> csvRow[k] }
      ] as InParameter
   }
   stmt.addBatch typed
}
