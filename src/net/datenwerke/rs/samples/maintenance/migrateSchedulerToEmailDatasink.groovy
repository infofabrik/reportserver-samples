import net.datenwerke.rs.base.service.parameterreplacements.provider.ReportForJuel
import net.datenwerke.rs.base.service.parameterreplacements.provider.UserForJuel
import net.datenwerke.rs.base.service.parameterreplacements.provider.ReportJobForJuel
import net.datenwerke.rs.configservice.service.configservice.ConfigService
import net.datenwerke.rs.core.service.mail.MailService
import net.datenwerke.rs.core.service.reportmanager.ReportExecutorService
import net.datenwerke.rs.emaildatasink.service.emaildatasink.action.ScheduleAsEmailFileAction
import net.datenwerke.rs.scheduler.service.scheduler.mail.MailReportAction
import net.datenwerke.rs.utils.juel.SimpleJuel
import net.datenwerke.scheduler.service.scheduler.SchedulerService
import javax.persistence.EntityManager
import static net.datenwerke.rs.utils.misc.StringEscapeUtils.removeInvalidFilenameChars

/**
 * migrateSchedulerToEmailDatasink.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.7.0
 * Migrates your old E-Mail scheduler jobs to use your default Email - SMTP server datasink.
 * You have to configure a default Email - SMTP server datasink before running this script.
 * This script has to be run after migrating to 4.7.0.
 * Execute this script like this: "exec -c migrateSchedulerToEmailDatasink.groovy". 
 * The -c flag is important in order to commit/save 
 * your changes into the database.
 */

def schedulerService = GLOBALS.getInstance(SchedulerService)
def mailService = GLOBALS.getInstance(MailService)
def configService = GLOBALS.getInstance(ConfigService)
def entityManager = GLOBALS.getInstance(EntityManager)

def defaultEmailDatasink = mailService.loadDefaultEmailDatasink()

if (!defaultEmailDatasink)
   throw new IllegalStateException("Default Email datasink could not be loaded.")

def attachmentNameTemplate = configService
      .getConfigFailsafe('scheduler/scheduler.cf')
      .getString('scheduler.mailaction.attachment.name', 'rep-${report.getName()}-${RS_CURRENT_DATE}')
//tout.println "Attachment template: $attachmentNameTemplate"

def activeJobs = schedulerService.jobStore.activeJobs
// tout.println "Active jobs: $activeJobs"

def actions = activeJobs*.actions
// tout.println "All actions: $actions"

def jobsToMigrate = activeJobs
      .findAll{ MailReportAction in it.actions*.class }
// tout.println "Jobs to migrate: " + jobsToMigrate

jobsToMigrate.each{ job ->
   tout.println "Migrating job '$job'..."

   def actionsToMigrate = job.actions.findAll{ MailReportAction == it.class }
   def migratedActions = []
   actionsToMigrate.each { action ->
      tout.println "Migrating action: '$action'..."
      def migratedAction = new ScheduleAsEmailFileAction()
      migratedAction.emailDatasink = defaultEmailDatasink
      migratedAction.message = action.message
      migratedAction.subject = action.subject
      migratedAction.compressed = action.compressed
      migratedAction.name = attachmentNameTemplate
      migratedActions << migratedAction
      tout.println "Migrated action: '$migratedAction'"
   }
   assert actionsToMigrate.size() == migratedActions.size()
   job.actions.removeAll actionsToMigrate
   actionsToMigrate.each{
      def toRemove = entityManager.find(it.class, it.id)
      assert toRemove
      entityManager.remove toRemove
   }
   job.actions.addAll migratedActions
}


"Migration success"