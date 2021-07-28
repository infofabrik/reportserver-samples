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


REPORT_ID = 239982L
DATE_EXP = "at 01.08.2021 17:30"
JOB_NAME = "Test Job"
JOB_DESCRIPTION = "A Test Job Description"
OUTPUT_FORMAT = "PDF"
OWNER_ID = 6L									//please use the ID of the Owner	
EXECUTOR_ID = 6L 								//please use the ID of the Executor
RECIPIENT_ID = [259962L, 99101L]				        //please use the IDs of the Recipients

/***********************/

def triggerService = GLOBALS.getInstance(NlpTriggerService.class)
def schedulerService = GLOBALS.getInstance(SchedulerService.class)
def reportService = GLOBALS.getInstance(ReportService.class)
def userManagerService = GLOBALS.getRsService(UserManagerService.class)
def datasinkService = GLOBALS.getInstance(DatasinkService.class)

Report report = reportService.getReportById(REPORT_ID) 				//singular specific Report by ID

try {
    
   /* First Variant for addressing every user as recipient 
   HashSet<User> hashSetUsers = userManagerService.getAllUsers()
   List<User> users = new ArrayList<User>(hashSetUsers)
   AbstractUserManagerNode abstractManagerNodeOwnerUser = userManagerService.getNodeById(OWNER_ID)
   AbstractUserManagerNode abstractManagerNodeExecutorUser = userManagerService.getNodeById(EXECUTOR_ID)
   User ownerUser, executorUser = null

   for (User userNode: users) {
      if(userNode.getName().equals(abstractManagerNodeOwnerUser.getName()))
      {
         ownerUser = userNode
      }
      if(userNode.getName().equals(abstractManagerNodeExecutorUser.getName()))
      {
         executorUser = userNode
      }
   }*/
    
   /* Second Variant with granularity for addressing only certain users as recipients!*/
   AbstractUserManagerNode abstractManagerNodeOwnerUser = userManagerService.getNodeById(OWNER_ID)
   AbstractUserManagerNode abstractManagerNodeExecutorUser = userManagerService.getNodeById(EXECUTOR_ID)
   User ownerUser = userManagerService.getUserByName(abstractManagerNodeOwnerUser.getName().split(" ")[0])
   User executorUser = userManagerService.getUserByName(abstractManagerNodeExecutorUser.getName().split(" ")[0])
   Set<User> setUsers = userManagerService.getUsers(RECIPIENT_ID)
   List<User> users = new ArrayList<User>(setUsers) 
    
	
   /* create the Report Job and add the corresponding owner, executor and recipients */
   ReportExecuteJob reportExecuteJob = new ReportExecuteJob()
   reportExecuteJob.report = report
   reportExecuteJob.addOwner(ownerUser)
   reportExecuteJob.executor = executorUser
   reportExecuteJob.recipients = users
   reportExecuteJob.outputFormat = OUTPUT_FORMAT
  	
   /* create a an action for the schedule, in this case an email file action */
   ScheduleAsEmailFileAction scheduleAsEmailFileAction = new ScheduleAsEmailFileAction()
   EmailDatasink eMailDatasink = new EmailDatasink()
   eMailDatasink = datasinkService.getDatasinkByName("SMTP Datasink Test")
   scheduleAsEmailFileAction.emailDatasink = eMailDatasink
   scheduleAsEmailFileAction.name = "SMTP Datasink Test"
   scheduleAsEmailFileAction.subject = "Test Email Subject"
   scheduleAsEmailFileAction.message = "Test Email Message to my Recipient!"
  	
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