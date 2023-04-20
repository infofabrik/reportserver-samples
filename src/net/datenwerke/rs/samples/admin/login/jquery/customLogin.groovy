import javax.servlet.http.HttpServletResponse;
import net.datenwerke.security.service.authenticator.AuthenticatorService 
import net.datenwerke.security.client.login.AuthToken
import net.datenwerke.rs.authenticator.client.login.dto.UserPasswordAuthToken
import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets

/**
 * customLogin.groovy
 * Version: 1.0.0
 * Type: Authenticator
 * Last tested with: ReportServer 4.5.0-6088
 * Demonstrates a customized ReportServer login page using jquery.
 * Both the script and the html have to placed in a "web accessible" directory.
 */


def response = GLOBALS.getInstance(HttpServletResponse)
if(args.size()==0){
  // no arguments => not enough info to login or logout
     response.status = 400
     return "Could not authenticate"
}
def authService = GLOBALS.getInstance(AuthenticatorService.class)
def slurper = new JsonSlurper()

def parameters = slurper.parseText(URLDecoder.decode(args[0], StandardCharsets.UTF_8.name()))

if( parameters["action"] == "logoff" && authService.isAuthenticated()){
  // log out if logged in 
  authService.logoff()
  response.status = 200
  return "success!"
}else{
  // try login
  def token = new UserPasswordAuthToken()
  token.username = parameters["user"]
  token.password = parameters["pw"]
  def result = authService.authenticate([token] as AuthToken[])

  if(result.isAllowed()){
    // if successfull: return null ==> 200 OK is used as default
    return null
  }
  // not allowed ==> error!
  response.status = 400
  return "Could not authenticate"
}


