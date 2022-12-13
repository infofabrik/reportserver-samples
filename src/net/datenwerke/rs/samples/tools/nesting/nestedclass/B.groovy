package net.datenwerke.rs.samples.tools.nesting.nestedclass

import net.datenwerke.rs.scripting.service.scripting.scriptservices.GlobalsWrapper

/**
 * B.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.4.0-6084
 * Nested script demonstration for nested classes.
 * You can test the script with "exec A.groovy" and it should print C's output.
 */

class B {

   GlobalsWrapper GLOBALS

   public B(GlobalsWrapper GLOBALS) {
      this.GLOBALS = GLOBALS
   }
   
   public String prepareString() {
      def cSource = GLOBALS.read('C.groovy')
      def cClass = new GroovyClassLoader(getClass().classLoader).parseClass( cSource )
      // do not use clazz.newInstance(): https://stackoverflow.com/questions/195321/why-is-class-newinstance-evil
      // use getDeclaredConstructor() instead:
      def cInstance = cClass.getDeclaredConstructor().newInstance()

      return cInstance.prepareString()
   }
}