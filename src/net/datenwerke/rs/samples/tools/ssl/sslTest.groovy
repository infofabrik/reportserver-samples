import javax.net.ssl.SSLSocketFactory

/**
 * sslTest.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.0.0
 * Tests SSL. Based on SSLPoke.java: 
 * https://confluence.atlassian.com/download/attachments/117455/SSLPoke.java
 */

def host = 'your_host_or_ip'
def port = 10636

def sslsocketfactory = SSLSocketFactory.default
def sslsocket = sslsocketfactory.createSocket host, port

def in = sslsocket.inputStream
def out = sslsocket.outputStream

// Write a test byte to get a reaction :)
out.write 1

while (in.available() > 0) 
 tout.print in.read()
  
tout.println 'Successfully connected'