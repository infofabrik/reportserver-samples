package net.datenwerke.rs.samples.tools.nesting.nestedclass

import net.datenwerke.rs.scripting.service.scripting.scriptservices.GlobalsWrapper

/**
 * B.groovy
 * Version: 1.0.0
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
      def cSource = GLOBALS.read('/fileserver/bin/C.groovy')
      def cClass = new GroovyClassLoader().parseClass( cSource )
      def cInstance = cClass.newInstance()

      return cInstance.prepareString()
   }
}