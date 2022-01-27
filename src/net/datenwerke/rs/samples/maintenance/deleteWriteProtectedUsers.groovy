package net.datenwerke.rs.samples.maintenance

import net.datenwerke.security.service.usermanager.UserManagerService

/**
 * deleteWriteProtectedUsers.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.0.0-6053
 * Deletes multiple users by their user ids even if the users are write protected.
 * Has to be called with -c flag to commit changes to the database.
 */

def userManagerService = GLOBALS.getInstance(UserManagerService)

// userIds of the users you want to get deleted as Long (L)
def userIds = [115254L]

userIds
   .collect { userManagerService.getNodeById(it) }
   .findAll { it != null }
   .each { user ->
      def msg = "User '$user.username' ($user.id) was deleted"
      user.writeProtection = false
      userManagerService.remove user
      tout.println msg
   }
