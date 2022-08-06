import net.datenwerke.rs.core.service.datasinkmanager.DatasinkTreeService
import net.datenwerke.rs.core.service.datasourcemanager.DatasourceService
import net.datenwerke.rs.core.service.reportmanager.ReportService
import net.datenwerke.rs.dashboard.service.dashboard.DashboardManagerService
import net.datenwerke.rs.dashboard.service.dashboard.DashboardService
import net.datenwerke.rs.fileserver.service.fileserver.FileServerService
import net.datenwerke.rs.fileserver.service.fileserver.entities.AbstractFileServerNode
import net.datenwerke.rs.tsreportarea.service.tsreportarea.TsDiskService
import net.datenwerke.security.service.usermanager.UserManagerService
import groovy.json.JsonBuilder

/**
 * radialTree.groovy
 * Version: 1.0.3
 * Type: Script report
 * Last tested with: ReportServer 4.2.0-6066
 * Visualize ReportServer entities structures as radial tree chart
 *
 * Necessary for installation:
 * <ul>
 * <li>Script report containing parameter "treeDbService" || for all possible entity types see allTreeDbServices below</li>
 * </ul>
 * 
 * Optional configuration:
 * <ul>
 * <li>Script report parameter chartHeight || requires an integer</li>
 * <li>Script report parameter chartWidth || requires an integer</li>
 * </ul>
 *
 * Output for report parameter's value ('File Server'):
 * https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/radialtree/radialtree-output.png
 * HTML output for report parameter's value ('File Server'):
 * https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/radialtree/radialtree-html-output.html
 */

Map allTreeDbServices       =[ 'Dashboard Manager':     GLOBALS.getInstance(DashboardManagerService),
                               'Dashboard':             GLOBALS.getInstance(DashboardService),
                               'Datasink':              GLOBALS.getInstance(DatasinkTreeService),
                               'Datasource':            GLOBALS.getInstance(DatasourceService),
                               'File Server':           GLOBALS.getInstance(FileServerService),
                               'Report':                GLOBALS.getInstance(ReportService),
                               'Ts Disk':               GLOBALS.getInstance(TsDiskService),
                               'User Manager':          GLOBALS.getInstance(UserManagerService)]

/* --------config---------*/
int chartHeight = parameterMap.chartHeight? parameterMap.chartHeight as Integer : 1000 //default value
int chartWidth = parameterMap.chartWidth? parameterMap.chartWidth as Integer : 1800 //default value


/* --------script---------*/
def choosenTreeDbService = parameterMap['treeDbService']
assert choosenTreeDbService && allTreeDbServices.keySet().contains(choosenTreeDbService) : "Invalid text paramenter value treeDbService: ${parameterMap}"

Map completeTree = [:]
completeTree.name = choosenTreeDbService
completeTree.children = addChildrenToMap(allTreeDbServices[choosenTreeDbService].roots[0].children)
def treeAsJson = new JsonBuilder(completeTree).toPrettyString()
                           
"""
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title></title>
    
  </head>
  <body>
    <script>
      // Copyright 2022 Observable, Inc.
      // Released under the ISC license.
      // https://observablehq.com/@d3/radial-tree
      function Tree(data, { // data is either tabular (array of objects) or hierarchy (nested objects)
        path, // as an alternative to id and parentId, returns an array identifier, imputing internal nodes
        id = Array.isArray(data) ? d => d.id : null, // if tabular data, given a d in data, returns a unique identifier (string)
        parentId = Array.isArray(data) ? d => d.parentId : null, // if tabular data, given a node d, returns its parent’s identifier
        children, // if hierarchical data, given a d in data, returns its children
        tree = d3.tree, // layout algorithm (typically d3.tree or d3.cluster)
        separation = tree === d3.tree ? (a, b) => (a.parent == b.parent ? 1 : 2) / a.depth : (a, b) => a.parent == b.parent ? 1 : 2,
        sort, // how to sort nodes prior to layout (e.g., (a, b) => d3.descending(a.height, b.height))
        label, // given a node d, returns the display name
        title, // given a node d, returns its hover text
        link, // given a node d, its link (if any)
        linkTarget = "_blank", // the target attribute for links (if any)
        width = 640, // outer width, in pixels
        height = 400, // outer height, in pixels
        margin = 60, // shorthand for margins
        marginTop = margin, // top margin, in pixels
        marginRight = margin, // right margin, in pixels
        marginBottom = margin, // bottom margin, in pixels
        marginLeft = margin, // left margin, in pixels
        radius = Math.min(width - marginLeft - marginRight, height - marginTop - marginBottom) / 2, // outer radius
        r = 3, // radius of nodes
        padding = 1, // horizontal padding for first and last column
        fill = "#999", // fill for nodes
        fillOpacity, // fill opacity for nodes
        stroke = "#555", // stroke for links
        strokeWidth = 1.5, // stroke width for links
        strokeOpacity = 0.4, // stroke opacity for links
        strokeLinejoin, // stroke line join for links
        strokeLinecap, // stroke line cap for links
        halo = "#fff", // color of label halo 
        haloWidth = 3, // padding around the labels
      } = {}) {
        
        // If id and parentId options are specified, or the path option, use d3.stratify
        // to convert tabular data to a hierarchy; otherwise we assume that the data is
        // specified as an object {children} with nested objects (a.k.a. the “flare.json”
        // format), and use d3.hierarchy.
        const root = path != null ? d3.stratify().path(path)(data)
            : id != null || parentId != null ? d3.stratify().id(id).parentId(parentId)(data)
            : d3.hierarchy(data, children);
      
        // Sort the nodes.
        if (sort != null) root.sort(sort);
      
        // Compute labels and titles.
        const descendants = root.descendants();
        const L = label == null ? null : descendants.map(d => label(d.data, d));
      
        // Compute the layout.
        tree().size([2 * Math.PI, radius]).separation(separation)(root);
      
        const svg = d3.create("svg")
            .attr("viewBox", [-marginLeft - radius, -marginTop - radius, width, height])
            .attr("width", width)
            .attr("height", height)
            .attr("style", "max-width: 100%; height: auto; height: intrinsic;")
            .attr("font-family", "sans-serif")
            .attr("font-size", 10);
      
        svg.append("g")
            .attr("fill", "none")
            .attr("stroke", stroke)
            .attr("stroke-opacity", strokeOpacity)
            .attr("stroke-linecap", strokeLinecap)
            .attr("stroke-linejoin", strokeLinejoin)
            .attr("stroke-width", strokeWidth)
          .selectAll("path")
          .data(root.links())
          .join("path")
            .attr("d", d3.linkRadial()
                .angle(d => d.x)
                .radius(d => d.y));
      
        const node = svg.append("g")
          .selectAll("a")
          .data(root.descendants())
          .join("a")            
            .attr("target", link == null ? null : linkTarget)
            .attr("transform", d => `rotate(\${d.x * 180 / Math.PI - 90}) translate(\${d.y},0)`);
      
        node.append("circle")
            .attr("fill", d => d.children ? stroke : fill)
            .attr("r", r);
      
        if (title != null) node.append("title")
            .text(d => title(d.data, d));
      
        if (L) node.append("text")
            .attr("transform", d => `rotate(\${d.x >= Math.PI ? 180 : 0})`)
            .attr("dy", "0.32em")
            .attr("x", d => d.x < Math.PI === !d.children ? 6 : -6)
            .attr("text-anchor", d => d.x < Math.PI === !d.children ? "start" : "end")
            .attr("paint-order", "stroke")
            .attr("stroke", halo)
            .attr("stroke-width", haloWidth)
            .text((d, i) => L[i]);
      
        return svg.node();
      }
   </script>
    <script src="https://d3js.org/d3.v7.min.js"></script>
    <script type="text/javascript">
    var svg = Tree(${treeAsJson}, {
          label: d => d.name,
          title: (d, n) => `\${n.ancestors().reverse().map(d => d.data.name).join(".")}`, // hover text
          width: ${chartWidth},
          height: ${chartHeight},
          margin: 100
        });
    document.body.appendChild(svg);
    </script>
  </body>
</html>

"""

def addChildrenToMap(List<AbstractFileServerNode> children) {
  def childmaps = []
  children
    .each{
       if(it.children.isEmpty()) {
          childmaps.add(['name' : it.name])
       } else {
          childmaps.add(['name' : it.name, 'children' : addChildrenToMap(it.children)])
       }
    }
  return childmaps
}

