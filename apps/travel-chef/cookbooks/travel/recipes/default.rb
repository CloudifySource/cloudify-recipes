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

directory node["travel"]["war_directory"] do
  mode "0755"
end

tarball = ::File.join(node["travel"]["war_directory"], node["travel"]["war_name"])
webapp_dir = ::File.join(node["tomcat"]["webapp_dir"], "travel")

remote_file tarball  do
  #source "http://repository.cloudifysource.org/org/cloudifysource/2.0.0/travel-mongo-example.war"
  source node["travel"]["war_url"]
  checksum node["travel"]["war_checksum"]
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
  variables :mysql_host => mysql_host,
            :mysql_password => node["travel"]["db_pass"],
            :mysql_user => node["travel"]["db_user"]
  notifies :restart, "service[tomcat]"
end
