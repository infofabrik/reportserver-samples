package net.datenwerke.rs.samples.admin.teamspace

import net.datenwerke.rs.teamspace.service.teamspace.TeamSpaceService
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.rs.tsreportarea.service.tsreportarea.TsDiskService
import net.datenwerke.rs.core.service.reportmanager.ReportService
import net.datenwerke.security.service.usermanager.UserPropertiesService
import net.datenwerke.security.service.usermanager.entities.User
import net.datenwerke.security.service.usermanager.entities.UserProperty

/**
 * createUserTeamspaces.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6064
 * Creates TeamSpaces for given users, sets these as default TeamSpaces for these users,
 * and imports given reports into the created TeamSpaces.
 */

///////////////////////////////////////////////////////////////

def userIds = [
   6L, 
   11431L
]

def teamspacePrefix = "teamspace_"

def reportIds = [
   374310L
]

def copyReport = false
def importReportAsReference = true

///////////////////////////////////////////////////////////////

def teamspaceService = GLOBALS.getInstance(TeamSpaceService)
def userManagerService = GLOBALS.getInstance(UserManagerService)
def tsDiskService = GLOBALS.getInstance(TsDiskService)
def reportService = GLOBALS.getInstance(ReportService)
def userPropertiesService = GLOBALS.getInstance(UserPropertiesService)

def users = userIds.collect{ userManagerService.getNodeById it }
assert users.size() == userIds.size() && !users.contains(null)

def reports = reportIds.collect{ reportService.getReportById it }
assert reports.size() == reportIds.size() && !reports.contains(null)

users.each{ user ->
   // create teamspace
   def teamspace = teamspaceService.createTeamSpace user
   teamspace.name = "$teamspacePrefix$user.username"
   teamspace.description = 'Automatically created by createUserTeamspaces.groovy'
   teamspaceService.merge teamspace

   // set teamspace as default
   def property = userPropertiesService.getProperty user, TeamSpaceService.USER_PROPERTY_PRIMARY_TEAMSPACE
   if (!property) {
      property = new UserProperty(TeamSpaceService.USER_PROPERTY_PRIMARY_TEAMSPACE, teamspace.id as String)
      userPropertiesService.setProperty user, property
   } else {
      property.value = teamspace.id
   }
   userManagerService.merge user

   def root = tsDiskService.getRoot teamspace
   reports.each{ report ->
      def reference = null
      if (!report.name && !report.description)
         tsDiskService.importReport report, root, copyReport, importReportAsReference
      else
         tsDiskService.importReport report, root, copyReport, report.name, report.description, importReportAsReference

      tsDiskService.merge root
   }
}

'Successfully created TeamSpaces'