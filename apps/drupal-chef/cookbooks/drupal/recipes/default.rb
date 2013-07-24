#
# Cookbook Name:: drupal
# Recipe:: default
#
# Copyright 2013, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#
include_recipe "apache2"
include_recipe "apache2::mod_rewrite"
include_recipe "apache2::mod_php5"
include_recipe "php"
include_recipe "php::module_mysql"
include_recipe "php::module_gd"

include_recipe "mysql::server"
include_recipe "mysql::ruby"

mysqlhost = search(:node, 'run_list:recipe\[mysql\:\:server\]').first.ipaddress

#connection_info = {:host => "#{mysqlhost}", 
#	:username => 'root', 
#	:password => node['mysql']['server_root_password']}
	
# remote_file "#{Chef::Config.file_cache_path}/drupal.tar.gz do
#  source node["drupal"]["tarball_url"]
#  mode 0644
# end

# execute "tar -xzf #{Chef::Config.file_cache_path}/drupal.tar.gz -C #{


ark "drupal" do
 url node["drupal"]["tarball_url"]
 checksum node["drupal"]["tarball_checksum"]
 version node["drupal"]["version"]
 action :install
end

%w(. default default/files).each do |dir| 
  directory ::File.join(node["drupal"]["dir"],"sites", dir) do
    mode "755"
    owner node["apache"]["user"] 
    group node["apache"]["group"]
  end
end 


template ::File.join(node["apache"]["dir"] , 
  "sites-available" , "drupal") do 
  mode "0644"
  source "apache.conf.erb"
  notifies :reload, "service[apache2]"
end 
 
apache_site "drupal" do 
 enable true
end 

apache_site "default" do
 enable false
end
