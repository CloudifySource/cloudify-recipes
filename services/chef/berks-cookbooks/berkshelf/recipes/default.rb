#
# Cookbook Name:: berkshelf
# Recipe:: default
#
# Copyright 2013, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#
node.set[:languages][:ruby][:default_version] = "1.9.1"
node.set['build_essential']['compiletime'] = true
include_recipe "build-essential"
include_recipe "xml::ruby"
include_recipe "gecode"
chef_gem "berkshelf"
package "git"
