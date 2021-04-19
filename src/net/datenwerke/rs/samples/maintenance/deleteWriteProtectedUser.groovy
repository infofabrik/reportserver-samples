import net.datenwerke.security.service.usermanager.UserManagerService

/**
 * deleteWriteProtectedUser.groovy
 * Type: Normal Script
 * Last tested with: ReportServer 3.4.0-6035
 * Deletes a user by user id even if the user is write protected.
 */

def userManagerService = GLOBALS.getInstance(UserManagerService.class)

// The userId of user to be deleted
def userId = 29313

def user = userManagerService.getNodeById(userId)

if(null == user){
   tout.println "User with Id: $userId can not be found!"
} else {
   user.writeProtection = false
   userManagerService.remove(user)
   tout.println "User with Id: $userId has been deleted!"
}
