import net.datenwerke.rs.keyutils.service.keyutils.hooks.*
import net.datenwerke.rs.core.service.datasourcemanager.DatasourceService
/**
 * specificGenerateDefaultKeyExample.groovy
 * Version: 1.0.0
 * Type: Hook
 * Last tested with: ReportServer 4.7.0
 * Registers a new default key generation for Datasources
 */

def HOOK_NAME = "datasourcedefaultkey"
def iter = 0
def callback = [ 
   consumes: { treedb ->
     return treedb instanceof DatasourceService
   },
   generateDefaultKey: { treedb ->
     iter += 1
     return "my_datasource_default_key_${iter}" as String
   }
] as SpecificGenerateDefaultKeyHook
GLOBALS.services.callbackRegistry.attachHook(HOOK_NAME, SpecificGenerateDefaultKeyHook.class, callback)
