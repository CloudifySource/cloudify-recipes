# WebSphere 

**Status**: Tested  
**Description**: WebSphere  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Demo**: [cloudifysourcetv](http://www.cloudifysource.org/cloudifysourcetv#K3p-wNhC9gA)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**: [Cloudify 2.2.0-rc b2496](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-rc-b2496.zip)  
**Linux* sudoer permissions**:	Not required  
**Release Date**: Oct 17th 2012  


Tested on:
--------

* <strong>EC2</strong>: CentOs 




Synopsis
--------

This folder contains a service recipe for WebSphere.

This recipe installs two WebSphere sample applications (Plants and AlbumCatalog).
 
You can set quite a few properties in websphere-service.properties:

version : WebSphere version
 e.g. : 7.0.0.11

gzipName : The .gz file that contains the WebSphere installation (just the name without the path) 
   e.g. : was.cd.70011.trial.base.opt.linux.ia32.tar.gz */

gzipDownloadPath : The folder in which $gzipName is located 
applicationName : The name of your application (which is in a WAR file) 

applicationContextRoot : The application's context root (without host and port)
  
startingPort: The 1st port that the installation will use

And more ...


Jacl Scripts
----------------

We also created three [Jacl](http://pic.dhe.ibm.com/infocenter/wasinfo/v7r0/index.jsp?topic=%2Fcom.ibm.websphere.express.doc%2Finfo%2Fexp%2Fae%2Fcxml_jacl.html) scripts (installWasApplication.jacl, startWasApplication.jacl and uninstallWasApplication.jacl) that enable users to manipulate on WebSphere Objects.


Links
----------------
Here are the links (which can also be found in the WebSphere service panel in Cloudify WEB UI once the recipe is deployed) :
 
WebSphere admin Console (user:admin,password:admin):
https://WEBSPHERE_IP_ADDRESS:8084/ibm/console
 
Websphere Plants Sample application : 
http://WEBSPHERE_IP_ADDRESS:8081/PlantsByWebSphere/
 
Websphere AlbumCatalog Sample application : 
http://WEBSPHERE_IP_ADDRESS:8081/AlbumCatalogWeb/AlbumCatalog.jsp
 
