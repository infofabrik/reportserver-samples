package net.datenwerke.rs.samples.admin.ldap
import net.datenwerke.rs.uservariables.service.uservariables.UserVariableService
import net.datenwerke.rs.ldap.service.ldap.hooks.LdapNodePostProcessHook
import net.datenwerke.security.service.usermanager.entities.User

/**
 * ldapUserVariableProcessorHooker.groovy
 * Version: 1.2.0
 * Type: Hook
 * Last tested with: ReportServer 4.5.0
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
 * If not standard, it must be included into the "additional" attributes in ldap.cf 
 */
def ldapProp = 'mail'

def userVarService = GLOBALS.getInstance(UserVariableService)

def callback = [
   postProcessNode : { node, searchResult -> {
         if (! (node instanceof User)) return

         def allUserVarDefs = userVarService.definedVariableDefinitions
         allUserVarDefs
            .findAll { userVarDef -> userVarDef.name == userVar }
            .each{ userVarDef ->
               def userVarInstance = null
               if (userVarService.hasVariableInstance(node, userVarDef)) {
                  // retrieve instance
                  userVarInstance = userVarService.getVariableInstanceForUser(node, userVarDef)
               } else {
                  // create instance
                  userVarInstance = userVarDef.createVariableInstance()
                  userVarInstance.folk = node
                  userVarService.persist userVarInstance
               }
   
               userVarInstance.value = searchResult.getAttributeValue(ldapProp)
            }
      }
   }
] as LdapNodePostProcessHook


GLOBALS.services.callbackRegistry.attachHook(HOOK_NAME, LdapNodePostProcessHook, callback)