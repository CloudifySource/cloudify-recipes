#
# Cookbook Name:: cloudify-tester
# Recipe:: default
#
# Copyright 2012, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#

include_recipe "jruby"

%w{ cucumber cucumber-nagios webrat jruby-openssl jdbc-mysql jmx4r }.each do |gem|
  jruby_gem gem
end

directory node['cloudify-tester']['cucumber_dir'] do
  owner "ubuntu"
  group "ubuntu"
  mode "0777"
  recursive true
end
