package net.datenwerke.rs.samples.admin.teamspace

import net.datenwerke.rs.teamspace.service.teamspace.TeamSpaceService
import net.datenwerke.security.service.usermanager.UserManagerService

/**
 * removeUsersFromTeamspaces.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6064
 * Remove given users from given Teamspaces.
 */

///////////////////////////////////////////////////////////////

def userIds = [
   11431L, 
   115254L
]

def teamspaceIds = [
   8055L, 
   8239L
]

///////////////////////////////////////////////////////////////

def teamspaceService = GLOBALS.getInstance(TeamSpaceService)
def userManagerService = GLOBALS.getInstance(UserManagerService)

def users = userIds.collect{ userManagerService.getNodeById it }
assert users.size() == userIds.size() && !users.contains(null)

def teamspaces = teamspaceIds.collect{ teamspaceService.getTeamSpaceById it }
assert teamspaces.size() == teamspaces.size() && !teamspaces.contains(null)

teamspaces.each{ teamspace ->
   users.each{ user ->
      if (!teamspace.isOwner(user)) {
         def member = teamspaceService.getMemberFor teamspace, user
         if (member) {
            teamspace.members.remove member
            teamspaceService.remove member
         }
      }
   }
   teamspaceService.merge teamspace
}

'Successfully removed users from Teamspaces'