package net.datenwerke.rs.samples.admin.svg.scriptreport

import net.datenwerke.rs.dot.service.dot.DotService
import net.datenwerke.rs.dot.service.dot.TextFormat
import groovy.xml.*

/**
 * DOT_SVG_scriptreport.groovy
 * Version: 1.0.0
 * Type: Script report
 * Last tested with: ReportServer 4.6.0
 * Parses and renders a DOT file to a SVG graph. Displays it on browser.
 * Output: https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/admin/svg/scriptreport/DOT_SVG_scriptreport.png
 */

def dotService = GLOBALS.getInstance(DotService)

def mySvgString = dotService.render(TextFormat.SVG, """
digraph D {
    
    and1[
      color="#c48f68"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label = "AND";
    ];
    
    
    or2[
      color="#dec5a4"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label = "OR";
    ];
    

and1 -> or2;

    i3[
      color="#328a3f"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label="CUS_CUSTOMERNUMBER
include [144]
null_handling --
case_sensitive true";
    ];
    
or2 -> i3;

    i4[
      color="#328a3f"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label="Y_VOLUME
null_handling --
include_range [[100000, null]]
case_sensitive true";
    ];
    
and1 -> i4;
    
    or5[
      color="#dec5a4"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label = "OR";
    ];
    

and1 -> or5;

    e6[
      color="#65c6db"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label="CUS_STATE >= CUS_COUNTRY";
    ];
    
or5 -> e6;

    i7[
      color="#328a3f"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label="CUS_COUNTRY
include [USA]
null_handling --
case_sensitive true";
    ];
    
and1 -> i7;
    
    or8[
      color="#dec5a4"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label = "OR";
    ];
    

and1 -> or8;
    
    and9[
      color="#c48f68"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label = "AND";
    ];
    

or8 -> and9;
    
    or10[
      color="#dec5a4"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label = "OR";
    ];
    

and9 -> or10;

    i11[
      color="#328a3f"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label="CUS_CUSTOMERNAME
include [Baane Mini Imports]
null_handling Include
case_sensitive false";
    ];
    
or10 -> i11;
    
    or12[
      color="#dec5a4"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label = "OR";
    ];
    

and1 -> or12;

    e13[
      color="#65c6db"; 
      fontcolor="black"; 
      style=filled;
      fontsize="15pt";
      label="CUS_COUNTRY < CUS_CUSTOMERNAME";
    ];
    
or12 -> e13;

}
""", 1200)

                              
def report = """\
<html>
<head>
  <title>SVG</title>
</head>
<body>
   <h1>SVG</h1>
   $mySvgString
</body>
</html>
"""


return renderer.get("html").render(report as String)