#Zookeeper recipe notes:

**Status**: Tested
**Description**: Zookeeper
**Maintainer**: Cloudify
**Contributors**: [dfilppi] https://github.com/dfilppi
**Homepage**: [http://www.cloudifysource.org](http://www.cloudifysource.org)
**Demo**:
**License**: Apache 2.0
**Linux* sudoer permissions**: Not required
**Windows* admin permissions**: NA
**Release Date**: Work In Progress

Tested on:
-----------

* <strong>EC2</strong>
 *<strong>Amazon Linux</strong> imageId "us-east-1/ami-76f0061f", hardwareId "m1.small", locationId "us-east-1"
* <strong>HPCS/Openstack</strong>
 *<strong>Centos 5.6</strong> imageId "20111207", hardwareId "standard.medium", locationId "110"</strong>
* <strong>Windows 7 64</strong>
 *<strong>localcloud</strong>

Synopsis:
--------

This folder contains a Zookeeper 3.4.3 service recipe.

Due to its quorum leader election strategy, the number of instances must always be odd.  For high availability, the number of instances should be 3 or higher (3 is default).  By it's nature/design, Zookeeper is not elastic.  To tailor the daemon configuration, modify the templates/zoo.cfg file.


Known limitations:
---------------

* Uses the default transaction log directory
* Uses default log directory. No attempt to autoclean it.
* No details monitor/plugin.
* Data and transaction logs not compacted.  See: http://zookeeper.apache.org/doc/r3.3.3/zookeeperAdmin.html#Ongoing+Data+Directory+Cleanup

