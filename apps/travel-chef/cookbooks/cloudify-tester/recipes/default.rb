#
# Cookbook Name:: cloudify-tester
# Recipe:: default
#
# Copyright 2012, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#

include_recipe "jruby"

%w{ cucumber cucumber-nagios webrat jdbc-mysql jmx4r }.each do |gem|
  jruby_gem gem
end

remote_directory node['cloudify-tester']['cucumber_dir'] do
  source "cucumber"
  files_owner "ubuntu"
  files_group "ubuntu"
  files_mode "0777"
  owner "ubuntu"
  group "ubuntu"
  mode "0777"
end
