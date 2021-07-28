package net.datenwerke.rs.samples.groovylanguage

import java.nio.file.Paths
import java.nio.file.Files

/**
 * tryWithResources.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 3.5.0-6037
 * Demonstrates the groovy's analogous construct to java's try-with-resources.
 */

def fileLocation = '/path/to/your/file.txt'

def path = Paths.get(fileLocation)
def sb = new StringBuffer()

// adding withCloseable closure allows groovy to close the resource (analogous to a java try-with-resources)
Files.newBufferedReader(path).withCloseable{ res -> 
   def theChar = res.read()
   
   while (-1 != theChar) {
      sb.append((char)theChar)
      theChar = res.read()
   }
}

tout.println sb.toString()
