import net.datenwerke.rs.core.service.datasinkmanager.DatasinkService
import net.datenwerke.rs.core.service.reportmanager.ReportService
import net.datenwerke.rs.core.service.reportmanager.entities.reports.Report
import net.datenwerke.rs.emaildatasink.service.emaildatasink.action.ScheduleAsEmailFileAction
import net.datenwerke.rs.emaildatasink.service.emaildatasink.definitions.EmailDatasink
import net.datenwerke.rs.scheduler.service.scheduler.exceptions.SchedulerRuntimeException
import net.datenwerke.rs.scheduler.service.scheduler.jobs.report.ReportExecuteJob
import net.datenwerke.scheduler.service.scheduler.entities.AbstractAction
import net.datenwerke.scheduler.service.scheduler.entities.AbstractJob
import net.datenwerke.scheduler.service.scheduler.entities.AbstractTrigger
import net.datenwerke.scheduler.service.scheduler.nlp.NlpTriggerService
import net.datenwerke.scheduler.service.scheduler.SchedulerService
import net.datenwerke.security.service.usermanager.entities.AbstractUserManagerNode
import net.datenwerke.security.service.usermanager.entities.User
import net.datenwerke.security.service.usermanager.UserManagerService

import java.util.ArrayList


/**
* variantScheduler.groovy
* Version: 1.0.0
* Type: Normal Script
* Last tested with: ReportServer 3.7.0-6044
* Schedules a report via the report's ID and puts up all information necessary 
* including an email datasink action.
*/

/**** USER SETTINGS ****/


REPORT_ID = 108285L
DATE_EXP = "at 01.08.2021 17:30"
JOB_NAME = "Test Job"
JOB_DESCRIPTION = "A Test Job Description"
OUTPUT_FORMAT = "PDF"
OWNER_ID = 6L                                   //please use the ID of the Owner    
EXECUTOR_ID = 6L                                //please use the ID of the Executor
RECIPIENT_IDS = [6L]                        //please use the IDs of the Recipients

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

Report report = reportService.getReportById(REPORT_ID)              //singular specific Report by ID

try {
    
   /* Second Variant with granularity for addressing only certain users as recipients!*/
   def owner = userManagerService.getNodeById(OWNER_ID)
   def executor = userManagerService.getNodeById(EXECUTOR_ID)
   def recipients = userManagerService.getUsers(RECIPIENT_IDS)
   List<User> users = new ArrayList<User>(recipients) 
    
    
   /* create the Report Job and add the corresponding owner, executor and recipients */
   ReportExecuteJob reportExecuteJob = new ReportExecuteJob()
   reportExecuteJob.report = report
   reportExecuteJob.addOwner owner
   reportExecuteJob.executor = executor
   reportExecuteJob.recipients = users
   reportExecuteJob.outputFormat = OUTPUT_FORMAT
    
   /* create a an action for the schedule, in this case an email file action */
   ScheduleAsEmailFileAction scheduleAsEmailFileAction = new ScheduleAsEmailFileAction()
   EmailDatasink eMailDatasink = new EmailDatasink()
   eMailDatasink = datasinkService.getDatasinkByName(EMAIL_DATASINK)
   scheduleAsEmailFileAction.emailDatasink = eMailDatasink
   scheduleAsEmailFileAction.name = EMAIL_ATTACHMENT_FILENAME
   scheduleAsEmailFileAction.subject = EMAIL_SUBJECT
   scheduleAsEmailFileAction.message = EMAIL_TEXT
    
   /* Link it with a List of abstract actions */
   List<AbstractAction> actions = new ArrayList<AbstractAction>()
   actions.add(scheduleAsEmailFileAction)
    
   /* create the abstract job and add the description, version, actions etc. */
   AbstractJob abstractJob = reportExecuteJob
   abstractJob.title = JOB_NAME
   abstractJob.description = JOB_DESCRIPTION
   abstractJob.actions = actions
    
   /* parse the date with nlp */
   AbstractTrigger abstractTrigger = triggerService.parseExpression(DATE_EXP)
    
   /* schedule the job via the schedulerService */
   schedulerService.schedule(abstractJob, abstractTrigger)
}
catch (SchedulerRuntimeException sre) {
    sre.printStackTrace()
}