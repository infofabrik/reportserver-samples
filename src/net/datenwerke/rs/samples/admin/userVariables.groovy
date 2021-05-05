package net.datenwerke.rs.samples.admin

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.core.service.reportmanager.entities.reports.Report
import net.datenwerke.rs.core.service.reportmanager.interfaces.ReportVariant
import net.datenwerke.rs.uservariables.service.uservariables.UserVariableService
import net.datenwerke.security.service.usermanager.entities.User

import org.apache.commons.lang3.time.DateUtils

/**
 * userVariables.groovy
 * Version: 1.0.0
 * Type: Script datasource
 * Last tested with: ReportServer 3.4.0-6035
 * Shows all user variables with their corresponding values for each user.
 */

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'].get('_report_user_variables_report_last')
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'].get('_report_user_variables_report_data')


/* load services */
def userVarService = GLOBALS.getInstance(UserVariableService.class)

/* load user variables */
def userVarDefs = userVarService.definedVariableDefinitions
def userVars = [:]
GLOBALS.getEntitiesByType(User.class).each{ user ->
   userVars[user] = [:]
   userVarDefs.each { userVarDef ->
      userVars[user].putAt(userVarDef, userVarService.getVariableInstanceForUser(user, userVarDef))
   }
}

/* prepare result */
TableDefinition tableDefinition = new TableDefinition(
      [
         'ID',
         'NAME',
         'TYPE',
         'USER_ID',
         'USER_FIRSTNAME',
         'USER_LASTNAME',
         'USERNAME',
         'USER_VAR_TYPE',
         'USER_VAR_VALUE',
      ],
      [
         Long.class,
         String.class,
         String.class,
         Long.class,
         String.class,
         String.class,
         String.class,
         String.class,
         String.class,
      ]
      )

def result = new RSTableModel(tableDefinition)

/* loop over all reports */
GLOBALS.getEntitiesByType(Report.class).each{ report ->
   if(report instanceof ReportVariant)
      return

   /* loop over all users and check their rights on the report */
   GLOBALS.getEntitiesByType(User.class).each{ user ->
      userVarDefs.each{ userVarDef ->
         def userVarInst = userVars[user][userVarDef]

         def resultLine = [
            report.id,
            report.name,
            report.class.simpleName,
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
}

/* put the report into the cache */
GLOBALS.services['registry'].put('_report_user_variables_report_last', new Date())
GLOBALS.services['registry'].put('_report_user_variables_report_data', result)

return result