import net.datenwerke.rs.base.service.reportengines.table.entities.TableReport
import net.datenwerke.rs.base.service.reportengines.table.entities.TableReportVariant


/**
 * pivotVariantsUpdate.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.4.0-6084
 * Upgrades your pivot variants to ReportServer 4.4.0. Only execute this script if you upgraded to 4.4.0 or above.
 * Execute this script like this: "exec -c pivotUpdate.groovy". The -c flag is important in order to commit/save 
 * your changes into the database.
 * 
 * *********************************************************************
 * IMPORTANT ***********************************************************
 * ***********
 * - This script can only be executed once with the commit flag -c. If you execute it again, your pivot variants will not work.
 * - You should have a DB backup before executing this script in case something goes wrong with your pivot variants.
 * - Only execute this script once if you upgraded to 4.4.0 or above.
 * *********************************************************************
 */

def pivotVariants = GLOBALS.getEntitiesByType(TableReport)
.findAll{ it instanceof TableReportVariant }
.findAll{ it.cubeFlag && it.cubeXml }

def pattern = /(?ix)        # case insensitive(i), ignore space(x)
  (                         # start capture of group 1
    \[                      # open braquet
    .*                      # anything
    \]                      # close braquet
  )                         # end capture of group 1
  \.                        # dot
  (\1)                      # followed by the same group 1
  /

pivotVariants.each {
  def thinQuery = it.cubeXml
  thinQuery = thinQuery.replaceAll(pattern, '$1')
  it.cubeXml = thinQuery
}


'Your pivot variants were updated correctly. NEVER run this script again.'