#Storm Nimbus recipe notes:

**Status**: Tested
**Description**: Storm Nimbus
**Maintainer**: Cloudify
**Contributors**: [dfilppi] https://github.com/dfilppi
**Homepage**: [http://www.cloudifysource.org](http://www.cloudifysource.org)
**Demo**:
**License**: Apache 2.0
**Linux* sudoer permissions**: Required
**Windows* admin permissions**: NA
**Release Date**: Work In Progress

Tested on:
-----------

* <strong>EC2</strong>
 *<strong>Amazon Linux</strong> imageId "us-east-1/ami-76f0061f", hardwareId "m1.small", locationId "us-east-1"
* <strong>HPCS/Openstack</strong>
 *<strong>Centos 5.6</strong> imageId "20111207", hardwareId "standard.medium", locationId "110"

Synopsis:
--------

This folder contains a Storm 0.8.1 Nimbus service recipe.

The Storm Nimbus is by its nature a single instance (not elastic).

It requires a running Zookeeper cluster to manage state.  Zookeeper is used to store state and communicate with Storm Supervisor nodes.

Storm depends on ZeroMQ 2.1.7 and JZMQ (a java language binding to ZeroMQ).  These are platform specific libraries and are built during the install phase.  Because of this, the recipe uses Yum to install gcc and autoconf, and builds the libraries.  Obviously this creates a dependency on Yum. NOTE: It is recommended that a production version of this recipe would simply have prebuilt binary versions of these libraries in a blob store.

Monitored by a custom plugin that uses a Thrift interface for getting cluster info.

Exposes a custom command (wordcount-start) to run the Storm-starter word count demo.

Known limitations:
---------------

* No details plugin
* Uses defaults for almost everything, including storm.local.dir.  Not optimal.
* Many things hardcoded that should be in properties.
* No Windows support
