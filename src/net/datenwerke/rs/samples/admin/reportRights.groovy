package net.datenwerke.rs.samples.admin

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.core.service.reportmanager.entities.reports.Report
import net.datenwerke.rs.core.service.reportmanager.interfaces.ReportVariant
import net.datenwerke.security.service.security.SecurityService
import net.datenwerke.security.service.security.SecurityServiceSecuree
import net.datenwerke.security.service.security.rights.Delete
import net.datenwerke.security.service.security.rights.Execute
import net.datenwerke.security.service.security.rights.GrantAccess
import net.datenwerke.security.service.security.rights.Read
import net.datenwerke.security.service.security.rights.Write
import net.datenwerke.security.service.usermanager.entities.User

import org.apache.commons.lang3.time.DateUtils

/**
 * reportRights.groovy
 * Version: 1.0.0
 * Type: Script datasource
 * Last tested with: ReportServer 3.4.0-6035
 * Shows all reports together with all users and their rights on the corresponding report.
 */

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'].get('_report_user_permission_report_last')
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'].get('_report_user_permission_report_data')

/* load services */
def securityService = GLOBALS.getInstance(SecurityService.class)

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
         'SUPER_USER',
         'READ_RIGHT',
         'WRITE_RIGHT',
         'EXECUTE_RIGHT',
         'DELETE_RIGHT',
         'GRANT_RIGHTS'
      ],
      [
         Long.class,
         String.class,
         String.class,
         Long.class,
         String.class,
         String.class,
         String.class,
         Integer.class,
         Integer.class,
         Integer.class,
         Integer.class,
         Integer.class,
         Integer.class
      ]
      )

def result = new RSTableModel(tableDefinition)

/* loop over all reports */
GLOBALS.getEntitiesByType(Report.class).each{ report ->
   if(report instanceof ReportVariant)
      return

   /* loop over all users and check their rights on the report */
   GLOBALS.getEntitiesByType(User.class).each{ user ->
      def superUser = (null == user.isSuperUser() ? false : user.isSuperUser()) ? 1 : 0
      def r = securityService.checkRights(user, report, SecurityServiceSecuree.class, Read.class)  ? 1 : 0
      def w = securityService.checkRights(user, report, SecurityServiceSecuree.class, Write.class)  ? 1 : 0
      def x = securityService.checkRights(user, report, SecurityServiceSecuree.class, Execute.class)  ? 1 : 0
      def d = securityService.checkRights(user, report, SecurityServiceSecuree.class, Delete.class)  ? 1 : 0
      def g = securityService.checkRights(user, report, SecurityServiceSecuree.class, GrantAccess.class)  ? 1 : 0

      def resultLine = [
         report.id,
         report.name,
         report.class.simpleName,
         user.id,
         user.firstname,
         user.lastname,
         user.username,
         superUser,
         r,
         w,
         x,
         d,
         g
      ]

      /* add to result */
      result.addDataRow(new RSTableRow(tableDefinition, resultLine.toArray()))
   }
}

/* put the report into the cache */
GLOBALS.services['registry'].put('_report_user_permission_report_last', new Date())
GLOBALS.services['registry'].put('_report_user_permission_report_data', result)

return result