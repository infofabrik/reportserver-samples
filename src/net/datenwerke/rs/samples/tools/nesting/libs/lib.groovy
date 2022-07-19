package net.datenwerke.rs.samples.tools.nesting.libs

/**
 * lib.groovy
 * Version: 1.0.0
 * Type: Library
 * Last tested with: ReportServer 4.2.0-6066
 * Lib: nested script demonstration.
 * Based on https://www.rosettacode.org/wiki/Caesar_cipher#Groovy
 */

def caesarEncode(k, text) {
    (text as int[]).collect { it==' ' ? ' ' : (((it & 0x1f) + k - 1).mod(26) + 1 | it & 0xe0) as char }.join()
}
def caesarDecode(k, text) { caesarEncode(26 - k, text) }
