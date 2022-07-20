package net.datenwerke.rs.samples.tools.theming

import net.datenwerke.gf.service.theme.ThemeService

/**
 * themingElements.groovy
 * Version: 1.0.0
 * Type: Normal Script
 * Last tested with: ReportServer 4.4.2.0-6066
 * Lists all available theming element groups.
 * https://reportserver.net/en/tutorials/tutorial-theming/
 */

new TreeMap(GLOBALS.getInstance(ThemeService).colorMap).each{ k,v ->
  tout.println "$k: $v"
}

null