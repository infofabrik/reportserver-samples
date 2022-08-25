# Visualize ReportServer Datastructures with Radial Tree and ScriptReports

With the Script [radialTree.groovy](https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/radialtree/radialTree.groovy)
you are able to visualize the underlying tree structure of the following entities:
* Dashboards
* Datasinks
* Datasources
* Files
* Reports
* Teamspaces
* Users

## Preview
![An example file server](https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/radialtree/radialtree-output.png)

## Installing Radial Tree as ScriptReport

1. Download and import [radialTree.groovy](https://github.com/infofabrik/reportserver-samples/blob/main/src/net/datenwerke/rs/samples/charts/radialtree/radialTree.groovy)
into your ReportServer filesystem. Remember scripts have to be placed inside of your bin directory.
2. Create a new ScriptReport and set the downloaded groovy file as 'Script' source
3. Configure parameters: the script has the following parameters:
    * mandatory: **treeDbService**
    * optional: *chartHeight* 
    * optional: *chartWidth* 
### Parameter treeDbService
For ease of configuration you may configure the key **treeDbService** as a Datasource parameter.
![image](https://user-images.githubusercontent.com/65605180/186396964-e37e92f5-7905-4899-a894-f24972b97de7.png)


For its source use a CSV List datasource with the connector: *Arguement Connector*
![image](https://user-images.githubusercontent.com/65605180/186397830-d5f767e4-0ec0-41ee-a5a7-cc570a385f9d.png)

Fill the parameter data with the following:
```
VALUE;DISPLAY
Dashboards;Dashboards
Datasinks;Datasinks
Datasources;Datasources
Files;Files
Reports;Reports
Teamspaces;Teamspaces
Users;Users
Everything;Everything
```
![image](https://user-images.githubusercontent.com/65605180/186398550-6a780d8e-dd08-4a69-b5c2-65cfab43248c.png)

Finally configure the *Selection mode*, *Selection style* and *Datatype* as the following:
![image](https://user-images.githubusercontent.com/65605180/186398860-c5112a5b-1567-4c35-aa36-88fb4679e6e5.png)




This will allow you to choose from a drop down list the item you wish to select:

![image](https://user-images.githubusercontent.com/65605180/186399121-4a06a753-d139-45f9-8772-5b0537cc98c6.png)


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
