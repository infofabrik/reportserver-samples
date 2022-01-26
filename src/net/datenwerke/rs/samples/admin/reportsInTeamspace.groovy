package net.datenwerke.rs.samples.admin

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.core.service.reportmanager.interfaces.ReportVariant
import net.datenwerke.rs.teamspace.service.teamspace.entities.TeamSpace
import net.datenwerke.rs.tsreportarea.service.tsreportarea.TsDiskService
import net.datenwerke.rs.tsreportarea.service.tsreportarea.entities.TsDiskReportReference
import net.datenwerke.rs.scheduleasfile.service.scheduleasfile.entities.ExecutedReportFileReference

import org.apache.commons.lang3.time.DateUtils

/**
 * reportsInTeamspace.groovy
 * Version: 1.0.1
 * Type: Script datasource
 * Last tested with: ReportServer 4.0.0-6053
 * Lists all reports contained in TeamSpaces and prints useful information about them.
 */

/* set same sizes for varchars as in reportserver */
def varcharSize = 128

def cacheName = 'reportsInTeamspace'
def lastCacheName = "_report_${cacheName}_last"
def dataCacheName = "_report_${cacheName}_data"

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'].get(lastCacheName)
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'].get(dataCacheName)

/* load services */
TsDiskService tsDiskService = GLOBALS.getInstance(TsDiskService)

/* prepare  result */
TableDefinition tableDefinition = new TableDefinition(
      [
         'TEAMSPACE_ID',
         'TEAMSPACE_NAME',
         'TEAMSPACE_DESCRIPTION',
         'REFERENCE_ID',
         'REFERENCE_NAME',
         'REFERENCE_PATH',
         'REPORT_ID',
         'REPORT_NAME',
         'REPORT_DESCRIPTION',
         'REPORT_TYPE',
         'REPORT_OUTPUT_FORMAT',
         'BASE_REPORT_ID',
         'BASE_REPORT_NAME'
      ],
      [
         Long,
         String,
         String,
         Long,
         String,
         String,
         Long,
         String,
         String,
         String,
         String,
         Long,
         String
      ]
      )


tableDefinition.displaySizes = [
   0,
   varcharSize,
   varcharSize,
   0,
   varcharSize,
   varcharSize,
   0,
   varcharSize,
   varcharSize,
   varcharSize,
   varcharSize,
   0,
   varcharSize
]

def result = new RSTableModel(tableDefinition)

GLOBALS.getEntitiesByType(TeamSpace).each{ ts ->
   tsDiskService.getGeneralReferencesFor(ts).each{ reportRef ->
      def report = (!(reportRef instanceof TsDiskReportReference)? reportRef.compiledReport.report : reportRef.report)
      def baseReport = (null != report && report instanceof ReportVariant) ? report.parent : null
      def referencePath = reportRef.rootLine.collect({reportRef.name}).reverse().join("/")

      def resultLine = [
         ts.id,
         ts.name,
         ts.description,
         reportRef?.id,
         reportRef?.name,
         referencePath,
         report.id,
         report.name,
         report.description,
         report.class.simpleName,
         reportRef instanceof ExecutedReportFileReference? reportRef.outputFormat: null,
         baseReport?.id,
         baseReport?.name
      ]

      /* add to result */
      result.addDataRow(new RSTableRow(tableDefinition, resultLine.toArray()))
   }
}

/* put the report into the cache */
GLOBALS.services['registry'].put(lastCacheName, new Date())
GLOBALS.services['registry'].put(dataCacheName, result)

return result