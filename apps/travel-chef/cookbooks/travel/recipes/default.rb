#
# Cookbook Name:: travel
# Recipe:: default
#
# Copyright 2012, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#

include_recipe "travel::common"
include_recipe "tomcat"

directory "/opt/travel" do
  mode "0755"
end

tarball = "/opt/travel/travel.war"
webapp_dir = ::File.join(node["tomcat"]["webapp_dir"], "travel")

remote_file tarball  do
  #source "http://repository.cloudifysource.org/org/cloudifysource/2.0.0/travel-mongo-example.war"
  source "http://s3.amazonaws.com/gigaspaces-repository/org/cloudifysource/sample-apps/travel.war"
  checksum "a7325aa316663cd2b9c4bf8d964114b50da889216f83c6be9fcc2405ca837096"
  mode "0644"
  action :create
  notifies :run, "execute[unzip -o #{tarball} -d #{webapp_dir}]", :immediately
end

execute "unzip -o #{tarball} -d #{webapp_dir}" do
  notifies :restart, "service[tomcat]"
  action :nothing
end

mysql_host = search(:node, "role:mysql").first.ipaddress
template ::File.join(webapp_dir, "WEB-INF", "classes", "jdbc.properties") do
  mode "0644"
  variables :mysql_host => mysql_host, :mysql_password => "test", :mysql_user => "travel"
  notifies :restart, "service[tomcat]"
end
