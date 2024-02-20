package net.datenwerke.rs.samples.tools.sql
/**
 * callProcedure.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.7.0 on postgres database
 * 
 * Script to call a remote procedure. 
 * 
 * Example procedure:
 * CREATE OR REPLACE PROCEDURE Test_proc 
 *(
 *  input1 IN varchar(10), 
 *  input2 IN INT, 
 *  output1 OUT INT, 
 *  output2 OUT varchar(10)
 *) 
 *LANGUAGE plpgsql 
 *AS  $$ 
 *DECLARE 
 *BEGIN 
 *  
 *  output2 := input1; 
 *  output1 := input2; 
 *  
 *END  $$
 */

import java.sql.*;

import net.datenwerke.dbpool.DbPoolService
import net.datenwerke.rs.base.service.datasources.definitions.DatabaseDatasource
import net.datenwerke.rs.terminal.service.terminal.TerminalSession
import net.datenwerke.rs.terminal.service.terminal.objresolver.ObjectResolverDeamon

def datasourcePath = "/datasources/testenv"
def procedure = "{CALL Test_proc(?, ?, ?, ?)}"
def arg1 = args.size() < 1 ? "param1" : args[0]
def arg2 = args.size() < 2 ?  123     : args[1] as int

DbPoolService dbPoolService = GLOBALS.getInstance(DbPoolService)
TerminalSession session = GLOBALS.getInstance(TerminalSession)
ObjectResolverDeamon resolver =  session.objectResolver

DatabaseDatasource sourceDatasource = resolver.getObjects(datasourcePath)[0]
sourceDatasource.setJdbcProperties("escapeSyntaxCallMode=call") // depending on your driver this could be omitted
dbPoolService.getConnection(sourceDatasource.connectionConfig).get().withCloseable { connection ->
   connection.prepareCall(procedure).withCloseable {stmt ->
      stmt.setString(1, arg1) // Input1-Parameter
      stmt.setInt(2, arg2) // Input2-Parameter
      stmt.registerOutParameter(3, Types.INTEGER) // Output1-Parameter
      stmt.registerOutParameter(4, Types.VARCHAR) // Output2-Parameter
      stmt.execute()

      int output1 = stmt.getInt(3);
      String output2 = stmt.getString(4);
      return "Output1: $output1 Output2: $output2"
   }
}
