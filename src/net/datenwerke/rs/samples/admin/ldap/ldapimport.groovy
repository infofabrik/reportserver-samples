package net.datenwerke.rs.samples.admin.ldap

import java.util.Optional

import net.datenwerke.rs.ldap.service.ldap.LdapService
import net.datenwerke.rs.ldapserver.service.ldapserver.entities.LdapServer

/**
 * ldapimport.groovy
 * Version: 2.1.0
 * Type: Normal Script
 * Last tested with: ReportServer 6.0.0
 * 
 * As of ReportServer 6.0.0 you can use the "ldapimport" terminal command
 * together with the default LDAP Server defined in the sso/sso.cf configuration file in order to manually import LDAP users.
 * For scheduling the functionality periodically, you can use the current script as shown below and schedule it via "scheduleScript".
 * Note you have to use the -c flag for committing changes into the database: exec -c ldapimport.groovy
 * 
 * If for any reason you need to use the legacy script, you can find it here:
 * https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/admin/ldap/legacyLdapimport.groovy
 * 
 */

LdapService ldapService = GLOBALS.getInstance(LdapService)

// resolves to the default LDAP server. If you need a custom LDAP server you can pass it
// to the method like this: ldapService.resolveServer(Optional.of('/remoteservers/your_ldap_server'))
LdapServer server = ldapService.resolveServer(Optional.empty())

ldapService.importUsers(server)

