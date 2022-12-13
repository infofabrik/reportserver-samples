package net.datenwerke.rs.samples.tools.nesting.multipleclass

import net.datenwerke.rs.scripting.service.scripting.scriptservices.GlobalsWrapper

/**
 * A.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.4.0-6084
 * Nested script demonstration for nested classes.
 * You can test the script with "exec A.groovy" and it should print C's output.
 */

def loader = new GroovyClassLoader(getClass().classLoader)
def myLibrariesSource = GLOBALS.read('myLibraries.groovy')
loader.parseClass(myLibrariesSource)

// use B
def bClass = loader.loadClass('net.datenwerke.rs.samples.tools.nesting.multipleclass.B')
// do not use clazz.newInstance(): https://stackoverflow.com/questions/195321/why-is-class-newinstance-evil
// use getDeclaredConstructor() instead:
def bInstance = bClass.getDeclaredConstructor().newInstance()

tout.println "Using B: ${bInstance.prepareString()}"

// use C directly
def cClass = loader.loadClass('net.datenwerke.rs.samples.tools.nesting.multipleclass.C')
def cInstance = cClass.getDeclaredConstructor().newInstance()

tout.println "Using C: ${cInstance.prepareString()}"