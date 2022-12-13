package net.datenwerke.rs.samples.tools.nesting.multipleclass

/**
 * myLibraries.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.4.0-6084
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