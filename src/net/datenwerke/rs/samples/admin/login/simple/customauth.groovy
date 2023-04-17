import net.datenwerke.security.service.authenticator.AuthenticatorService
import net.datenwerke.security.client.login.AuthToken
import net.datenwerke.rs.authenticator.client.login.dto.UserPasswordAuthToken

/**
 * customauth.groovy
 * Version: 1.0.0
 * Type: Authenticator
 * Last tested with: ReportServer 4.5.0-6088
 * Demonstrates simple ReportServer login page customization.
 * Both the script and the html have to placed in a "web accessible" directory.
 */

def service = GLOBALS.getInstance(AuthenticatorService)
 
def user = httpRequest.getParameter('user')
def pw = httpRequest.getParameter('pw')

/* construct authentication tokens */
def token = new UserPasswordAuthToken()
token.username = user
token.password = pw

def result = service.authenticate([token] as AuthToken[])

if(result.isAllowed()){
  httpResponse.sendRedirect('http://reporting.mycompany.com/ReportServer/ReportServer.html')
  return null
}

return 'Could not authenticate'