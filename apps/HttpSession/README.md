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

Another operation mode is:
 Point to an external [XAP 9](http://www.gigaspaces.com/xap-download) as a session store by setting the global attribute "SPACE_URL" before the HttpSession deployment.
 
**set-attributes -scope global '{"SPACE_URL":"jini://x.x.x.x:####/*/mySpace"}'

The sample application is a very simple servlet which let you set key value pairs and keep the previous entries in addition to the newly added pair. The servelt also provides the IP address of the tomcat currently serving the request behind the load balancer.
You can test the high availability by taking down the tomcat which was serving the requests and see that the session continues with the prior values in the second instance of the tomcat (It take the loadbalancer ~30 seconds to detect the failure. during that time, the request will be in a wait state)


In parallel, Cloudify will make sure a new tomcat server is added in order to get back to the sla of 2 tomcats per the deploymet.

Demo script:

1. Install-Application HttpSession

2. Once deployed, goto to the ApacheLB service in the UI and click the two URLs (one will show the app, another will show the balancer management).

3. In the applicaiton URL you opened, you can set key value pairs (for example Joe:Doe, John: Key ...). Show that the key value pairs are maintained and the IP of the backend server that is deiplayed stays the same (sticky session)

4. Got to the AWS EC2 cosole and kill the tomcat server which its IP we saw in #3

5. Try to add another key value pair to the open applicaiton page we used in #3. Expect it to wait for 30 seconds (the time the load balancer takes to find out that the server is no longer available).

6. The application page should come back with a new IP and the key pairs we previously entered. This shows the session was maintaned even after the tomcat server went down.

7. show that Cloudify is recovering from the SLA breach by launching a new tomcat instance (from the Cloudify UI)


