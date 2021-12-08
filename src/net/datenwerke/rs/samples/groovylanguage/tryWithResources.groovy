package net.datenwerke.rs.samples.groovylanguage

/**
 * tryWithResources.groovy
 * Version: 1.2.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.0.0
 * Demonstrates the groovy's analogous construct to java's try-with-resources.
 */

fileLocation = '/path/to/your/file.txt'

/* 1: adding withCloseable closure allows groovy to auto-close the resource 
 * (analogous to a java try-with-resources).
 * The object must implement Closeable or AutoCloseable in order for this to work. */
new File(fileLocation).newReader().withCloseable{ resource ->
   doSomethingWithResource 1, resource
}

/* 2: for streams you can use withStream() as well. This auto-flushes and auto-closes the stream */
new File(fileLocation).newInputStream().withStream{ resource ->
   doSomethingWithResource 2, resource
}

/* 3: you can also use this with a constructor */
new FileReader(fileLocation).withCloseable{ resource ->
   doSomethingWithResource 3, resource
}

/* 4: if you use nested streams and your outer stream can throw exceptions,
 * the inner one is not closed automatically, 
 * so you can use this robust solution.  */
new FileReader(fileLocation).withCloseable { fileReader ->
   new BufferedReader(fileReader).withCloseable{ resource ->
      doSomethingWithResource 4, resource
   }
}

/* 5: you can use standard java try-with-resources as well. 
 * As in 4, we use the robust solution */
try (
FileReader fileReader = new FileReader(fileLocation);
BufferedReader resource =  new BufferedReader(fileReader)
) {
   doSomethingWithResource 5, resource
}



/* 6: do NOT use the following, as the inner stream is not getting closed
 * if the outer stream throws exceptions */
//  try (
//  BufferedReader resource =  new BufferedReader(new FileReader(fileLocation))
//  ) {
//      doSomethingWithResource 6, resource
//  }



/* here you can do whatever you need with the given resource. 
 * In this example we just print its contents into the screen. */
def doSomethingWithResource(example, resource) {
   tout.println "Example $example: ${'='*40}"
   def sb = new StringBuilder()
   def theChar = resource.read()

   while (-1 != theChar) {
      sb.append((char)theChar)
      theChar = resource.read()
   }
   tout.println sb.toString()
}