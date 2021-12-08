package net.datenwerke.rs.samples.groovylanguage

import java.nio.file.Paths
import java.nio.file.Files

/**
 * tryWithResources.groovy
 * Version: 1.1.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.0.0
 * Demonstrates the groovy's analogous construct to java's try-with-resources.
 */

def fileLocation = '/path/to/your/file.txt'

def separator = '='
def separatorLength = 40
def path = Paths.get(fileLocation)

/* 1 */
def sb = new StringBuilder()
tout.println '1: ' + separator*separatorLength
/* adding withCloseable closure allows groovy to close the resource (analogous to a java try-with-resources) */
Files.newBufferedReader(path).withCloseable{ res ->
   def theChar = res.read()

   while (-1 != theChar) {
      sb.append((char)theChar)
      theChar = res.read()
   }
}
tout.println sb.toString()

/* 2 */
sb = new StringBuilder()
tout.println '2: ' + separator*separatorLength
/* you can also use this with a new instance */
new BufferedReader(new FileReader(path.toFile())).withCloseable{ res ->
   def theChar = res.read()

   while (-1 != theChar) {
      sb.append((char)theChar)
      theChar = res.read()
   }
}
tout.println sb.toString()

/* 3 */
sb = new StringBuilder()
tout.println '3: ' + separator*separatorLength
/* if your outer stream can throw exceptions the inner one is not closed automatically, 
 * so you can use in this case the following more robust solution */
new FileReader(path.toFile()).withCloseable { fileReader ->
   new BufferedReader(fileReader).withCloseable{ res ->
      def theChar = res.read()

      while (-1 != theChar) {
         sb.append((char)theChar)
         theChar = res.read()
      }
   }
}
tout.println sb.toString()

/* 4 */
sb = new StringBuilder()
tout.println '4: ' + separator*separatorLength
/* you can use standard try-with-resources as well. As in 3, we used the more robust solution */
try (
FileReader fileReader = new FileReader(path.toFile());
BufferedReader res =  new BufferedReader(fileReader)
) {
   def theChar = res.read()

   while (-1 != theChar) {
      sb.append((char)theChar)
      theChar = res.read()
   }
}
tout.println sb.toString()