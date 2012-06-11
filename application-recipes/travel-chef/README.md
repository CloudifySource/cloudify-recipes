# The Spring Travel application 

This application is composed of a Chef-server installation that loads the Chef with the relevant cookbooks once it's installed, and from a mysql and tomcat services. 
The recipes use Chef for installation and then define Cloudify specific monitoring and auto scaling on top of it. 

> *Important*: Currently the Chef recipes have only been tested on an Ubuntu environment on Amazon EC2. Please make sure to use the [EC2 ubuntu cloud driver](https://github.com/CloudifySource/cloudify-cloud-drivers/tree/master/ec2-ubuntu) when installing this application

