package net.datenwerke.rs.samples.admin

import org.apache.commons.lang3.time.DateUtils

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.core.service.reportmanager.entities.reports.Report
import net.datenwerke.rs.core.service.reportmanager.interfaces.ReportVariant
import net.datenwerke.security.service.security.SecurityService
import net.datenwerke.security.service.security.SecurityServiceSecuree
import net.datenwerke.rs.utils.jpa.EntityUtils
import net.datenwerke.security.service.security.rights.Delete
import net.datenwerke.security.service.security.rights.Execute
import net.datenwerke.security.service.security.rights.GrantAccess
import net.datenwerke.security.service.security.rights.Read
import net.datenwerke.security.service.security.rights.Write
import net.datenwerke.security.service.usermanager.entities.User

/**
 * reportRights.groovy
 * Version: 1.0.5
 * Type: Script datasource
 * Last tested with: ReportServer 4.1.0-6062
 * Shows all reports together with all users and their rights on the corresponding report.
 */

/* set same sizes for varchars as in reportserver */
def varcharSize = 128

def entityUtils = GLOBALS.getInstance(EntityUtils)

/* check registry: we cache the report for 10 minutes */
def cacheName = 'reportRights'
def lastCacheName = "_report_${cacheName}_last"
def dataCacheName = "_report_${cacheName}_data"

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'][lastCacheName]
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'][dataCacheName]

/* load services */
def securityService = GLOBALS.getInstance(SecurityService)

/* prepare result */
def tableDef = [
   'ID':                Long,
   'NAME':              String,
   'TYPE':              String,
   'USER_ID':           Long,
   'USER_FIRSTNAME':    String,
   'USER_LASTNAME':     String,
   'USERNAME':          String,
   'SUPER_USER':        Integer,
   'READ_RIGHT':        Integer,
   'WRITE_RIGHT':       Integer,
   'EXECUTE_RIGHT':     Integer,
   'DELETE_RIGHT':      Integer,
   'GRANT_RIGHTS':      Integer
]

/* set same sizes for varchars as in reportserver */
TableDefinition tableDefinition = new TableDefinition(
   columnNames:     tableDef*.key,
   columnTypes:     tableDef*.value,
   displaySizes:    tableDef.collect{it instanceof String? varcharSize: 0}
   )

def result = new RSTableModel(tableDefinition: tableDefinition)

/* loop over all reports */
GLOBALS.getEntitiesByType(Report)
   .findAll{ !(it instanceof ReportVariant) }
   .each{ report ->
      /* loop over all users and check their rights on the report */
      GLOBALS.getEntitiesByType(User)
     	.collect{ entityUtils.simpleHibernateUnproxy(it) }
     	.each{ user ->
           def superUser = user.superUser ? 1 : 0
           def r = securityService.checkRights(user, report, SecurityServiceSecuree, Read)  ? 1 : 0
           def w = securityService.checkRights(user, report, SecurityServiceSecuree, Write)  ? 1 : 0
           def x = securityService.checkRights(user, report, SecurityServiceSecuree, Execute)  ? 1 : 0
           def d = securityService.checkRights(user, report, SecurityServiceSecuree, Delete)  ? 1 : 0
           def g = securityService.checkRights(user, report, SecurityServiceSecuree, GrantAccess)  ? 1 : 0

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
GLOBALS.services['registry'][lastCacheName] = new Date()
GLOBALS.services['registry'][dataCacheName] = result

result