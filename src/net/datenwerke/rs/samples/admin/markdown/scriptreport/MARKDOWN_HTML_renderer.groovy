package net.datenwerke.rs.samples.admin.svg.scriptreport

import net.datenwerke.rs.fileserver.service.fileserver.entities.FileServerFile
import net.datenwerke.rs.fileserver.service.fileserver.FileServerService
import java.nio.charset.StandardCharsets
import groovy.xml.*
/**
 * MARKDOWN_HTML_renderer.groovy
 * Version: 1.0.0
 * Type: Script report
 * Last tested with: ReportServer 4.7.0
 * Uses the markdown-html renderer to parse and render a markdown file (or a simple String)
 * to an HTML report. 
 * !!!WARNING!!! Not every markdown command can be parsed properly although most are.
 * Output: https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/admin/svg/scriptreport/DOT_SVG_renderer.png
 */

def fsService = GLOBALS.getInstance(FileServerService)
def path =  "path/to/markdown_example.md"
def node = (FileServerFile) fsService.getNodeByPath(path)
String mdContent = new String(node.getData(), StandardCharsets.UTF_8);

return renderer.get("markdown-html").render(mdContent);
