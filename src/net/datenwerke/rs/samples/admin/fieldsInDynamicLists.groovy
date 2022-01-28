package net.datenwerke.rs.samples.admin

import org.apache.commons.lang3.time.DateUtils

import net.datenwerke.rs.base.service.reportengines.table.entities.ColumnReference
import net.datenwerke.rs.base.service.reportengines.table.entities.TableReport
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.computedcolumns.service.computedcolumns.entities.ComputedColumn
import net.datenwerke.rs.core.service.reportmanager.ReportService

/**
 * fieldsInDynamicLists.groovy
 * Version: 1.0.4
 * Type: Script datasource
 * Last tested with: ReportServer 4.0.0-6053
 * Shows all fields used in all dynamic lists' variants and prints useful information about them.
 */

/* set same sizes for varchars as in reportserver */
def varcharSize = 128

def cacheName = 'fieldsInDynamicLists'
def lastCacheName = "_report_${cacheName}_last"
def dataCacheName = "_report_${cacheName}_data"

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'][lastCacheName]
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'][dataCacheName]

/* load services */
def reportService = GLOBALS.getInstance(ReportService.class)

/* prepare result */
def tableDef = [
   'BASE_REPORT_ID':                Long,
   'BASE_REPORT_NAME':              String,
   'REPORT_ID':                     Long,
   'REPORT_NAME':                   String,
   'COLUMN_ID':                     Long,
   'COLUMN_NAME':                   String,
   'COLUMN_POSITION':               Long,
   'COMPUTED_COLUMN_EXPRESSION':    String
]

TableDefinition tableDefinition = new TableDefinition(
   columnNames:     tableDef*.key, 
   columnTypes:     tableDef*.value,
   displaySizes:    tableDef.collect{it instanceof String? varcharSize: 0}
   )

def result = new RSTableModel(tableDefinition: tableDefinition)

reportService.allReports
      .findAll{ report -> report instanceof TableReport }
      .each { report ->
         report.columns.each { column ->
            result.addDataRow(new RSTableRow(tableDefinition, [
               report.parent.id,
               report.parent.name,
               report.id,
               report.name,
               column.id,
               column.name,
               column.position,
               column instanceof ColumnReference && column.reference instanceof ComputedColumn ? column.reference.expression: null
            ]))
         }
      }

/* put the report into the cache */
GLOBALS.services['registry'][lastCacheName] = new Date()
GLOBALS.services['registry'][dataCacheName] = result

result