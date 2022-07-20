package net.datenwerke.rs.samples.tools.sendto.email

import net.datenwerke.rs.core.service.sendto.hooks.SendToTargetProviderHook
import net.datenwerke.rs.core.service.sendto.hooks.adapter.SendToTargetProviderHookAdapter
import net.datenwerke.rs.core.client.sendto.SendToClientConfig

import net.datenwerke.rs.core.service.mail.MailService
import net.datenwerke.rs.core.service.reportmanager.ReportExecutorService
import net.datenwerke.rs.core.service.mail.SimpleAttachment

/**
 * sendToEmail.groovy
 * Version: 1.0.1
 * Type: Hook
 * Last tested with: ReportServer 4.2.0-6066
 * Allows to add an entry to the send-to menu, sending the report via email.
 * https://reportserver.net/en/guides/script/chapters/Send-To/
 *
 * You can place this script in onstartup.d to hook up automatically.
 */

def HOOK_NAME = 'MY_SEND_TO_EMAIL'

reportExec = GLOBALS.getInstance(ReportExecutorService)
mailService = GLOBALS.getInstance(MailService)

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

      // prepare for sending mail
      def mail = mailService.newSimpleMail()
      mail.subject = 'The Report'
      mail.toRecipients = values['email']
      mail.from = 'from@reportserver.net'

      def attachment = new SimpleAttachment(pdf.report, pdf.mimeType, 'filename.pdf')
      mail.setContent('Some Message', attachment)

      // send mail
      mailService.sendMail mail

      return "Send the report via mail. Config $values" as String
   }

] as SendToTargetProviderHookAdapter

GLOBALS.services.callbackRegistry.attachHook(HOOK_NAME, SendToTargetProviderHook, callback)
