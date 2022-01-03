package net.datenwerke.rs.samples.admin.ldap

import net.datenwerke.rs.ldap.service.ldap.LdapService

/**
 * ldapimport.groovy
 * Version: 2.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.0.0
 * 
 * As of ReportServer 4.0.0 you can use the "ldapimport" terminal command
 * together with the sso/ldap.cf configuration file in order to manually import LDAP users.
 * For scheduling the script periodically, you can use the current script as shown below and schedule it via "scheduleScript".
 * 
 * If for any reason you need to use the legacy script, you can find it here:
 * https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/admin/ldap/legacyLdapimport.groovy
 * 
 */

def ldapService = GLOBALS.getInstance(LdapService.class)

ldapService.importUsers()

