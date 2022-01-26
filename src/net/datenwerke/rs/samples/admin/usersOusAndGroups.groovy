package net.datenwerke.rs.samples.admin

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.security.service.usermanager.entities.User
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.gf.service.history.HistoryService
import net.datenwerke.security.service.usermanager.entities.Group
import net.datenwerke.security.service.usermanager.entities.OrganisationalUnit

/**
 * usersOusAndGroups.groovy
 * Version: 1.0.1
 * Type: Script datasource
 * Last tested with: ReportServer 4.0.0-6053
 * Shows all users, OUs and groups and their corresponding OU and group memberships.
 */

def cacheName = 'ousAndGroups'
def lastCacheName = "_report_${cacheName}_last"
def dataCacheName = "_report_${cacheName}_data"

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'].get(lastCacheName)
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'].get(dataCacheName)

userManagerService = GLOBALS.getInstance(UserManagerService)
historyService = GLOBALS.getInstance(HistoryService)

/* prepare result */
TableDefinition tableDefinition = new TableDefinition(
      [
         'USER_ID', // USER_ information for user nodes
         'USER_FIRSTNAME',
         'USER_LASTNAME',
         'USERNAME',
         'GROUP_ID', // GROUP_ information for group nodes
         'GROUP_NAME',
         'OU_ID', // OU_ information for OU nodes
         'OU_NAME',
         'CHILD_OF_OU', // the node is inside this OU
         'CHILD_OF_OU_ID',
         'MEMBER_OF_GROUPS' // the node is a member of these groups
      ],
      [
         Long,
         String,
         String,
         String,
         Long,
         String,
         Long,
         String,
         String,
         Long,
         String
      ]
      )

def result = new RSTableModel(tableDefinition)

/* loop over all users */
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
GLOBALS.services['registry'].put(lastCacheName, new Date())
GLOBALS.services['registry'].put(dataCacheName, result)

return result