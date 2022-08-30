import java.time.LocalDateTime
import net.datenwerke.rs.core.service.mail.MailBuilderFactory
import net.datenwerke.rs.core.service.mail.MailService
import net.datenwerke.rs.grideditor.service.grideditor.hookers.GridEditorAfterCommitHook;
import net.datenwerke.security.service.usermanager.UserManagerService
/**
 * gridReportEmailHook.groovy
 * Version: 1.0.1
 * Type: Hook
 * Last tested with: ReportServer 4.4.0
 * Send email to selected users when changes to selected GridReports are made.
 * The email is sent using the standard email-datasink which is selected in
 * fileserver/etc/datasinks/datasinks.cf
 * Other email examples: https://github.com/infofabrik/reportserver-samples/tree/main/src/net/datenwerke/rs/samples/tools/email
 *
 * Here is a quick list of types passed to the doAfterCommit Method:
 * TableReport tableReport, GridEditorReport gridReport, User user, ParameterSet ps,
 * List<Map<String,Object>> modified,
 * List<Map<String,Object>> deletedRecords, List<Map<String,Object>> newRecords
 *
 */

///////////////////////////////////////////////////////////
// Config:
// names of the reports to which the hook should latch on
def reportNames = [
   'MY_GRID_REPORT',
   'MY_OTHER_GRID_REPORT'
]
// the user ids. They have to exist and the ids are passed as long (L)
def to = [6L]

///////////////////////////////////////////////////////////

def mailBuilder = GLOBALS.getInstance(MailBuilderFactory)
def mailService = GLOBALS.getInstance(MailService)
def userService = GLOBALS.getInstance(UserManagerService)
// name of the hook
def HOOK_NAME = 'sendMailAfterCommitOnGridReport'

def callback = [
   doAfterCommit : { tableReport, gridReport,  user,  ps,  modified, deletedRecords, newRecords ->
      // check if hook activates
      if (! reportNames.contains(gridReport.name)) return
         def subject = configureSubject(gridReport)
      def content = configureContent(gridReport,  user,  modified, deletedRecords, newRecords)
      def mail = mailBuilder
            .create(
            subject,
            content,
            to.collect{userId -> userService.getNodeById(userId)})
            .build()
      mailService.sendMail mail
   }
] as GridEditorAfterCommitHook

GLOBALS.services.callbackRegistry.attachHook(HOOK_NAME, GridEditorAfterCommitHook.class, callback)
def configureSubject(def gridReport){
   return "Changes to ${gridReport.name}"
}

def configureContent(def gridReport, def user, def modified, def deletedRecords, def newRecords ) {
   return """
  $user.name commited the following changes to GridReport ${gridReport.name} ${LocalDateTime.now()} \n\n
  modified: ${modified.collect{ it.collect{ prettyPrint(it.key, it.value.before, it.value.after)} }} \n
  deleted: ${deletedRecords.toString()} \n
  new: ${newRecords.toString()}
  """
}

def prettyPrint(name, before, after) {
  "$name: ${before == after? before: before + ' ---> ' + after}"
}