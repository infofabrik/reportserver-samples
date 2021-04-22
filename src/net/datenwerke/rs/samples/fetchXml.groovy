package net.datenwerke.rs.samples

import net.datenwerke.rs.base.service.reportengines.table.output.object.*

import groovy.util.XmlSlurper
import java.util.Date
import java.text.SimpleDateFormat

import org.apache.commons.lang3.time.DateUtils

/**
 * fetchxml.groovy
 * Type: Script datasource
 * Last tested with: ReportServer 3.4.0-6035
 * Fetches XML data from the web and shows results as script datasource.
 */

/* check registry: we cache the report for 10 minutes */
def last = GLOBALS.services['registry'].get('_report_fetch_xml_last')
if(null != last && last instanceof Date && DateUtils.addMinutes(last.clone(), 10).after(new Date()) )
   return GLOBALS.services['registry'].get('_report_fetch_xml_data')

def address = "https://opendata.socrata.com/api/views/2dps-ayzy/rows.xml?accessType=DOWNLOAD"

def connection = address.toURL().openConnection()
def feed = new XmlSlurper().parseText(connection.content.text)

def td = new TableDefinition([
   'employee_name',
   'office',
   'city',
   'employee_title',
   'biweekly_hourly_rate',
   'payroll_type',
   'pay_period',
   'pay_period_begin_date',
   'pay_period_end_date',
   'check_date',
   'legislative_entity'
],[
   String.class,
   String.class,
   String.class,
   String.class,
   Float.class,
   String.class,
   Integer.class,
   Date.class,
   Date.class,
   Date.class,
   String.class
])
def table = new RSTableModel(td)
def formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

feed.row.row.each { row ->
   table.addDataRow(
         row.employee_name.text(),
         row.office.text(),
         row.city.text(),
         row.employee_title.text(),
         Float.parseFloat(row.biweekly_hourly_rate.text()),
         row.payroll_type.text(),
         Integer.parseInt(row.pay_period.text()),
         formatter.parse(row.pay_period_begin_date.text()),
         formatter.parse(row.pay_period_end_date.text()),
         formatter.parse(row.check_date.text()),
         row.legislative_entity.text(),
         )
}

/* put the report into the cache */
GLOBALS.services['registry'].put('_report_fetch_xml_last', new Date())
GLOBALS.services['registry'].put('_report_fetch_xml_data', table)

return table