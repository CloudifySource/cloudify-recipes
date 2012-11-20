#Storm Supervisor recipe notes:

**Status**: Tested
**Description**: Storm Supervisor
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
 *<strong>CentOs 5</strong> imageId "us-east-1/ami-76f0061f", hardwareId "m1.small", locationId "us-east-1"

Synopsis:
--------

This folder contains a Storm 0.8.1 Supervisor service recipe.

It requires a running Zookeeper cluster to manage state.  Zookeeper is used to store state and communicate with Storm Supervisor nodes.

Storm depends on ZeroMQ 2.1.7 and JZMQ (a java language binding to ZeroMQ).  These are platform specific libraries and are built during the install phase.  Because of this, the recipe uses Yum to install gcc and autoconf, and builds the libraries.  Obviously this creates a dependency on Yum.

Known limitations:
---------------

* No details or monitor plugin
* Uses defaults for almost everything, including storm.local.dir.  Not optimal.
* Many things hardcoded that should be in properties.
* No Windows support
