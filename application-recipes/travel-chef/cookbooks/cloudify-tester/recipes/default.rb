#
# Cookbook Name:: cloudify-tester
# Recipe:: default
#
# Copyright 2012, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#

include_recipe "jruby"

jruby_gem "cucumber"

remote_directory "/home/ubuntu/cucumber" do
  source "cucumber"
  files_owner "ubuntu"
  files_group "ubuntu"
  files_mode "0777"
  owner "ubuntu"
  group "ubuntu"
  mode "0777"
end
