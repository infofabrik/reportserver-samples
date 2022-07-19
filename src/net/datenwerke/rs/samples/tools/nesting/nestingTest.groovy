package net.datenwerke.rs.samples.tools.nesting

/**
 * nestingTest.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.2.0-6066
 * Nested script demonstration.
 */

GLOBALS.exec('libs/lib.groovy')

def plain = 'The quick brown fox jumps over the lazy dog'
def key = 6
def cipher = caesarEncode(key, plain)

tout.println plain
tout.println cipher
tout.println caesarDecode(key, cipher)

