package net.datenwerke.rs.samples.admin

import org.apache.commons.lang3.time.DateUtils

import net.datenwerke.gf.service.history.HistoryService
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.security.service.usermanager.entities.Group
import net.datenwerke.security.service.usermanager.entities.OrganisationalUnit
import net.datenwerke.security.service.usermanager.entities.User

/**
 * usersOusAndGroups.groovy
 * Version: 1.0.5
 * Type: Script datasource
 * Last tested with: ReportServer 4.0.0-6053
 * Shows all users, OUs and groups and their corresponding OU and group memberships.
 */

def cacheName = 'ousAndGroups'
def lastCacheName = "_report_${cacheName}_last"
def dataCacheName = "_report_${cacheName}_data"

/* set same sizes for varchars as in reportserver */
def varcharSize = 128

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'][lastCacheName]
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'][dataCacheName]

userManagerService = GLOBALS.getInstance(UserManagerService)
historyService = GLOBALS.getInstance(HistoryService)

/* prepare result */
def tableDef = [
   'USER_ID':           Long,       // USER_ information for user nodes
   'USER_FIRSTNAME':    String,
   'USER_LASTNAME':     String,
   'USERNAME':          String,
   'GROUP_ID':          Long,       // GROUP_ information for group nodes
   'GROUP_NAME':        String,
   'OU_ID':             Long,       // OU_ information for OU nodes
   'OU_NAME':           String,
   'CHILD_OF_OU':       String,     // the node is inside this OU
   'CHILD_OF_OU_ID':    Long,
   'MEMBER_OF_GROUPS':  String      // the node is a member of these groups
]

/* set same sizes for varchars as in reportserver */
TableDefinition tableDefinition = new TableDefinition(
   columnNames:     tableDef*.key,
   columnTypes:     tableDef*.value,
   displaySizes:    tableDef.collect{it instanceof String? varcharSize: 0}
   )

def result = new RSTableModel(tableDefinition: tableDefinition)

addUsers result, tableDefinition
addGroups result, tableDefinition
addOus result, tableDefinition

def addUsers(result, tableDefinition) {
   GLOBALS.getEntitiesByType(User).each{ user ->
         def resultLine = [
            user.id,
            user.firstname,
            user.lastname,
            user.username,
            null,
            null,
            null,
            null,
            collectOu(user),
            user.parent.id,
            collectGroups(userManagerService.getReferencedGroups(user))
         ]
      
         /* add to result */
         result.addDataRow(new RSTableRow(tableDefinition, resultLine.toArray()))
      }
}

def addGroups(result, tableDefinition) {
   GLOBALS.getEntitiesByType(Group).each { group ->
      def resultLine = [
         null,
         null,
         null,
         null,
         group.id,
         group.name,
         null,
         null,
         collectOu(group),
         group.parent.id,
         collectGroups(userManagerService.getGroupsWithMember(group))
      ]
      
      /* add to result */
      result.addDataRow(new RSTableRow(tableDefinition, resultLine.toArray()))
   }
}

def addOus(result, tableDefinition) {
   GLOBALS.getEntitiesByType(OrganisationalUnit).each { ou ->
      def resultLine = [
         null,
         null,
         null,
         null,
         null,
         null,
         ou.id,
         ou.name,
         ou.parent ? collectOu(ou): null,
         ou.parent ? ou.parent.id: null,
         collectGroups(userManagerService.getGroupsWithMember(ou))
      ]
      
      /* add to result */
      result.addDataRow(new RSTableRow(tableDefinition, resultLine.toArray()))
   }
}

def collectGroups(groups) {
   groups.sort { group -> group.name }
         .collect{ group -> "$group.name ($group.id)" }
         .join(', ') ?: null
}

def collectOu(node) {
   historyService.buildLinksFor(node.parent)
      .collect{ link -> "$link.objectCaption" }[0] as String
}

/* put the report into the cache */
GLOBALS.services['registry'][lastCacheName] = new Date()
GLOBALS.services['registry'][dataCacheName] = result

result