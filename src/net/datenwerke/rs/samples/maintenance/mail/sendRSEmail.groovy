package net.datenwerke.rs.samples.maintenance.mail

import net.datenwerke.rs.core.service.mail.MailBuilderFactory
import net.datenwerke.rs.core.service.mail.MailService
import net.datenwerke.security.service.usermanager.UserManagerService
import java.nio.file.Paths

import java.time.LocalDateTime

/**
 * sendRSEmail.groovy
 * Type: Normal Script
 * Last tested with: ReportServer 3.5.0-6037
 * Sends a simple email to ReportServer users including attachments using ReportServer APIs.
 * Uses the mail configuration in /etc/mail/mail.cf
 * Suitable for testing email configuration.
 * If you want to send an email with java APIs directly, you can use sendEmail.groovy.
 */

def mailBuilder = GLOBALS.getInstance(MailBuilderFactory.class)
def mailService = GLOBALS.getInstance(MailService.class)
def userService = GLOBALS.getInstance(UserManagerService.class)

// the user ids. They have to exist and be valid.
def to = [123, 456]
def subject = 'Test Email'
def content = "ReportServer Test Email ${LocalDateTime.now()}"
// list of attachments. They have to exist and be readable
def attachments = [
   '/Users/user/file1.txt',
   '/Users/user/file2.txt'
]
// name of the zip
def attachmentFilename = 'data.zip'

def mail = mailBuilder.create(
      subject, 
      content, 
      to.collect{userId -> userService.getNodeById(userId)}
   )
   .withZippedAttachments(attachmentFilename, attachments.collect{ attachment -> Paths.get(attachment)})
   .build();

mailService.sendMail(mail)
