package net.datenwerke.rs.samples.maintenance.mail

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.time.LocalDateTime

/**
 * sendEmail.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 3.5.0-6037
 * Sends a simple email using standard java APIs directly.
 * If you want to send an email to ReportServer users, you can use sendRSEmail.groovy
 */

def smtpHost = 'smtp.host.com'
def smtpPort = 587
def username = 'username'
def password = 'secret'

def to = 'to@host.com'
def from = 'from@host.com'
def subject = 'Test Email'
def content = "ReportServer Test Email ${LocalDateTime.now()}"

def auth = [
   getPasswordAuthentication: {
      new PasswordAuthentication(username, password)
   }
] as Authenticator

def props = new Properties()
props.put('mail.transport.protocol', 'smtp')
props.put('mail.smtp.host', smtpHost)
props.put('mail.smtp.port', smtpPort)
props.put('mail.smtp.auth', 'true')
/*
 * Set the tls/ssl properties as required by your server.
 *  More information here: https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
 */
//props.put('mail.smtp.starttls.enable','true')
props.put('mail.smtp.starttls.require','true')

def mailSession = Session.getInstance (props, auth)

// uncomment for debugging infos to stdout
mailSession.debug = true

def transport = mailSession.transport

def message = new MimeMessage(mailSession)
message.subject = subject
message.setContent(content as String, 'text/plain')
message.from = new InternetAddress(from)
message.addRecipient(Message.RecipientType.TO, new InternetAddress(to))

transport.connect()
transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
transport.close()