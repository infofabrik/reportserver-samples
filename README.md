# ReportServer Scripting and Template Samples

[ReportServer](https://reportserver.net) is a modern and versatile open source business intelligence (BI) platform with powerful reporting features.

With ReportServer you are not limited to one provider's solutions. ReportServer integrates Jasper, Birt, Mondrian and Excel-based reporting: choose what best suits your needs!

The example scripts have one script type. The specific type is available in the script's header comment. Note that you can of course use elements of different script types in another type. This was just added to clarify how to use the script examples without modification.
* Normal Script: can be directly executed via "exec script.groovy" or "exec -c script.groovy", depending if you want to commit changes to the database or not. Details can be found [here](https://reportserver.net/en/tutorials/tutorial-scripting/). Note that these scripts can be scheduled directly. More information on scheduling scripts on the [documentation](https://reportserver.net/en/guides/script/chapters/Scheduling-Scripts/). 
* Script report: can be used for generating reports via script. Details can be found [here](https://reportserver.net/en/guides/script/chapters/Script-Reporting/).
* Script datasource: can be used for producing datasources and showing any kind of data via scripts. Details can be found [here](https://reportserver.net/en/guides/script/chapters/Script-Datasources/). 
* Script datasink: can be used for sending reports/data to a custom datasink. 
* Hook: can be used for adapting ReportServer for your specific needs. Details can be found [here](https://reportserver.net/en/guides/script/chapters/Tapping-into-ReportServer/). 
* Library: can be imported into another scripts. Note that virtually all scripts can be used as a library. Details can be found [here](https://reportserver.net/en/guides/script/chapters/Nesting-Scripts-Calling-Scripts-from-Scripts/). 

Note that script reports and script datasources can be scheduled via report scheduling. Details [here](https://reportserver.net/en/guides/user/chapters/Scheduling/) and [here](https://reportserver.net/en/guides/admin/chapters/Scheduling-of-Reports/).

Further, you can find examples of [templates](https://reportserver.net/en/guides/user/chapters/Dynamic-Lists/#Templates) which can be used for data export of dynamic lists directly into Excel, Word (XDoc), Plain-Text (Velocity) or XML.

ReportServer Homepage: https://reportserver.net

Also refer to the [documentation](https://reportserver.net/en/guides/script/chapters/Script-Datasources/) and to the [scripting documentation](https://reportserver.net/en/guides/script/main/).

ReportServer source code: https://github.com/infofabrik/reportserver

Demo: https://reportserver.net/en/demo/

Forum: https://forum.reportserver.net

Bitnami Builds: https://bitnami.com/stack/reportserver
