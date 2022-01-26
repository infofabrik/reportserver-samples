import net.datenwerke.rs.core.service.reportmanager.ReportService
import net.datenwerke.rs.base.service.reportengines.table.TableReportUtils
import net.datenwerke.rs.core.service.reportmanager.hooks.ReportExecutionNotificationHook
import net.datenwerke.rs.core.service.reportmanager.exceptions.ReportExecutorException

/**
 * vetoLargeReportExecution.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.0.0-6053
 * Allows to veto report execution to different formats if a report has a large number of rows.
 *
 * You can put this report in onstartup.d to hook up automatically.
 */

def HOOK_NAME = 'PROHIBIT_EXECUTION'
def maxNumberOfRows = 1000
/* available are: RS_TABLE (for dynamic list preview), HTML, PDF, EXCEL, CSV */
def vetoedFormats = ['RS_TABLE', 'EXCEL']
def errorMessage = '''Your report is too large to be exported, please add filters or similar to limit its output. 
Current number of rows:'''

def callback = [
   notifyOfReportExecution : { report, parameterSet, user, outputFormat, configs ->  },
   notifyOfReportsSuccessfulExecution : { compiledReport, report, parameterSet, user, outputFormat, configs ->
   },
   notifyOfReportsUnsuccessfulExecution : { e, report, parameterSet, user, outputFormat, configs ->
   },
   doVetoReportExecution: { report, parameterSet, user, outputFormat, configs ->
      def tableReportUtils = GLOBALS.getInstance(TableReportUtils)
      def reportService = GLOBALS.getInstance(ReportService)

      if (outputFormat in vetoedFormats) {
         def tableReportInformation = tableReportUtils.getReportInformation(report, null)
         if (tableReportInformation.dataCount > maxNumberOfRows)
            throw new ReportExecutorException("$errorMessage $tableReportInformation.dataCount")
      }
   }
] as ReportExecutionNotificationHook

GLOBALS.services.callbackRegistry.attachHook(HOOK_NAME, ReportExecutionNotificationHook,
      callback)