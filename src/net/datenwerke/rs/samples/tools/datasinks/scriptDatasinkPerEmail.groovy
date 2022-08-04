package net.datenwerke.rs.samples.tools.email

import net.datenwerke.rs.core.service.mail.MailBuilderFactory
import net.datenwerke.rs.core.service.mail.MailService
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.rs.utils.misc.MimeUtils
import net.datenwerke.rs.core.service.mail.SimpleAttachment
import java.nio.file.Paths

import java.time.LocalDateTime

/**
 * scriptDatasinkPerEmail.groovy
 * Version: 1.0.2
 * Type: Script datasink
 * Last tested with: ReportServer 4.3.0
 * Sends a given report/given files per email
 * and includes the script groovy file in the ZIP.
 */

def mailBuilder = GLOBALS.getInstance(MailBuilderFactory)
def mailService = GLOBALS.getInstance(MailService)
def userService = GLOBALS.getInstance(UserManagerService)
def mimeUtils = GLOBALS.getInstance(MimeUtils)

// the user ids. They have to exist and the ids are passed as long (L)
def to = [123L]
def subject = 'Script datasink'
def content = "ReportServer script datasink ${LocalDateTime.now()}"
// name of the zip
def attachmentFilename = 'data.zip'

def attachments = [
   new SimpleAttachment(data, // you can also use report
   mimeUtils.getMimeTypeByExtension(datasinkConfiguration.filename),
   datasinkConfiguration.filename),
   // add the script
   new SimpleAttachment(script.data, script.contentType, script.name) 
]

def mail = mailBuilder.create(
      subject,
      content,
      to.collect{userId -> userService.getNodeById(userId)})
      .withAttachments(attachments)
      .withZippedAttachments(attachmentFilename)
      .build()

mailService.sendMail mail