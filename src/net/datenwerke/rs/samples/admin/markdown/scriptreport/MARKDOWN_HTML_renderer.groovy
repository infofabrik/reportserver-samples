package net.datenwerke.rs.samples.admin.svg.scriptreport

import net.datenwerke.rs.fileserver.service.fileserver.entities.FileServerFile
import net.datenwerke.rs.fileserver.service.fileserver.FileServerService
import java.nio.charset.StandardCharsets
import groovy.xml.*

/*
* This renderer parses a markdown string and converts it to an HTML report.
* Most command structures will work although there are some exceptions.
* The renderer requires the file test.md to be of content type "text/markdown" to work.
*/

def fsService = GLOBALS.getInstance(FileServerService)
def path =  "resources/rs/test.md"
def node = (FileServerFile) fsService.getNodeByPath(path)
String mdContent = new String(node.getData(), StandardCharsets.UTF_8);

return renderer.get("markdown-html").render(mdContent);
