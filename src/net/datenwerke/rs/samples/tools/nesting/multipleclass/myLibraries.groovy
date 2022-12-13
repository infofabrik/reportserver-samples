package net.datenwerke.rs.samples.tools.nesting.multipleclass

/**
 * myLibraries.groovy
 * Version: 1.0.1
 * Type: Normal Script
 * Last tested with: ReportServer 4.5.0
 * Nested script demonstration for nested classes.
 * You can test the script with "exec A.groovy" and it should print C's output.
 */

class B {
   public String prepareString() {
      def cInstance = new C()
      return cInstance.prepareString()
   }
}

class C {
   public String prepareString() {
      return 'this is C'
   }
}