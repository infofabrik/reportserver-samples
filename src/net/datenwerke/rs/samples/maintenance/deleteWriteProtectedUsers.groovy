package net.datenwerke.rs.samples.maintenance

import net.datenwerke.security.service.usermanager.UserManagerService

/**
* deleteWriteProtectedUsers.groovy
* Type: Normal Script
* Last tested with: ReportServer 3.4.0-6035
* Deletes multiple users by their user ids even if the users are write protected.
*/

def userManagerService = GLOBALS.getInstance(UserManagerService.class)

//Please put the userIds of the users you want to get deleted into the list
//converting the ids to long (l)
def userIds = [123l, 456l]

userIds.each { userId -> 
    def user = userManagerService.getNodeById(userId)
  
    if(null == user){
        tout.println "User with Id: $userId can not be found!"
    } else {
        user.writeProtection = false
        userManagerService.remove(user)
        tout.println "User with Id: $userId has been deleted!"
    }
}
