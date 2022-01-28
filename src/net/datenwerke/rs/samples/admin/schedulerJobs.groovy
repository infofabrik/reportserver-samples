import org.apache.commons.lang3.time.DateUtils

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.scheduler.service.scheduler.jobs.report.ReportExecuteJob
import net.datenwerke.scheduler.service.scheduler.SchedulerService
import net.datenwerke.security.service.usermanager.UserManagerService

/**
 * schedulerJobs.groovy
 * Version: 1.0.1
 * Type: Script datasource
 * Last tested with: ReportServer 4.0.0-6053
 * Shows all current scheduler jobs.
 */

/* set same sizes for varchars as in reportserver */
def varcharSize = 128

/* check registry: we cache the report for 10 minutes */
def cacheName = 'schedulerJobs'
def lastCacheName = "_report_${cacheName}_last"
def dataCacheName = "_report_${cacheName}_data"

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'].get(lastCacheName)
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'].get(dataCacheName)

def schedulerService = GLOBALS.getInstance(SchedulerService)
def userManagerService = GLOBALS.getInstance(UserManagerService)

/* prepare result */
def tableDef = [
   'JOB_ID':                    Long,
   'JOB_ACTION_CLASS':          String,
   'JOB_CREATE_DATE':           Date,
   'JOB_DESCRIPTION':           String,
   'JOB_EXECUTED_REPORT_ID':    Long,
   'JOB_EXEC_STATUS':           String,
   'JOB_EXPORT_CFG':            String,
   'JOB_LAST_EXEC_DATE':        Date,
   'JOB_SUPERSEDES':            Long,
   'JOB_OUTPUT_FORMAT':         String,
   'JOB_REPORT_ID':             Long,
   'JOB_REPORT_NAME':           String,
   'JOB_TITLE':                 String,
   'JOB_TRIGGER':               String,
   'JOB_OWNERS':                String,
   'JOB_EXECUTOR':              String,
   'JOB_JOB_RCPTS':             String
]

/* set same sizes for varchars as in reportserver */
TableDefinition tableDefinition = new TableDefinition(
   columnNames:     tableDef*.key,
   columnTypes:     tableDef*.value,
   displaySizes:    tableDef.collect{it instanceof String? varcharSize: 0}
   )

def result = new RSTableModel(tableDefinition: tableDefinition)

schedulerService.jobStore.allJobs
   .findAll{ it instanceof ReportExecuteJob}
   .findAll { it.active } // we don't want archived jobs
   .each{ job -> 
      result.addDataRow(new RSTableRow(tableDefinition, [
         job.id,
         job.actions*.class.simpleName as String,
         job.createdOn,
         job.description,
         job.executedReport?.id,
         job.executionStatus as String,
         job.exportConfiguration as String,
         job.lastExecution,
         job.linkToPrevious?.id,
         job.outputFormat,
         job.report?.id,
         job.report?.name,
         job.title,
         job.trigger.class.simpleName,
         job.owners as String,
         job.executor as String,
         userManagerService.getUsers(job.recipientsIds, true) as String
      ]))
   }

/* put the report into the cache */
GLOBALS.services['registry'].put(lastCacheName, new Date())
GLOBALS.services['registry'].put(dataCacheName, result)

result
