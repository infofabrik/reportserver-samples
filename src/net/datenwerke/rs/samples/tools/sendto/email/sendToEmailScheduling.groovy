import net.datenwerke.rs.core.service.sendto.hooks.SendToTargetProviderHook
import net.datenwerke.rs.core.service.sendto.hooks.adapter.SendToTargetProviderHookAdapter
import net.datenwerke.rs.core.client.sendto.SendToClientConfig

import net.datenwerke.rs.core.service.mail.MailService
import net.datenwerke.rs.core.service.reportmanager.ReportExecutorService
import net.datenwerke.rs.core.service.mail.SimpleAttachment

/**
 * sendToEmailScheduling.groovy
 * Version: 1.0.0
 * Type: Hook
 * Last tested with: ReportServer 4.6.1-6096
 * Allows to add an entry to the send-to menu, sending the report via email.
 * Includes scheduling support as well.
 * https://reportserver.net/en/guides/script/chapters/Send-To/
 *
 * You can place this script in onstartup.d to hook up automatically.
 */

def HOOK_NAME = 'MY_SEND_TO_EMAIL'

reportExec = GLOBALS.getInstance(ReportExecutorService)
mailService = GLOBALS.getInstance(MailService)

def doSendEmail(pdf, values) {
  // prepare for sending mail
  def mail = mailService.newSimpleMail()
  mail.subject = 'The Report'
  mail.toRecipients = values['email']
  mail.from = 'from@reportserver.net'

  def attachment = new SimpleAttachment(pdf.report, pdf.mimeType, 'filename.pdf')
  mail.setContent('Some Message', attachment)

  // send mail
  mailService.sendMail mail
}


def callback = [
   consumes : { report ->
      def config = new SendToClientConfig()
      config.title = 'Send via Custom Mail'
      config.icon = 'send'
      config.form = """
{
        "width": 400,
        "height": 180,
        "form" : {
                "labelAlign": "top",
                "fields": [{
                        "id": "email",
                        "type": "string",
                        "label": "Email Address",
                        "value": "name@example.com"
                }]
        }
}
"""
      return config
   },
   getId : {
      ->
      return 'someUniqueId'
   },
   sendTo : { report, values, execConfig ->
      def pdf = reportExec.execute(report, ReportExecutorService.OUTPUT_FORMAT_PDF, execConfig)

      doSendEmail(pdf, values)

      return "Send the report via mail. Config $values" as String
   },
  
   scheduledSendTo: { compiledReport, report, reportJob, format, values ->
     def pdf = reportExec.execute(report, ReportExecutorService.OUTPUT_FORMAT_PDF)
     
     doSendEmail(pdf, values)
   }

] as SendToTargetProviderHookAdapter

GLOBALS.services.callbackRegistry.attachHook(HOOK_NAME, SendToTargetProviderHook, callback)