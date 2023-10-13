package net.datenwerke.rs.samples.admin.svg.scriptreport

import net.datenwerke.rs.dot.service.dot.DotService
import net.datenwerke.rs.dot.service.dot.TextFormat
import groovy.xml.*

/**
 * DOT_SVG_renderer.groovy
 * Version: 1.0.0
 * Type: Script report
 * Last tested with: ReportServer 4.7.0
 * Uses the dot-svg renderer to parse and render a DOT file to a SVG graph. Displays it on browser.
 * Output: https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/admin/svg/scriptreport/DOT_SVG_renderer.png
 */

package net.datenwerke.rs.samples.admin.svg.scriptreport

def dot = '''
digraph  {
node [style=filled,fillcolor =white];


start -> Usermanagement
start -> Reports
start -> Dashboards
start -> Datasources
start -> Datasinks
start -> RemoteReportServer
Usermanagement -> LDAP


start [shape=square label="ReportServer"];

Reports[fillcolor="#00255255"]

  subgraph cluster_0 {
    edge[arrowhead=none]
    style=filled;
    color=lightblue;
    Reports -> JXLS;
    JXLS -> JASPER;
    JASPER -> BIRT;
    BIRT -> SAIKU;
    SAIKU -> DynamicList;
    DynamicList -> Grid;
    Grid -> DOT;
    DOT -> Script;
    Script -> Crystal
    Crystal -> moreReport

    moreReport[label="..."]
  }


subgraph cluster_1 {
    edge[arrowhead=none]
    style=filled;
    color="#00880022";
    Datasources -> Relational;
    Datasources -> BIRT;
    Datasources -> rr;

    Relational -> Oracle;
    Oracle -> SQLServer;
    SQLServer -> SAPHana;
    SAPHana -> PostgreSQL;
    PostgreSQL -> SQLite;
    SQLite -> Mondrian;
    Mondrian -> Db2;
    Db2 -> moreDB

    Relational[fillcolor="#00880066"]
    moreDB[label="..."]
    rr[label="Script Datasource"];
  }
}
'''

return renderer.get("dot-svg").render(dot)