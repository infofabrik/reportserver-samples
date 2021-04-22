package net.datenwerke.rs.samples.admin

import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableModel
import net.datenwerke.rs.base.service.reportengines.table.output.object.RSTableRow
import net.datenwerke.rs.base.service.reportengines.table.output.object.TableDefinition
import net.datenwerke.rs.base.service.reportengines.table.entities.ColumnReference
import net.datenwerke.rs.base.service.reportengines.table.entities.TableReport
import net.datenwerke.rs.computedcolumns.service.computedcolumns.entities.ComputedColumn
import net.datenwerke.rs.core.service.reportmanager.ReportService

import org.apache.commons.lang3.time.DateUtils

/**
 * fieldsindynamiclists.groovy
 * Type: Script datasource
 * Last tested with: ReportServer 3.4.0-6035
 * Shows all fields used in all dynamic lists' variants and prints useful information about them.
 */

/* set same sizes for varchars as in reportserver */
def varcharSize = 128

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'].get('_report_fields_in_dynamiclist_last')
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'].get('_report_fields_in_dynamiclist_data')

/* load services */
def reportService = GLOBALS.getInstance(ReportService.class)

/* prepare  result */
TableDefinition tableDefinition = new TableDefinition(
      [
         'BASE_REPORT_ID',
         'BASE_REPORT_NAME',
         'REPORT_ID',
         'REPORT_NAME',
         'COLUMN_ID',
         'COLUMN_NAME',
         'COLUMN_POSITION',
         'COMPUTED_COLUMN_EXPRESSION'
      ],
      [
         Long.class,
         String.class,
         Long.class,
         String.class,
         Long.class,
         String.class,
         Long.class,
         String.class
      ]
      )

/* set same sizes for varchars as in reportserver */
tableDefinition.displaySizes = [
   0,
   varcharSize,
   0,
   varcharSize,
   0,
   varcharSize,
   0,
   varcharSize
]

def result = new RSTableModel(tableDefinition)

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
GLOBALS.services['registry'].put('_report_fields_in_dynamiclist_last', new Date())
GLOBALS.services['registry'].put('_report_fields_in_dynamiclist_data', result)

return result