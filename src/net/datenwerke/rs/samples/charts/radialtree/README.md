# Visualize ReportServer Datastructures with Radial Tree and ScriptReports

With the Script [radialTree.groovy](https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/radialtree/radialTree.groovy)
you are able to visualize the underlying tree structure of the following services:
* DashboardManagerService
* DashboardService
* DatasinkTreeService
* DatasourceService
* FileServerService
* ReportService
* TsDiskService
* UserManagerService

## Preview
![An example file server](https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/radialtree/radialtree-output.png)

## Installing Radial Tree as ScriptReport

1. Download and import [radialTree.groovy](https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/radialtree/radialTree.groovy)
into your ReportServer filesystem
2. Create a new ScriptReport and set the downloaded groovy file as 'Script' source
3. Configure parameters: the script has the following parameters:
    * mandatory: **treeDbService**
    * optional: *chartHeight* 
    * optional: *chartWidth* 
### Parameter treeDbService
For ease of configuration you may configure the key **treeDbService** as an ScriptParamter with [treeDbServiceSelector_scriptParameter.html](https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/radialtree/treeDbServiceSelector_scriptParameter.html)
as its source: 

![image](https://user-images.githubusercontent.com/65605180/183521582-47fc0656-ade6-46db-9d80-aabd49970c7d.png)
![image](https://user-images.githubusercontent.com/65605180/183250885-d349571a-f979-42cc-a1dd-6e08685b13a6.png)

This will allow you to choose from a drop down list the item you wish to select:

![image](https://user-images.githubusercontent.com/65605180/183521818-c5eefc2f-ad55-48c0-9a79-23e52d8f2867.png)


### Parameter chartHeight and chartWidth

Both of these parameters are optional parameters. There are default values in place if you choose to not use them. They declare the resulting dimensions of 
the computed tree in pixels. 

They can be set as text-parameter. If set the script will convert the input string to an integer. In case the conversion fails the execution is stopped.

## F.A.Q

### My image looks squished together
E.g

![image](https://user-images.githubusercontent.com/65605180/183521928-8a6c2c6f-88d4-4666-a55d-8d98cea05a2d.png)
![image](https://user-images.githubusercontent.com/65605180/183251363-95b3c846-df25-42d6-ba55-5503f5590565.png)

This is a result from having the dimensions set too low. Long names may also cause this.

Fix: increase chart width and height

![image](https://user-images.githubusercontent.com/65605180/183522001-b34ec1ff-41a4-437b-a033-ba11ed049629.png)
![image](https://user-images.githubusercontent.com/65605180/183251449-90b1ea6a-017d-4281-90a7-25b5259cc0db.png)
