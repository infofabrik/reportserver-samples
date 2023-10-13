package net.datenwerke.rs.samples.admin.svg.scriptreport

import net.datenwerke.rs.dot.service.dot.TextFormat
import net.datenwerke.rs.fileserver.service.fileserver.FileServerService
import java.nio.charset.StandardCharsets

/**
 * DOT_SVG_renderer_file.groovy
 * Version: 1.0.0
 * Type: Script report
 * Last tested with: ReportServer 4.7.0
 * Uses the dot-svg renderer to parse and render a DOT file from the RS file system
 * to a SVG graph. Displays it on browser.
 * Output: https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/admin/svg/scriptreport/DOT_SVG_renderer_file.png
 */

def fsService = GLOBALS.getInstance(FileServerService)
def path =  '/resources/graph.dot'
def node = fsService.getNodeByPath(path)
String data = new String(node.getData(), StandardCharsets.UTF_8)
return renderer.get("dot-svg").render(data)