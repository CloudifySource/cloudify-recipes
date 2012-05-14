#
# Cookbook Name:: pet-clinic
# Recipe:: default
#
# Copyright 2012, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#

include_recipe "pet-clinic::common"
include_recipe "tomcat"

remote_file "/opt/petclinic/petclinic.war" do
  #source "http://repository.cloudifysource.org/org/cloudifysource/2.0.0/petclinic-mongo-example.war"
  source "http://appspring.googlecode.com/files/PetClinic.war"
  checksum "f9f40dcc9dc75b60dc2e85bd379f0df44b2e73617743496068fcac7d1b1ef6d4"
  mode "0644"
  action :create
end

mysql_host = search(:node, "role:mysql").first.ipaddress
template ::File.join(node["tomcat"]["config_dir"], "Catalina", "localhost", "petclinic.xml") do
  mode "0644"
  variables :mysql_host => mysql_host, :mysql_password => "test", :mysql_user => "petclinic"
  notifies :restart, "service[tomcat]"
end
