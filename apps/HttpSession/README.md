# Hello world 

**Status**: Tested  
**Description**: HttpSession
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [yoramw](https://github.com/yoramw)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**: Cloudify 2.7 GA
**Linux* sudoer permissions**:	Required  
**Windows* Admin permissions**:	NA  
**Release Date**: February 5th 2014    


Tested on:
--------

* <strong>EC2</strong>: 
 * <strong>Amazon Linux</strong> imageId "us-east-1/ami-1624987f", hardwareId "m1.small", locationId "us-east-1"  


Synopsis
--------

This recipe show a highly available web service example using 2 tomcat servers behind an apache loadbalancer with distributed session manager.

The default behavior is that the session managment will be done by the internal Cloudify in memory attribute store as the session store.

Two other operation modes are:

1. Uncomment the XAP components from the applicaition groovy (mgt, pu & webui in the HttpSession-application.groovy)

2. Point to an external XAP 9 as a session store by setting the global attribute "SPACE_URL" before the HttpSession deployment.


The sample application is a very simple servlet which let you set key value pairs and keep the previous entries in addition to the newly added pair. The servelt also provides the IP address of the tomcat currently serving the request behind the load balancer.
You can test the high availability by taking down the tomcat which was serving the requests and see that the session continues with the prior values in the second instance of the tomcat (It take the loadbalancer ~30 seconds to detect the failure. during that time, the request will be in a wait state)

In parallel, Cloudify will make sure a new tomcat server is added in order to get back to the sla of 2 tomcats per the deploymet.


