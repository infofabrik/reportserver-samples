import net.datenwerke.scheduler.service.scheduler.SchedulerService
import net.datenwerke.security.service.security.SecurityService
import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.rs.scheduler.service.scheduler.jobs.report.ReportExecuteJob
import net.datenwerke.rs.scripting.service.jobs.ScriptExecuteJob
import net.datenwerke.security.service.security.SecurityServiceSecuree
import net.datenwerke.rs.scheduler.service.scheduler.genrights.SchedulingBasicSecurityTarget
import net.datenwerke.security.service.security.rights.Execute

/**
 * replaceSchedulerUser.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.5.0-6085
 * Replaces an old user with a new user in all owners, executors, scheduled-by and recipients 
 * of all active scheduler jobs.
 * Has to be called with -c flag to commit changes to the database.
 */

/**** USER SETTINGS ****/

def OLD_USER_ID = 123L     // the ID (Long) of the user to replace
def NEW_USER_ID = 456L     // the ID (Long) of the new user

/***********************/

schedulerService = GLOBALS.getInstance(SchedulerService)
userManagerService = GLOBALS.getInstance(UserManagerService)
securityService = GLOBALS.getInstance(SecurityService)

oldUser = userManagerService.getNodeById(OLD_USER_ID)
newUser = userManagerService.getNodeById(NEW_USER_ID)

assert oldUser && newUser

// check permissions
schedulerService.jobStore.allJobs
      .findAll { it.active } // we don't want archived jobs
      .each { job ->
         if (job instanceof ReportExecuteJob)
            assertReportJobPermissions job
         else if (job instanceof ScriptExecuteJob)
            assertScriptJobPermissions job
      }

// replace user
schedulerService.jobStore.allJobs
      .findAll { it.active } // we don't want archived jobs
      .each{ job -> // Both ReportExecuteJobs and ScriptExecuteJobs
         if (job.scheduledBy == oldUser)
            job.scheduledBy = newUser

         /* Important: set executor before setting owner because of 
          * net.datenwerke.rs.scheduler.service.scheduler.jobs.report.ReportExecuteJob.prePersist()
          */
         if (job.executor == oldUser)
            job.executor = newUser
         if (job instanceof ReportExecuteJob) {
            def owners = job.owners
            if (oldUser in owners) {
               owners = owners - oldUser + newUser
               job.owners = owners
            }
            def recipients = userManagerService.getUsers(job.recipientsIds)
            if (oldUser in recipients) {
               recipients = recipients - oldUser + newUser
               job.recipients = (recipients as Set) as List // remove duplicates
            }
         }
         schedulerService.merge job
      }

private def reportToString(report) {
   "ID=${report.id ?: report.oldTransientId}, name='${report.name}'"
}

private def scriptToString(script) {
   "ID=${script.id}, name='${script.name}'"
}

private def assertReportJobPermissions(job) {
   if (job.scheduledBy == oldUser
         || job.executor == oldUser
         || oldUser in job.owners) {
      assert securityService.checkRights(newUser, SchedulingBasicSecurityTarget, SecurityServiceSecuree, Execute),
         "'$newUser' does not have 'SchedulingBasicSecurityTarget' permission: Execute"
      assert securityService.checkRights(newUser, job.report, SecurityServiceSecuree, Execute),
         "'$newUser' does not have 'Execute' permission on report: ${reportToString(job.report)}"
   }
   def recipients = userManagerService.getUsers(job.recipientsIds)
   if (oldUser in recipients) {
      assert newUser.email, "'$newUser' email is empty"
   }
}

private def assertScriptJobPermissions(job) {
   if (job.scheduledBy == oldUser
         || job.executor == oldUser) {
      assert securityService.checkRights(newUser, SchedulingBasicSecurityTarget, SecurityServiceSecuree, Execute),
         "'$newUser' does not have 'SchedulingBasicSecurityTarget' permission: Execute"
      assert securityService.checkRights(newUser, job.script, SecurityServiceSecuree, Execute),
         "'$newUser' does not have 'Execute' permission on script: ${scriptToString(job.script)}"
   }
}

"All users '$oldUser' correctly changed to '$newUser'"