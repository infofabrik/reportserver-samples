import net.datenwerke.rs.core.service.datasinkmanager.DatasinkService
import net.datenwerke.rs.core.service.reportmanager.ReportService
import net.datenwerke.scheduler.service.scheduler.nlp.NlpTriggerService
import net.datenwerke.scheduler.service.scheduler.SchedulerService
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.rs.emaildatasink.service.emaildatasink.action.ScheduleAsEmailFileAction
import net.datenwerke.rs.scheduler.service.scheduler.jobs.report.ReportExecuteJob


/**
* variantScheduler.groovy
* Version: 1.0.0
* Type: Normal Script
* Last tested with: ReportServer 3.7.0-6044
* Schedules a report via the report's ID and puts up all information necessary 
* including an email datasink action.
* Has to be called with -c flag to commit changes to the database.
*/

/**** USER SETTINGS ****/

REPORT_ID = 108285L
DATE_EXPRESSION = "at 01.08.2021 17:30"
JOB_NAME = "Test Job"
JOB_DESCRIPTION = "A Test Job Description"
OUTPUT_FORMAT = "PDF"
OWNER_ID = 6L                       // please use the ID of the Owner    
EXECUTOR_ID = 6L                    // please use the ID of the Executor
RECIPIENT_IDS = [6L, 7L]            // please use the IDs of the Recipients

// email settings
EMAIL_DATASINK = "Default Email Datasink"
EMAIL_ATTACHMENT_FILENAME = 'My Report'
EMAIL_SUBJECT = 'Test Email Subject'
EMAIL_TEXT = 'Test Email Message Body'

/***********************/

def triggerService = GLOBALS.getInstance(NlpTriggerService.class)
def schedulerService = GLOBALS.getInstance(SchedulerService.class)
def reportService = GLOBALS.getInstance(ReportService.class)
def userManagerService = GLOBALS.getRsService(UserManagerService.class)
def datasinkService = GLOBALS.getInstance(DatasinkService.class)

def report = reportService.getReportById(REPORT_ID)                 //singular specific Report by ID

def owner = userManagerService.getNodeById(OWNER_ID)
def executor = userManagerService.getNodeById(EXECUTOR_ID)
def recipients = userManagerService.getUsers(RECIPIENT_IDS)    
 
/* create the Report Job and add the corresponding owner, executor and recipients */
def job = new ReportExecuteJob()
job.report = report
job.owners = [owner] as Set
job.executor = executor
job.recipients = recipients as List
job.outputFormat = OUTPUT_FORMAT
 
/* create a an action for the schedule, in this case an email file action */
def action = new ScheduleAsEmailFileAction()
def emailDatasink = datasinkService.getDatasinkByName(EMAIL_DATASINK)
action.emailDatasink = emailDatasink
action.name = EMAIL_ATTACHMENT_FILENAME
action.subject = EMAIL_SUBJECT
action.message = EMAIL_TEXT
 
/* add the description, version, actions etc. */
job.title = JOB_NAME
job.description = JOB_DESCRIPTION
job.actions = [action]
 
/* parse the date with nlp */
def trigger = triggerService.parseExpression DATE_EXPRESSION
 
/* schedule the job via the schedulerService */
schedulerService.schedule job, trigger
