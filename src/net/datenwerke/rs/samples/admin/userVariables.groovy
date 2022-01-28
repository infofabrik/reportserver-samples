package net.datenwerke.rs.samples.admin

import org.apache.commons.lang3.time.DateUtils

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.uservariables.service.uservariables.UserVariableService
import net.datenwerke.security.service.usermanager.entities.User

/**
 * userVariables.groovy
 * Version: 1.0.4
 * Type: Script datasource
 * Last tested with: ReportServer 4.0.0-6053
 * Shows all user variables with their corresponding values for each user.
 */

/* check registry: we cache the report for 10 minutes */
def cacheName = 'userVariables'
def lastCacheName = "_report_${cacheName}_last"
def dataCacheName = "_report_${cacheName}_data"

/* set same sizes for varchars as in reportserver */
def varcharSize = 128

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'][lastCacheName]
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'][dataCacheName]

/* load services */
def userVarService = GLOBALS.getInstance(UserVariableService)

/* load user variables */
def userVarDefs = userVarService.definedVariableDefinitions
def userVars = [:]
GLOBALS.getEntitiesByType(User).each{ user ->
   userVars[user] = userVarDefs.collectEntries { userVarDef -> 
      [(userVarDef): userVarService.getVariableInstanceForUser(user, userVarDef)]
   }
}

/* prepare result */
def tableDef = [
   'USER_ID':           Long,
   'USER_FIRSTNAME':    String,
   'USER_LASTNAME':     String,
   'USERNAME':          String,
   'USER_VAR_TYPE':     String,
   'USER_VAR_VALUE':    String
]

/* set same sizes for varchars as in reportserver */
TableDefinition tableDefinition = new TableDefinition(
   columnNames:     tableDef*.key,
   columnTypes:     tableDef*.value,
   displaySizes:    tableDef.collect{it instanceof String? varcharSize: 0}
   )

def result = new RSTableModel(tableDefinition: tableDefinition)

/* loop over all users and check their rights on the report */
GLOBALS.getEntitiesByType(User).each{ user ->
   userVarDefs.each{ userVarDef ->
      def userVarInst = userVars[user][userVarDef]

      def resultLine = [
         user.id,
         user.firstname,
         user.lastname,
         user.username,
         userVarDef.name,
         userVarInst?.value?.toString(),
      ]

      /* add to result */
      result.addDataRow(new RSTableRow(tableDefinition, resultLine.toArray()))
   }
}

/* put the report into the cache */
GLOBALS.services['registry'][lastCacheName] = new Date()
GLOBALS.services['registry'][dataCacheName] = result

result