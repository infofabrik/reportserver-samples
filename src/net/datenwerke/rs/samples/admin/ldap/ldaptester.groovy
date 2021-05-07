package net.datenwerke.rs.samples.admin.ldap

import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.directory.Attributes
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.LdapName

import java.util.logging.Level
import java.util.logging.Logger

/**
 * ldaptester.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 3.6.0-6038
 * Allows to safely test LDAP settings printing all results found into Tomcat logs.
 * Useful for finding out LDAP configuration before setting ldap.cf attributes.
 * The logs should be opened with a text editor without automatic line breaks.
 */

/* start settings. Only this has to be adapted for testing LDAP  --------------------------------------- */

// if true: prints to console. If false: prints to tomcat logs.
// formatting is *much* better in the tomcat logs, so this is the recommended setting: false
printToConsole = false

// same settings as in the ldap.cf configuration file
url = 'ldap://directory.example.com'
port = 389
principal = 'CN=ldaptest,CN=Users,DC=directory,DC=example,DC=com'
credentials = 'ldaptest'
base = 'OU=EXAMPLE,DC=directory,DC=example,DC=com'
filter = '(|(objectClass=organizationalUnit)(objectClass=user)(objectClass=group))'
// externalDir not necessary for this test
// writeProtection not necessary for this test
includeNamespace = false
// logResultingTree not necessary for this test
// start of attributes:
// general attributes:
objectClass = 'objectClass'
guid = 'objectGUID'
// organizational unit attributes:
organizationalUnit_objectClass = 'organizationalUnit'
organizationalUnit_name = 'name'
// group attributes:
group_objectClass = 'group'
group_name = 'name'
member = 'member'
// user attributes:
user_objectClass = 'user'
user_firstname = 'givenName'
user_lastname = 'sn'
user_username = 'sAMAccountName'
user_mail = 'mail'

/* end settings ---------------------------------------------------------------------------------------- */

logger = Logger.getLogger(getClass().name)
loggerPrefix = "LDAP ${'='*3}"
lineLength = 400
format = "%-10s %-50s %-50s %-50s %-60s %-45s %-100s"
notFound = "${'!'*3}"
userCounter = 0
ouCounter = 0
groupCounter = 0

Hashtable env = new Hashtable()
env.put(Context.INITIAL_CONTEXT_FACTORY, 'com.sun.jndi.ldap.LdapCtxFactory')
env.put(Context.PROVIDER_URL, "$url:$port".toString())
env.put(Context.SECURITY_PRINCIPAL, principal)
env.put(Context.SECURITY_CREDENTIALS, credentials)
env.put('java.naming.ldap.attributes.binary', guid)

DirContext ctx = null

try {
   def start = System.nanoTime()
   
   ctx = new InitialDirContext(env)

   def returnAttrs = [
      objectClass,
      guid,
      organizationalUnit_objectClass,
      organizationalUnit_name,
      group_objectClass,
      group_name,
      member,
      user_objectClass,
      user_firstname,
      user_lastname,
      user_username,
      user_mail
   ]

   SearchControls searchControls = new SearchControls()
   searchControls.searchScope = SearchControls.SUBTREE_SCOPE
   searchControls.attributesToReturn = returnAttrs

   def results = ctx.search(base, filter, searchControls)

   printer "Starting LDAP Test..."

   printer 'LDAP Test Results: '
   printer '-'*lineLength

   printHeader()

   results.each { sr ->
      LdapName nodeName = new LdapName(includeNamespace ? sr.nameInNamespace : sr.name)
      LdapName nodeNameInNamespace = new LdapName(sr.nameInNamespace)

      /* skip empty nodes */
      if(nodeName.size() != 0) {
         LdapName parentName = (LdapName) nodeNameInNamespace.getPrefix(Math.max(0, nodeNameInNamespace.size() - 1))

         def objectClassAttr = sr.attributes[objectClass]
         if (!objectClassAttr)
            printer "$notFound${createAttributePair('objectClass')}"
         else {
            if(objectClassAttr.contains(organizationalUnit_objectClass))
               printOu sr, parentName
            else if(objectClassAttr.contains(user_objectClass))
               printUser sr, parentName
            else if(objectClassAttr.contains(group_objectClass))
               printGroup sr, parentName
         }
      }
   }

   def end = System.nanoTime()
   printer '-'*lineLength
   printSummary( end - start)
   printer '-'*lineLength

   printer "LDAP Test finished"

   if (!printToConsole)
      tout.println "LDAP Test results printed into tomcat logs"
} catch (Exception e) {
   logger.log(Level.WARNING, "LDAP test: ${e.message}", e)
   tout.println "Could not retrieve from directory. Please check your logs for details."
} finally{
   try {
      if(ctx)
         ctx.close()
   } catch (Exception e) {
      tout.println "Could not retrieve from directory. Please check your logs for details."
      logger.log(Level.WARNING, "LDAP test: ${e.message}", e)
   }
}

void printer(def str) {
   if (printToConsole)
      tout.println str
   else
      logger.log(Level.INFO, "$loggerPrefix $str")
}

void printHeader() {
   printer sprintf(format, 'Type', 'Name/Firstname', 'Lastname', 'Username', 'Email', 'Guid', 'Parent')
   printer '='*lineLength
}

void printSummary(def duration) {
   def summaryFormat = "%-15s %d %150s"

   printer 'Summary'
   printer (sprintf(summaryFormat, 'Total users', userCounter,
         "${!userCounter? createSummaryNote('user_objectClass'):''}"))
   printer (sprintf(summaryFormat, 'Total groups', groupCounter,
         "${!groupCounter? createSummaryNote('group_objectClass'):''}"))
   printer (sprintf(summaryFormat, 'Total OUs', ouCounter,
         "${!ouCounter? createSummaryNote('organizationalUnit_objectClass'):''}"))
   printer (sprintf("%-15s %,d ns %s", 'Duration', duration, ''))
}

String createSummaryNote(def attributeName) {
   "Hint: if this is not correct, check your ${createAttributePair(attributeName)} attribute and your filter."
}

String createAttributePair(def attributeName) {
   "($attributeName:'${this[attributeName]}')"
}

void printOu(def sr, def parent) {
   ouCounter++
   def formatted = sprintf (format,
         'OU', getStringAttribute(sr, 'organizationalUnit_name'), '', '', '', guidToString(sr.attributes[guid]), parent)
   printer formatted
}

void printUser(def sr, def parent) {
   userCounter++
   def formatted = sprintf (format,
         'User', getStringAttribute(sr, 'user_firstname'), getStringAttribute(sr, 'user_lastname'),
         getStringAttribute(sr, 'user_username'), getStringAttribute(sr, 'user_mail'),
         guidToString(sr.attributes[guid]), parent)
   printer formatted
}

void printGroup(def sr, def parent) {
   groupCounter++
   def formatted = sprintf (format,
         'Group', getStringAttribute(sr, 'group_name'), '', '', '', guidToString(sr.attributes[guid]), parent)
   printer formatted

   // print group members
   def memberAttribute = sr.attributes[member]
   if (member) {
      sr.attributes[member].getAll().each {
         LdapName memberName = new LdapName(it.toString())
         printer sprintf ("%10s %-226s", '>'*3, memberName.toString())
      }
   }
}

String getStringAttribute(SearchResult sr, String attributeName){

   if (!sr.attributes[this[attributeName]]) 
      return "[$notFound${createAttributePair(attributeName)})]"

   try{
      return sr.attributes[this[attributeName]].get().toString()
   }catch(Exception e){
      tout.println "Failed to retrieve attribute '${this[attributeName]}' from ${sr.nameInNamespace}", e
   }
}

void addByte(StringBuffer sb, int k) {
   if(k<=0xF)
      sb << '0'
   sb << Integer.toHexString(k)
}

String guidToString(def guidAttribute) {
   if (!guidAttribute) 
      return "[$notFound${createAttributePair('guid')}]"

   def bytes = guidAttribute.get()
   StringBuffer sb = new StringBuffer()
   addByte(sb, (int)bytes[3] & 0xFF)
   addByte(sb, (int)bytes[2] & 0xFF)
   addByte(sb, (int)bytes[1] & 0xFF)
   addByte(sb, (int)bytes[0] & 0xFF)
   sb << '-'
   addByte(sb, (int)bytes[5] & 0xFF)
   addByte(sb, (int)bytes[4] & 0xFF)
   sb.append("-")
   addByte(sb, (int)bytes[7] & 0xFF)
   addByte(sb, (int)bytes[6] & 0xFF)
   sb << '-'
   addByte(sb, (int)bytes[8] & 0xFF)
   addByte(sb, (int)bytes[9] & 0xFF)
   sb << '-'
   addByte(sb, (int)bytes[10] & 0xFF)
   addByte(sb, (int)bytes[11] & 0xFF)
   addByte(sb, (int)bytes[12] & 0xFF)
   addByte(sb, (int)bytes[13] & 0xFF)
   addByte(sb, (int)bytes[14] & 0xFF)
   addByte(sb, (int)bytes[15] & 0xFF)

   return "[${sb.toString()}]"
}