Description
===========

Installs JRuby into `/usr/local/lib/jruby`

Binaries are linked to `/usr/local/bin/jruby`

/usr/local/bin/ is added to $PATH

Requirements
============

Platform:

* Debian, Ubuntu (tested on 10.04), CentOS (tested with 5.7)

The following Opscode cookbooks are dependencies:

* java

Attributes
==========

* `['jruby']['version']`
* `['jruby']['checksum']`
* `['jruby']['install_path']`
* `['jruby']['gems']` 
* `['jruby']['nailgun']` # disabled by default.

Usage
=====

include_recipe "jruby"

jruby-1.7.0-preview1
====================

default[:jruby][:version] = "1.7.0.preview1"
default[:jruby][:checksum] = "7b9e5e1cd0d818d0199086d948f948b4"
