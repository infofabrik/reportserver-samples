import java.util.logging.Level
import java.util.logging.Logger
import groovy.json.JsonSlurper
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import groovy.sql.Sql
import groovy.sql.InParameter
import net.datenwerke.dbpool.DbPoolService
import net.datenwerke.rs.terminal.service.terminal.TerminalSession

/**
 * insertFormData.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6061
 * Allows you to save uploaded form data and insert it into a given table.
 */

// ==================================================================================
datasourceId = 58L
table = 'myTable'
// ==================================================================================

def session = GLOBALS.getInstance(TerminalSession)
def objectResolver = session.objectResolver
def dbPoolService = GLOBALS.getInstance(DbPoolService)

def datasource = objectResolver.getObjects("id:DatabaseDatasource:$datasourceId")

def logger = Logger.getLogger(getClass().name)

def slurper = new JsonSlurper()
def m = slurper.parseText(URLDecoder.decode(args[0], StandardCharsets.UTF_8.name()))

def keys = m.keySet()
dbPoolService.getConnection(datasource.connectionConfig).get().withCloseable { conn ->
    def sql = new Sql(conn)
    def insertStmt = """INSERT INTO $table (name,age,email,gender,country,comments) VALUES (
    ${formatStringValue('name',m)}, ${m.age?:0}, ${formatStringValue('email',m)}, ${formatStringValue('gender',m)}, 
    ${formatStringValue('country',m)}, ${formatStringValue('comments',m)})
    """
    logger.log(Level.INFO, "INSERT: ${insertStmt}")
    sql.execute insertStmt.toString()
}

def formatStringValue(key, m) {
    def val = m."$key"?:'NULL'
    if (val != 'NULL')
        val = "'$val'"
    val
}