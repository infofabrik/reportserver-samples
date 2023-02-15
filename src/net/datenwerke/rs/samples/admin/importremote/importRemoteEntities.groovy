import groovy.json.JsonSlurper
import net.datenwerke.eximport.im.ImportConfig
import net.datenwerke.rs.terminal.service.terminal.TerminalService
import net.datenwerke.eximport.ImportService
import net.datenwerke.security.service.usermanager.entities.OrganisationalUnit
import net.datenwerke.eximport.ExportDataProviderImpl
import net.datenwerke.eximport.ExportDataAnalyzerService
import net.datenwerke.treedb.ext.service.eximport.TreeNodeImporterConfig
import net.datenwerke.usermanager.ext.service.eximport.UserManagerExporter
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.treedb.ext.service.eximport.TreeNodeImportItemConfig
import net.datenwerke.eximport.obj.ReferenceItemProperty
import net.datenwerke.eximport.im.ImportMode

/**
 * importRemoteEntities.groovy
 * Version: 1.0.0
 * Type: Normal script
 * Last tested with: ReportServer 4.6.0
 * Imports (copies) users from a remote ReportServer installation.
 * Has to be called with -c flag to commit changes to the database.
 */

/**** USER SETTINGS ****/

// path of the remote users to be imported
def entityPath = '/usermanager/ClassicModelCars'
// username in remote ReportServer installation
def user = 'myuser'
// apikey of the user in remote ReportServer installation
def apikey = 'myapikey'
// URL of the remote ReportServer installation containing the path to the REST accesspoint
def restUrl = 'http://localhost:8081/ReportServer/reportserver/rest'
// the target directory in the local installation. Has to be empty.
def target = '/usermanager/import'

/***********************/

terminalService = GLOBALS.getInstance(TerminalService)
analyzerService = GLOBALS.getInstance(ExportDataAnalyzerService)
userService = GLOBALS.getInstance(UserManagerService)
importService = GLOBALS.getInstance(ImportService)

enum SupportedExports {
   REPORTS('reportmanager'),
   USERS('usermanager'),
   DATASOURCES('datasources'),
   DATASINKS('datasinks'),
   FILESERVER('fileserver'),
   DASHBOARDLIB('dashboardlib'),

   private String manager

   SupportedExports(String manager) {
      this.manager = manager
   }
}

def exportType = SupportedExports.values().findAll { entityPath.startsWith("/$it.manager") }?.get(0)
assert exportType
assert target.startsWith("/$exportType.manager")

assertPreconditions exportType, target

def remoteUrl = "$restUrl/node-exporter$entityPath?user=$user&apikey=$apikey"

def httpConnection = new URL(remoteUrl).openConnection()
assert httpConnection.responseCode == httpConnection.HTTP_OK
def response = new JsonSlurper().parse(httpConnection.inputStream.newReader())

def exportXml = response.export
assert exportXml


/* prepare import */
def config = new ImportConfig(new ExportDataProviderImpl(exportXml.bytes))

if (exportType == SupportedExports.USERS) {
   importUsers config, target
}

def importUsers(ImportConfig config, target) {
   def treeConfig = new TreeNodeImporterConfig()
   config.addSpecificImporterConfigs treeConfig

   /* loop over items to find root*/
   def exportRootId = null
   def exportRootName = null
   analyzerService.getExportedItemsFor(config.exportDataProvider, UserManagerExporter).each {
      def nameprop = it.getPropertyByName('name')
      if(!it.getPropertyByName('parent') && OrganisationalUnit == it.type ){
         exportRootId = it.id
         exportRootName = nameprop?.element?.value
      }
   }
   if(!exportRootId)
      throw new IllegalStateException('Could not find root')

//   tout.println "Root folder: $exportRootName($exportRootId)"
   
   def targetNode = terminalService.getObjectByQuery(target)
   
   /* one more loop to configure user import */
   analyzerService.getExportedItemsFor(config.exportDataProvider, UserManagerExporter).each {
      def parentProp = it.getPropertyByName('parent')
      def usernameProp = it.getPropertyByName('username')

      if(usernameProp && userService.getUserByName(usernameProp.element.value)) {
         tout.println "Skipping: '${usernameProp.element.value}' because username already exists"
      } else {
         if(null != parentProp){
            def itemConfig = new TreeNodeImportItemConfig(it.id)

            /* set parent */
            if(parentProp instanceof ReferenceItemProperty && exportRootId == parentProp.referenceId)
               itemConfig.parent = targetNode

            config.addItemConfig itemConfig
         } else {
            /* add reference entry */
            config.addItemConfig(new TreeNodeImportItemConfig(it.id, ImportMode.REFERENCE, targetNode))
         }
      }
   }
   
   /* complete import */
   def result = importService.importData config
   tout.println 'Import complete'
}

def assertPreconditions(exportType, target) {
   if (exportType != SupportedExports.USERS)
      throw new IllegalArgumentException("$exportType not yet supported")
      
   def targetNode = terminalService.getObjectByQuery(target)
   if (!targetNode)
      throw new IllegalArgumentException("Node does not exist: '$target'")
      
   if (targetNode && !(targetNode instanceof OrganisationalUnit))
      throw new IllegalArgumentException("Node is not an organizational unit: '$target'")
   
   if (targetNode.children)
      throw new IllegalArgumentException("Node is not empty: '$target'")
}