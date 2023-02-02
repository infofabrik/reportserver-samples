import net.datenwerke.rs.core.service.reportmanager.ReportService
import net.datenwerke.rs.utils.entitycloner.EntityClonerService
import net.datenwerke.rs.core.service.reportmanager.ReportParameterService
import net.datenwerke.rs.core.service.reportmanager.interfaces.ReportVariant

/**
 * copyReportParameterDefinitions.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.5.0-6086
 * Copies all parameter definitions from a report (REPORT_ID_FROM) to another report (REPORT_ID_TO). 
 * Note: parameter dependencies are not copied. These have to be copied manually.
 * Has to be called with -c flag to commit changes to the database.
 */

/**** USER SETTINGS ****/

def REPORT_ID_FROM = 200802L
def REPORT_ID_TO = 203899L

// should parameters with the same key be replaced or skipped?
def REPLACE_EXISTING_PARAMETERS = true

/***********************/

reportService = GLOBALS.getInstance(ReportService)
entityClonerService = GLOBALS.getInstance(EntityClonerService)
parameterService = GLOBALS.getInstance(ReportParameterService)

def reportFrom = reportService.getReportById(REPORT_ID_FROM)
def reportTo = reportService.getReportById(REPORT_ID_TO)

assert reportFrom && reportTo

if (reportFrom instanceof ReportVariant)
   reportFrom = reportFrom.baseReport
if (reportTo instanceof ReportVariant)
   reportTo = reportTo.baseReport

def parameterDefsKeysFrom = reportFrom.parameterDefinitions*.key
assert !(null in parameterDefsKeysFrom), "The report with ID=$REPORT_ID_FROM contains parameter definitions with NULL key"

def parameterDefsFrom = reportFrom.parameterDefinitions
def clonedParameterDefsFrom = parameterDefsFrom
      .collect{ entityClonerService.cloneEntity(it) }

def parameterDefsKeysTo = reportTo.parameterDefinitions*.key

def copiedParamKeys = []
def existingParamKeys = []

clonedParameterDefsFrom
      .each{ param ->
         if (param.key in parameterDefsKeysTo) {
            existingParamKeys = existingParamKeys << param.key
            if (REPLACE_EXISTING_PARAMETERS) {
               def existingParameter = parameterService.getParameterByKey(reportTo.id, param.key)
               parameterService.remove existingParameter
               persist param, reportFrom, reportTo
            }
         } else {
            copiedParamKeys = copiedParamKeys << param.key
            persist param, reportFrom, reportTo
         }
         // important: always merge report to avoid lost ParameterInstances
         reportService.merge reportTo
      }

private def persist(param, reportFrom, reportTo) {
   //delete dependencies
   param.cleanDuplicated()
   parameterService.persist param
   parameterService.addParameterDefinition reportTo, param
}

"Parameters copied: $copiedParamKeys, parameters ${REPLACE_EXISTING_PARAMETERS?'replaced':'ignored'}: $existingParamKeys"