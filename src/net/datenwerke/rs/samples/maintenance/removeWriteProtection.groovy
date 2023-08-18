import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.security.service.usermanager.entities.User
import net.datenwerke.security.service.usermanager.entities.Group
import net.datenwerke.security.service.usermanager.entities.OrganisationalUnit


/**
 * removeWriteProtection.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.6.1-6096
 * Removes write protection in all users, organizational units and groups
 * Has to be called with -c flag to commit changes to the database.
 */

def userManagerService = GLOBALS.getInstance(UserManagerService)

def all = GLOBALS.getEntitiesByType(User) + GLOBALS.getEntitiesByType(Group) + GLOBALS.getEntitiesByType(OrganisationalUnit)
def modified = false
all
	.findAll { it.writeProtected }
	.each { node ->
		def msg = "node '$node was modified"
		node.writeProtection = false
		modified = true
		tout.println msg
	}
	
if (modified) 
	return 'Removing write protection is finished'
else
	return 'No nodes had write protection, so no nodes were modified'