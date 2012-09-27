# Vert.x

**Status**: **Under Construction**
**Description**:  Vert.x
**Maintainer**: GigaSpaces Technologies
**Maintainer email**: cloudifysource@gigaspaces.com
**Contributors**:  [Uri Cohen](https://github.com/uric)
**Homepage**: [http://www.cloudifysource.org](http://www.cloudifysource.org)
**License**: Apache 2.0
**Build**: [Cloudify 2.2.0 M4](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-m2-b2491.zip)
**Linux* sudoer permissions**:	Not required (unless using a port <=1024)
**Release Date**: September 13, 2012

Tested on:
--------
* <strong>EC2</strong>: CentOs, Ubuntu
* <strong>OpenStack</strong>: CentOs 

Synopsis
--------

This folder contains a service recipe for the Vert.x framework.

[Vert.x](https://vertx.io) is an event driven application framework that runs on top of the JVM. Vert.x apps can be written in Ruby, Java, Groovy, JavaScript, Coffeescript and Python (soon also Scala and Clojure). The recipe auto-installs Java7 and then the vertx distribution (the Java installation is local to the recipe and does not interfere with the default Java installation on the hosting server). Most of the relevant vertx parameters can be set in the ['vertx-service.properties'](vertx-service.properties) file. Please refer to this file for more details.
To learn about all the possible vertx configuration parameters please refer to the [vertx documentation](http://vertx.io/manual.html#interacting-with-vertx).

Using a Load Balancer
---------------------
The recipe also support fronting vertx with an Apache Load Balancer (see the [Apache Load Balancer](../apacheLB) recipe ).
To enable load balancer support, make sure to include the apacheLB service within the same application to which the vertx service belongs, and set the 'useLoadBalancer' properties in the ['vertx-service.properties'](vertx-service.properties) file to 'true'.

Auto Scaling
------------
The service is configured to auto-scale by instantiating a new VM with vertx if the average CPU utilization of at least one node in the cluster crosses 60 percent for more than 20 seconds.
See the 'scalingRules' section in the [vertx-service.groovy](vertx-service.groovy) file for the exact syntax used to configure this behavior.

Clustering
----------
The recipe supports clustering via the 'cluster' property. When set to true, the recipe will auto configure clustering. To control clustering parameters, edit the 'clusterConfig' section of the ['vertx-service.properties'](vertx-service.properties) file. To learn more about vertx clustering options please refer to the [vertx documentation](http://vertx.io/manual.html#configuring-clustering)


Custom Commands
---------------
TBD

	