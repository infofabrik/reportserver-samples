package net.datenwerke.rs.samples.tools.nesting.nestedclass

import net.datenwerke.rs.scripting.service.scripting.scriptservices.GlobalsWrapper

/**
 * A.groovy
 * Version: 1.0.3
 * Type: Normal Script
 * Last tested with: ReportServer 4.5.0
 * Nested script demonstration for nested classes.
 * You can test the script with "exec A.groovy" and it should print C's output.
 */

// use absolute (e.g. /fileserver/bin/B.groovy) or relative path
def bClass = GLOBALS.loadClass('B.groovy', 'net.datenwerke.rs.samples.tools.nesting.nestedclass.B')
def bInstance = bClass.getDeclaredConstructor(GlobalsWrapper).newInstance(GLOBALS)

return bInstance.prepareString()

/*
 * In versions previous to ReportServer 4.5.0 you can use:
 * 
 * def bSource = GLOBALS.read('B.groovy')
 * def bClass = new GroovyClassLoader(getClass().classLoader).parseClass( bSource )
 * def bInstance = bClass.getDeclaredConstructor(GlobalsWrapper).newInstance(GLOBALS)
 * return bInstance.prepareString()
 * 
 */
