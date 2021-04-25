package net.datenwerke.rs.samples.maintenance

import net.datenwerke.security.service.usermanager.UserManagerService

/**
* deleteWriteProtectedUsers.groovy
* Type: Normal Script
* Last tested with: ReportServer 3.5.0-6037
* Deletes multiple users by their user ids even if the users are write protected.
*/

def userManagerService = GLOBALS.getInstance(UserManagerService.class)

//Please put the userIds of the users you want to get deleted into the list
//passing the ids as long (L)
def userIds = [123L, 456L]

userIds.each { userId -> 
    def user = userManagerService.getNodeById(userId)
  
    if(! user){
        tout.println "User with Id: $userId can not be found!"
    } else {
        user.writeProtection = false
        userManagerService.remove user
        tout.println "User with Id: $userId has been deleted!"
    }
}
