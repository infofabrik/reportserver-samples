package net.datenwerke.rs.samples.admin

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.uservariables.service.uservariables.UserVariableService
import net.datenwerke.security.service.usermanager.entities.User

import org.apache.commons.lang3.time.DateUtils

/**
 * userVariables.groovy
 * Version: 1.0.1
 * Type: Script datasource
 * Last tested with: ReportServer 4.0.0-6053
 * Shows all user variables with their corresponding values for each user.
 */

/* check registry: we cache the report for 10 minutes */
def cacheName = 'userVariables'
def lastCacheName = "_report_${cacheName}_last"
def dataCacheName = "_report_${cacheName}_data"

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'].get(lastCacheName)
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'].get(dataCacheName)


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
TableDefinition tableDefinition = new TableDefinition(
      [
         'USER_ID',
         'USER_FIRSTNAME',
         'USER_LASTNAME',
         'USERNAME',
         'USER_VAR_TYPE',
         'USER_VAR_VALUE',
      ],
      [
         Long,
         String,
         String,
         String,
         String,
         String,
      ]
      )

def result = new RSTableModel(tableDefinition)


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
GLOBALS.services['registry'].put(lastCacheName, new Date())
GLOBALS.services['registry'].put(dataCacheName, result)

return result