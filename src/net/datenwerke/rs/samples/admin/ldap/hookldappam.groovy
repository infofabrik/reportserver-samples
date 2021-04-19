package net.datenwerke.rs.samples.admin.ldap

import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.InvalidNameException
import javax.naming.NamingException
import javax.naming.directory.InitialDirContext
import javax.persistence.NoResultException

import net.datenwerke.rs.authenticator.client.login.dto.UserPasswordAuthToken
import net.datenwerke.rs.authenticator.client.login.pam.UserPasswordClientPAM
import net.datenwerke.rs.utils.crypto.PasswordHasher;
import net.datenwerke.security.client.login.AuthToken
import net.datenwerke.security.service.authenticator.AuthenticationResult
import net.datenwerke.security.service.authenticator.ReportServerPAM
import net.datenwerke.security.service.authenticator.hooks.PAMHook
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.security.service.usermanager.entities.User

import com.google.inject.Inject

final LdapPAM ldapPam = GLOBALS.injector.getInstance(LdapPAM.class);
GLOBALS.services.callbackRegistry.attachHook("LDAP_PAM", PAMHook.class, new PAMHook() {

    public void beforeStaticPamConfig(LinkedHashSet < ReportServerPAM > pams) {}
    public void afterStaticPamConfig(LinkedHashSet < ReportServerPAM > pams) {
        /* The only PAM we allow is the current LDAP PAM. 
         * The PAM in reportserver.properties is ignored. */
        pams.clear()
        pams.add(ldapPam);
    }

});


public class LdapPAM implements ReportServerPAM {

    /* If true, RS users are allowed alongside LDAP users. If false, only LDAP users are allowed. */
    private static final ALLOW_LOCAL_USERS = true;

    private static final String CLIENT_MODULE_NAME = UserPasswordClientPAM.class.getName();
    private UserManagerService userManagerService;
    private PasswordHasher passwordHasher;

    @Inject
    public LdapPAM(UserManagerService userManagerService, PasswordHasher passwordHasher) {
        this.userManagerService = userManagerService;
        this.passwordHasher = passwordHasher;
    }


    public AuthenticationResult authenticate(AuthToken[] tokens) {
        for (Object token: tokens) {
            if (token instanceof UserPasswordAuthToken) {
                UserPasswordAuthToken credentials = (UserPasswordAuthToken) token;
                User u = authenticate(credentials.getUsername(), credentials.getPassword());
                if (null != u) {
                    //System.out.println("####### LdapPAM: authenticate success (usr=" + u.getUsername() + ")")
                    return new AuthenticationResult(true, u);
                } else {
                    //System.out.println("####### LdapPAM: authenticate failed (result=AuthenticationResult(false, null)")
                    return new AuthenticationResult(false, null);
                }
            }
        }
        //System.out.println("####### LdapPAM: authenticate notoken (result=AuthenticationResult(false, null)")
        return new AuthenticationResult(false, null);
    }


    protected User getUserOrNull(String username) {
        try {
            return userManagerService.getUserByName(username);
        } catch (NoResultException ex) {
            return null;
        }
    }


    public User authenticate(String username, String cleartextPassword) {
        User user = getUserOrNull(username);
        if (null == user)
            return null;

        if (ALLOW_LOCAL_USERS) {
            if (null != user.getPassword() && !user.getPassword().isEmpty() && passwordHasher.validatePassword(user.getPassword(), cleartextPassword)) {
                //System.out.println("####### LdapPAM: authenticate with local password: success")
                return user;
            } else {
                //System.out.println("####### LdapPAM: authenticate with local password: fail")
            }
        }

        LdapAuthenticator authenticator = new LdapAuthenticator();
        if (authenticator.authenticate(user, cleartextPassword)) {
            //System.out.println("####### LdapPAM: authenticate against directory server: success")
            return user;
        } else {
            //System.out.println("####### LdapPAM: authenticate against directory server: failed")
            return null;
        }
    }

    public String getClientModuleName() {
        return CLIENT_MODULE_NAME;
    }

}


public class LdapAuthenticator {

    public boolean authenticate(User user, String password) {
        if (null == user.getOrigin() || null == user.getGuid())
            return false;

        Properties props = new Properties();

        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.setProperty(Context.PROVIDER_URL, getProvider(user));
        props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.jndi.url");
        props.setProperty(Context.SECURITY_AUTHENTICATION, "simple");

        props.setProperty(Context.SECURITY_PRINCIPAL, getPrincipal(user));
        props.setProperty(Context.SECURITY_CREDENTIALS, password);

        try {
            InitialDirContext ctx = new InitialDirContext(props);
            ctx.getAttributes(getPrincipal(user));
            return true;
        } catch (AuthenticationException e) {
            return false;
        } catch (InvalidNameException e) {
            throw new RuntimeException(e);
        } catch (NamingException e) {
            if (e.getMessage().contains("LdapErr: DSID-0C0906E8")) {
                return false;
            }

        }

    }

    private String getProvider(User user) {
        String origin = user.getOrigin();
        int i = origin.lastIndexOf("/");

        return origin.substring(0, i);
    }

    private String getPrincipal(User user) {
        String origin = user.getOrigin();

        int i = user.getOrigin().lastIndexOf("/");
        return origin.substring(i + 1);
    }

}