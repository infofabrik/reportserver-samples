package net.datenwerke.rs.samples.tools.email

import net.datenwerke.rs.core.service.mail.MailBuilderFactory
import net.datenwerke.rs.core.service.mail.MailService
import net.datenwerke.rs.emaildatasink.service.emaildatasink.definitions.EmailDatasink
import net.datenwerke.security.service.usermanager.UserManagerService
import java.nio.file.Paths

import java.time.LocalDateTime

import javax.mail.internet.InternetAddress

/**
 * sendRSEmail.groovy  
 *  
 * Version: 1.0.3  
 * Type: Normal Script  
 * Last tested with: ReportServer 4.7.3  
 *  
 * This script sends a simple email to ReportServer users, including attachments,  
 * using the ReportServer APIs. It relies on the mail configuration of your standard email datasink.  
 *  
 * Ideal for testing email configurations.  
 *  
 * If you need to send an email directly using Java APIs, consider using sendEmail.groovy instead.  
 */

def mailBuilder = GLOBALS.getInstance(MailBuilderFactory)
def mailService = GLOBALS.getInstance(MailService)
def userService = GLOBALS.getInstance(UserManagerService)

// the user ids. They have to exist and the ids are passed as long (L)
def to = [123L, 456L]
def subject = 'Test Email'
def content = "ReportServer Test Email ${LocalDateTime.now()}"
// list of attachments. They have to exist and be readable
def attachments = [
   '/path/to/file1.txt',
   '/path/to/file2.txt'
]
// name of the zip
def attachmentFilename = 'data.zip'

def defaultDatasink = mailService.loadDefaultEmailDatasink()
assert defaultDatasink

def mail = mailBuilder.create(
      subject, 
      content, 
      to.collect{userId -> userService.getNodeById(userId)},
      new InternetAddress(defaultDatasink.sender, defaultDatasink.senderName)
   )
   .withFileAttachments(attachments.collect{ attachment -> Paths.get(attachment)})
   .withZippedAttachments(attachmentFilename)
   .build()

mailService.sendMail mail 
