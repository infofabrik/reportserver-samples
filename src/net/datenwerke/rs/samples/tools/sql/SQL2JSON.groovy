package net.datenwerke.rs.samples.tools.sql

import net.datenwerke.rs.terminal.service.terminal.TerminalSession
import net.datenwerke.dbpool.DbPoolService
import groovy.json.JsonOutput
import groovy.sql.Sql

/**
 * sql2json.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6064
 *
 * Script to query a database and convert the resulting rows to a JSON
 *
 * Example:
 *                          Absolute path to the datasource     SQL-Query
 * exec sql2json.groovy     '/datasources/my datasource'        'SELECT * FROM MyTable'
 */

if(args.size() != 2){
   throw new Exception("Needs 2 arguments but ${args.size()} supplied!")
}else{
   return safeExecuteSQL(args[0], args[1])
}

def safeExecuteSQL(datasourcePath,query){
   def session = GLOBALS.getInstance(TerminalSession)
   def objectResolver = session.objectResolver
   def dbPoolService = GLOBALS.getInstance(DbPoolService)
   
   assert datasourcePath.startsWith('/datasources/')
   def sourceDatasource = objectResolver.getObjects(datasourcePath)

   assert sourceDatasource
   
   dbPoolService.getConnection(sourceDatasource.connectionConfig).get().withCloseable { connection ->
      // open connection
      // and return the resulting rows from the executed query as JSON
      return JsonOutput.toJson([RESULT:new Sql(connection).rows(query)])
   }
}
