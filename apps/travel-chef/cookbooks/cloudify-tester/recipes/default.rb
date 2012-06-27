#
# Cookbook Name:: cloudify-tester
# Recipe:: default
#
# Copyright 2012, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#

include_recipe "jruby"

%w{ cucumber cucumber-nagios jruby-openssl jdbc-mysql jmx4r }.each do |gem|
  jruby_gem gem
end

#we're going to hack webrat to fix a still open bug in the redirect
jruby_gem "webrat" do
  version "0.7.2"
end

cookbook_file ::File.join(node['jruby']['install_path'], "lib/ruby/gems/1.8/gems/webrat-0.7.2/lib/webrat/core/session.rb") do
  source "webrat-session.rb"
  mode "0644"
end

cookbook_file ::File.join(node['jruby']['install_path'], "lib/ruby/gems/1.8/gems/webrat-0.7.2/lib/webrat/core/elements/link.rb") do
  source "webrat-link.rb"
  mode "0644"
end

#deploy the cucumber features
directory node['cloudify-tester']['cucumber_dir'] do
  owner "ubuntu"
  group "ubuntu"
  mode "0777"
  recursive true
end
