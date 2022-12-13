package net.datenwerke.rs.samples.tools.nesting.nestedclass

import net.datenwerke.rs.scripting.service.scripting.scriptservices.GlobalsWrapper

/**
 * A.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.4.0-6084
 * Nested script demonstration for nested classes.
 * You can test the script with "exec A.groovy" and it should print C's output.
 */

// use absolute (e.g. /fileserver/bin/B.groovy) or relative path
def bSource = GLOBALS.read('B.groovy')
def bClass = new GroovyClassLoader().parseClass( bSource )
def bInstance = bClass.getDeclaredConstructor(GlobalsWrapper).newInstance(GLOBALS)

return bInstance.prepareString()