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
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6062
 * Reads an input csv file and writes it into a DB table.
 */

/**** USER SETTINGS ****/
CSV_FILENAME = '/path/to/your/input.csv'
DATE_FORMAT = 'dd.MM.yyyy'

DATASOURCE_ID = 58L // the datasource where the table is found
TABLE_NAME = 'myCsv' // the name of the table
SQL_COLS = [
   A:       Types.INTEGER,
   B:       Types.VARCHAR,
   C:       Types.VARCHAR,
   D:       Types.DATE,
   E:       Types.DOUBLE,
   F:       Types.BIT
]
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
      def header = mapReader.getHeader(true)

      dbPoolService.getConnection(datasource.connectionConfig).get().withCloseable { conn ->
         assert conn

         def insertStmt = "INSERT INTO $TABLE_NAME (${SQL_COLS.keySet().join(',')}) VALUES (${SQL_COLS.collect{'?'}.join(',')})"

         def sql = new Sql(conn)
         sql.withTransaction {
            sql.withBatch(BATCH_SIZE, insertStmt) { stmt ->
               def csvRow
               while( (csvRow = mapReader.read(header, PROCESSORS as CellProcessor[])) != null )
                  insertRow stmt, csvRow
            }
         }
      }
   }
   tout.println 'CSV values successfully written into DB'
}

def insertRow(stmt, csvRow) {
   def typed = csvRow.collect { k, v ->
      [
         getType: { -> SQL_COLS[k] },
         getValue: { -> v }
      ] as InParameter
   }
   stmt.addBatch typed
}
