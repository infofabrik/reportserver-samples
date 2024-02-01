
import net.datenwerke.rs.keyutils.service.keyutils.hooks.GeneralGenerateDefaultKeyHook

/**
 * generalGenerateDefaultKeyExample.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.7.0
 * Registers a new default key generation
 */

def HOOK_NAME = "defaultkey"
def iter = 0
def callback = [ 
   generateDefaultKey: { ->
     iter += 1
     return "my_default_key_${iter}" as String
   }
] as GeneralGenerateDefaultKeyHook
GLOBALS.services.callbackRegistry.attachHook(HOOK_NAME, GeneralGenerateDefaultKeyHook.class, callback)
