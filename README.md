# ReportServer Scripting Samples

ReportServer is a modern and versatile open source business intelligence (BI) platform with powerful reporting features.

With ReportServer you are not limited to one provider's solutions. ReportServer integrates Jasper, Birt, Mondrian and Excel-based reporting: choose what best suits your needs!

The example scripts have one script type. The specific type is available in the script's header comment:
* Normal Script: scripts that can be directly executed via "exec script.groovy" or "exec -c script.groovy", depending if you want to commit changes to the database or not. Details can be found here: https://reportserver.net/en/tutorials/tutorial-scripting/. Note that these scripts can also be scheduled directly. Details here: https://reportserver.net/en/guides/script/chapters/Scheduling-Scripts/ 
* Script report: can be used for generating reports via script. Details can be found here: https://reportserver.net/en/guides/script/chapters/Script-Reporting/
* Script datasource: can be used for producing datasources and show any kind of data via script. Details can be found here: https://reportserver.net/en/guides/script/chapters/Script-Datasources/

Note that script reports and script datasources can be scheduled via report scheduling. Details here: https://reportserver.net/en/guides/user/chapters/Scheduling/ and https://reportserver.net/en/guides/admin/chapters/Scheduling-of-Reports/

ReportServer: https://reportserver.net

Documentation: https://reportserver.net/en/documentation
Scripting documentation: https://reportserver.net/en/guides/script/main/

ReportServer Source code: https://github.com/infofabrik/reportserver

Bitnami Builds: https://bitnami.com/stack/reportserver and https://bitnami.com/stack/reportserver-enterprise
