import net.datenwerke.security.service.authenticator.AuthenticationResult
import net.datenwerke.rs.authenticator.server.hookers.LoginLogHook

/**
 * hookloglogins.groovy
 * Version: 1.0.0
 * Type: Hook
 * Last tested with: ReportServer 4.3.0-6078
 * Allows to log login attempts 
 * 
 * Note that as of ReportServer 4.3.0, this is supported out-of-the-box,
 * so you don't have to use this script manually.
 *
 * You can place this script in onstartup.d to hook up automatically.
 */

def HOOK_NAME = "LoginLogToFileHook"

def callback = [ log : { authRes, username ->
   def path = "C:\\projects\\test\\login.log"
   def data = [:]
   data.date = java.time.LocalDateTime.now()
   data.username = username
   data.success = authRes.allowed
   data.reason = ""
   if(!authRes.user) {
     data.reason = "unknown user"
   } else if (!data.success && !authRes.info) {
     data.reason = "password error"
   } else if (!data.success && authRes.info) {
     data.reason = authRes.info.toString()
   }
   def logFile = new File(path)
   log_plainText(logFile, data)
   //log_csv(logFile, data, ";")
  }
] as LoginLogHook
GLOBALS.services.callbackRegistry.attachHook(HOOK_NAME, LoginLogHook.class, callback)

def log_plainText(File file, def data) {
  if(!file.exists()) { file.createNewFile() }
  def order = ["date", "username", "success", "reason"]
  order.each{key -> file.append("" + data[key].toString() + "\t")}
  file.append("\n")
}
   
def log_csv(File file, def data, def separator) {
  def order = ["date", "username", "success", "reason"]
  if(!file.exists()) {
    file.createNewFile()
    file.write(order.join(separator))
    file.append("\n")
  }
  def values = order.collect{key -> '"' + data[key] + '"'}.toList()
  file.append(values.join(separator))
  file.append("\n")
}
