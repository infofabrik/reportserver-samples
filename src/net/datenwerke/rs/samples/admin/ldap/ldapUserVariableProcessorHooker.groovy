package net.datenwerke.rs.samples.admin.ldap
import net.datenwerke.rs.uservariables.service.uservariables.UserVariableService
import net.datenwerke.rs.ldap.service.ldap.hooks.LdapNodePostProcessHook
import net.datenwerke.security.service.usermanager.entities.User
import net.datenwerke.rs.ldap.service.ldap.LdapService

/**
 * ldapUserVariableProcessorHooker.groovy
 * Version: 1.0.1
 * Type: Hooker
 * Last tested with: ReportServer 4.0.0
 *
 * Allows you to post-process LDAP users after they are imported by 
 * setting a given user-variable's value to a given LDAP property value.
 *
 */

// name of the hook
def HOOK_NAME = 'MY_LDAP_USER_VARIABLE_PROCESSOR'

// the userVariable you want to set. The user variable definition must exist.
def userVar = 'myUserVar'

/* The LDAP property to read. It must exist in your LDAP installation.
// If not standard, it must be included into the "additional" attributes in ldap.cf */
def ldapProp = 'department'

def userVarService = GLOBALS.getInstance(UserVariableService.class)
def ldapService = GLOBALS.getInstance(LdapService.class)

def callback = [
   postProcessNode : {
      node, searchResult -> {
         if (! (node instanceof User)) return

         def allUserVarDefs = userVarService.definedVariableDefinitions
         allUserVarDefs.each{ userVarDef ->
            if (! userVarDef.name.equals(userVar) ) return

            def userVarInstance = null
            if (userVarService.hasVariableInstance(node, userVarDef)) {
               // retrieve instance
               userVarInstance = userVarService.getVariableInstanceForUser(node, userVarDef)
            } else {
               // create instance
               userVarInstance = userVarDef.createVariableInstance()
               userVarInstance.setFolk node
               userVarService.persist userVarInstance
            }

            userVarInstance.setValue ldapService.getStringAttribute(searchResult, ldapProp)
         }
      }
   }
] as LdapNodePostProcessHook


GLOBALS.services.callbackRegistry.attachHook(HOOK_NAME, LdapNodePostProcessHook.class, callback)