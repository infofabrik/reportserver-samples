package net.datenwerke.rs.samples.admin

import org.apache.commons.lang3.time.DateUtils

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.teamspace.service.teamspace.entities.TeamSpace

/**
 * usersInTeamspace.groovy
 * Version: 1.0.0
 * Type: Script datasource
 * Last tested with: ReportServer 4.0.0-6053
 * Shows all users in all teamspaces and their corresponding roles.
 */

/* set same sizes for varchars as in reportserver */
def varcharSize = 128

/* check registry: we cache the report for 10 minutes */
def cacheName = 'usersInTeamspace'
def lastCacheName = "_report_${cacheName}_last"
def dataCacheName = "_report_${cacheName}_data"

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'][lastCacheName]
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'][dataCacheName]

/* prepare result */
def tableDef = [
   'TEAMSPACE_ID':              Long,
   'TEAMSPACE_NAME':            String,
   'TEAMSPACE_DESCRIPTION':     String,
   'USER_ID':                   Long,
   'USER_FIRSTNAME':            String,
   'USER_LASTNAME':             String,
   'USERNAME':                  String,
   'ROLE':                      String
]

/* set same sizes for varchars as in reportserver */
TableDefinition tableDefinition = new TableDefinition(
   columnNames:     tableDef*.key,
   columnTypes:     tableDef*.value,
   displaySizes:    tableDef.collect{it instanceof String? varcharSize: 0}
   )

def result = new RSTableModel(tableDefinition: tableDefinition)

/* loop over all teamspaces */
GLOBALS.getEntitiesByType(TeamSpace).each { teamspace -> 
   teamspace.members.each { member -> 
      def resultLine = [
         teamspace.id,
         teamspace.name,
         teamspace.description,
         member.folk.id,
         member.folk.firstname,
         member.folk.lastname,
         member.folk.username,
         member.role as String
      ]

      /* add to result */
      result.addDataRow(new RSTableRow(tableDefinition, resultLine.toArray()))
   }
   
   /* add teamspace owners */
   def resultLine = [
      teamspace.id,
      teamspace.name,
      teamspace.description,
      teamspace.owner.id,
      teamspace.owner.firstname,
      teamspace.owner.lastname,
      teamspace.owner.username,
      'OWNER'
   ]
   
   /* add to result */
   result.addDataRow(new RSTableRow(tableDefinition, resultLine.toArray()))
}

/* put the report into the cache */
GLOBALS.services['registry'][lastCacheName] = new Date()
GLOBALS.services['registry'][dataCacheName] = result

result