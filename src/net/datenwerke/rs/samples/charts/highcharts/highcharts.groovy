package net.datenwerke.rs.samples.charts.highcharts
import groovy.sql.Sql

/**
 * highcharts.groovy
 * Version: 1.0.2
 * Type: Script report
 * Last tested with: ReportServer 4.6.1-6053
 * Shows demo data (orders) for a given year grouped by quarters as Highcharts bar chart. 
 * Include average number of orders.
 * 
 * Necessary for installation:
 * <ul>
 * <li>ReportServer Demo Data</li>
 * <li>Script report containing text parameter "year"</li>
 * <li>The script report's datasource should be "demo"</li>
 * </ul>
 * 
 * Output for report parameter's value (year) 2022:
 * https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/highcharts/highcharts-output-2022.png
 * HTML output for report parameter's value (year) 2022: 
 * https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/highcharts/highcharts-html-output-2022.html
 */

/* get sql object of selected "demo" datasource */
def sql = new Sql(connection)

/* get number of resolved orders for given year */
def resolvedOrders = []
sql.eachRow("""SELECT Q, COUNT(OD_ORDERNUMBER) ORDERS FROM (
    SELECT *, 
          CASE  WHEN MONTH(OR_ORDERDATE) < 4 THEN 1
                WHEN MONTH(OR_ORDERDATE) < 7 THEN 2 
                WHEN MONTH(OR_ORDERDATE) < 10 THEN 3 
                ELSE 4
          END as Q
    FROM T_AGG_ORDER WHERE OR_STATUS <> 'Cancelled' AND YEAR(OR_ORDERDATE) = ?
    ) GROUP BY Q ORDER BY Q""", [parameterMap['year']]){ row ->
         resolvedOrders.add(row['ORDERS'])
      };

/* orders by productline */
def ordersByProductline = [:]
sql.eachRow("""SELECT PRO_PRODUCTLINE, Q, COUNT(OD_ORDERNUMBER) ORDERS FROM (
    SELECT *, 
          CASE  WHEN MONTH(OR_ORDERDATE) < 4 THEN 1
                WHEN MONTH(OR_ORDERDATE) < 7 THEN 2 
                WHEN MONTH(OR_ORDERDATE) < 10 THEN 3 
                ELSE 4
          END as Q
    FROM T_AGG_ORDER WHERE OR_STATUS <> 'Cancelled' AND YEAR(OR_ORDERDATE) = ?
    ) GROUP BY PRO_PRODUCTLINE, Q ORDER BY PRO_PRODUCTLINE, Q""", [parameterMap['year']]){ row ->
         if(null == ordersByProductline[row['PRO_PRODUCTLINE']])
            ordersByProductline[row['PRO_PRODUCTLINE']]= [row['ORDERS']]
         else
            ordersByProductline[row['PRO_PRODUCTLINE']].add(row['ORDERS'])
      };

/* scale resolved orders */
def averageOrders = resolvedOrders.collect{ resolvedOrder -> return resolvedOrder / ordersByProductline.size() }

/* prepare HTML output for script report */
"""
<!DOCTYPE html>
<html>
    <head>
      <script
        src="https://code.jquery.com/jquery-3.6.0.min.js"
        integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4="
        crossorigin="anonymous"></script>
      
      <!-- Note that you can also download the Highcharts javascript file into your ReportServer fileserver 
         and include it with: 
         <script src="reportserver/fileServerAccess/resources/js/highcharts.js" type="text/javascript"></script> 
         The file has to have the web access option selected.     
      -->
      <script 
        src="https://code.highcharts.com/9.0.1/highcharts.js" 
        crossorigin="anonymous"></script>    
      
      <script>
        var url = ""

        \$(document).ready(function () {
          \$('#container').highcharts({
              title: {
                  text: 'Orders per Quarter'
              },
              xAxis: {
                  categories: ['Q1', 'Q2', 'Q3', 'Q4' ]
              },
              yAxis : {
                  title: {text:  'Number of orders'}
              },
              series: [""" +
            ordersByProductline.collect{ k, v ->
               return """{
                       type: 'column',
                       name: '${k}',
                       data: [""" + v.join(",") + "]}";
            }.join(',') + """
                , {
                  type: 'spline',
                  name: 'Average Nr. of Orders',
                  data: [""" + averageOrders.join(',') + """],
                  marker: {
                      lineWidth: 2,
                      lineColor: Highcharts.getOptions().colors[3],
                      fillColor: 'white'
                  }
              }
              ]
          });
      });
      </script>
    </head>
    <body>
      <div id="container" style="width:100%; height: 435px;"></div>      
      
    </body>
</html>"""
