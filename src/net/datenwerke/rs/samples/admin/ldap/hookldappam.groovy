package net.datenwerke.rs.samples.admin.ldap

import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.InvalidNameException
import javax.naming.NamingException
import javax.naming.directory.InitialDirContext
import javax.persistence.NoResultException

import net.datenwerke.rs.authenticator.client.login.dto.UserPasswordAuthToken
import net.datenwerke.rs.authenticator.client.login.pam.UserPasswordClientPAM
import net.datenwerke.rs.utils.crypto.PasswordHasher
import net.datenwerke.security.client.login.AuthToken
import net.datenwerke.security.service.authenticator.AuthenticationResult
import net.datenwerke.security.service.authenticator.ReportServerPAM
import net.datenwerke.security.service.authenticator.hooks.PAMHook
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.security.service.usermanager.entities.User

import com.google.inject.Inject

/* If true, RS users are allowed alongside LDAP users. If false, only LDAP users are allowed. */
def ALLOW_LOCAL_USERS = true

def ldapPam = GLOBALS.getInstance(LdapPAM)
ldapPam.allowLocalUsers = ALLOW_LOCAL_USERS
def callback = [
   beforeStaticPamConfig: {pams -> },
   afterStaticPamConfig: {pams -> 
      /* The only PAM we allow is the current LDAP PAM. 
       * The PAM in reportserver.properties is ignored. */
      pams.clear()
      pams.add ldapPam
   }
] as PAMHook

GLOBALS.services.callbackRegistry.attachHook('LDAP_PAM', PAMHook, callback)



public class LdapPAM implements ReportServerPAM {

   private boolean allowLocalUsers

   private static final String CLIENT_MODULE_NAME = UserPasswordClientPAM.name
   private UserManagerService userManagerService
   private PasswordHasher passwordHasher

   @Inject
   public LdapPAM(UserManagerService userManagerService, PasswordHasher passwordHasher) {
      this.userManagerService = userManagerService
      this.passwordHasher = passwordHasher
   }

   @Override
   public AuthenticationResult authenticate(AuthToken[] tokens) {
      def authResult = tokens
         .findAll{ it instanceof UserPasswordAuthToken }
         .collect{ 
            def user = authenticate(it.username, it.password)
            if (user) {
               //println "####### LdapPAM: authenticate success (usr=$u.username)"
               return new AuthenticationResult(true, user)
            } else {
               //println '####### LdapPAM: authenticate failed (result=AuthenticationResult(false, null)'
               return new AuthenticationResult(false, null)
            }
         }

      if (authResult && authResult.size() == 1)
         return authResult[0]
      
      //println '####### LdapPAM: authenticate notoken (result=AuthenticationResult(false, null)'
      return new AuthenticationResult(false, null)
   }

   @Override
   public String getClientModuleName() {
      return CLIENT_MODULE_NAME
   }


   private User getUserOrNull(String username) {
      try {
         return userManagerService.getUserByName(username)
      } catch (NoResultException ex) {
         return null
      }
   }


   public User authenticate(String username, String cleartextPassword) {
      User user = getUserOrNull(username)
      if (!user)
         return null

      if (allowLocalUsers) {
         if (user.password && !user.password.isEmpty() && passwordHasher.validatePassword(user.password, cleartextPassword)) {
            //println '####### LdapPAM: authenticate with local password: success'
            return user
         } else {
            //println '####### LdapPAM: authenticate with local password: fail'
         }
      }

      LdapAuthenticator authenticator = new LdapAuthenticator()
      if (authenticator.authenticate(user, cleartextPassword)) {
         //println '####### LdapPAM: authenticate against directory server: success'
         return user
      } else {
         //println '####### LdapPAM: authenticate against directory server: failed'
         return null
      }
   }

}


public class LdapAuthenticator {

   public boolean authenticate(User user, String password) {
      if (!user.origin || !user.guid)
         return false

      Properties props = new Properties()

      props[Context.INITIAL_CONTEXT_FACTORY] = 'com.sun.jndi.ldap.LdapCtxFactory'
      props[Context.PROVIDER_URL] = getProvider(user)
      props[Context.URL_PKG_PREFIXES] = 'com.sun.jndi.url'
      props[Context.SECURITY_AUTHENTICATION] = 'simple'

      props[Context.SECURITY_PRINCIPAL] = getPrincipal(user)
      props[Context.SECURITY_CREDENTIALS] = password

      try {
         InitialDirContext ctx = new InitialDirContext(props)
         ctx.getAttributes(getPrincipal(user))
         return true
      } catch (AuthenticationException e) {
         return false
      } catch (InvalidNameException e) {
         throw new RuntimeException(e)
      } catch (NamingException e) {
         if (e.message.contains('LdapErr: DSID-0C0906E8')) {
            return false
         }
      }
   }

   private String getProvider(User user) {
      String origin = user.origin
      int i = origin.lastIndexOf('/')

      return origin[0..i]
   }

   private String getPrincipal(User user) {
      String origin = user.origin

      int i = user.origin.lastIndexOf('/')
      return origin[(i + 1)..(origin.size()-1)]
   }
}