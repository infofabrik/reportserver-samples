package net.datenwerke.rs.samples.tools.nesting.nestedclass

import net.datenwerke.rs.scripting.service.scripting.scriptservices.GlobalsWrapper

/**
 * B.groovy
 * Version: 1.0.2
 * Type: Normal Script
 * Last tested with: ReportServer 4.5.0
 * Nested script demonstration for nested classes.
 * You can test the script with "exec A.groovy" and it should print C's output.
 */

class B {
   GlobalsWrapper GLOBALS

   public B(GlobalsWrapper GLOBALS) {
      this.GLOBALS = GLOBALS
   }
   
   public String prepareString() {
      def cClass = GLOBALS.loadClass('C.groovy', 'net.datenwerke.rs.samples.tools.nesting.nestedclass.C')
      // do not use clazz.newInstance(): https://stackoverflow.com/questions/195321/why-is-class-newinstance-evil
      // use getDeclaredConstructor() instead:
      def cInstance = cClass.getDeclaredConstructor().newInstance()
      return cInstance.prepareString()
      
      /*
       * In versions previous to ReportServer 4.5.0 you can use:
       *
       * def cSource = GLOBALS.read('C.groovy')
       * def cClass = new GroovyClassLoader(getClass().classLoader).parseClass( cSource )
       * // do not use clazz.newInstance(): https://stackoverflow.com/questions/195321/why-is-class-newinstance-evil
       * // use getDeclaredConstructor() instead:
       * def cInstance = cClass.getDeclaredConstructor().newInstance()
       *
       * return cInstance.prepareString()
       * 
       */
   }
}