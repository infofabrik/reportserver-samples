package net.datenwerke.rs.samples.admin.teamspace

import net.datenwerke.rs.teamspace.service.teamspace.TeamSpaceService
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.rs.teamspace.service.teamspace.entities.TeamSpaceRole
import net.datenwerke.rs.teamspace.service.teamspace.entities.TeamSpaceMember

/**
 * addUsersToTeamspaces.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.1.0-6064
 * Adds given users to given Teamspaces with a given role.
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

// ADMIN, MANAGER, USER, GUEST
def role = TeamSpaceRole.MANAGER

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
            member.role = role
         } else {
            member = new TeamSpaceMember()
            member.role = role
            member.folk = user

            teamspaceService.persist member
            teamspace.addMember member
         }
      }
   }
   teamspaceService.merge teamspace
}

'Successfully added users to Teamspaces'