package net.datenwerke.rs.samples.admin.teamspace

import org.apache.commons.lang3.time.DateUtils

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.core.service.reportmanager.interfaces.ReportVariant
import net.datenwerke.rs.scheduleasfile.service.scheduleasfile.entities.ExecutedReportFileReference
import net.datenwerke.rs.teamspace.service.teamspace.entities.TeamSpace
import net.datenwerke.rs.tsreportarea.service.tsreportarea.TsDiskService
import net.datenwerke.rs.tsreportarea.service.tsreportarea.entities.TsDiskReportReference

/**
 * reportsInTeamspace.groovy
 * Version: 1.0.4
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
def last = GLOBALS.services['registry'][lastCacheName]
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'][dataCacheName]

/* load services */
TsDiskService tsDiskService = GLOBALS.getInstance(TsDiskService)

/* prepare result */
def tableDef = [
   'TEAMSPACE_ID':              Long,
   'TEAMSPACE_NAME':            String,
   'TEAMSPACE_DESCRIPTION':     String,
   'REFERENCE_ID':              Long,
   'REFERENCE_NAME':            String,
   'REFERENCE_PATH':            String,
   'REPORT_ID':                 Long,
   'REPORT_NAME':               String,
   'REPORT_DESCRIPTION':        String,
   'REPORT_TYPE':               String,
   'REPORT_OUTPUT_FORMAT':      String,
   'BASE_REPORT_ID':            Long,
   'BASE_REPORT_NAME':          String
]

/* set same sizes for varchars as in reportserver */
TableDefinition tableDefinition = new TableDefinition(
   columnNames:     tableDef*.key,
   columnTypes:     tableDef*.value,
   displaySizes:    tableDef.collect{it instanceof String? varcharSize: 0}
   )

def result = new RSTableModel(tableDefinition: tableDefinition)

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
GLOBALS.services['registry'][lastCacheName] = new Date()
GLOBALS.services['registry'][dataCacheName] = result

result