include_recipe "cloudify"
include_recipe "mysql::server"

cloudify_attribute "mysql_address" do
  scope :application
  value "#{node["ipaddress"]}:#{node["mysql"]["port"]}"
  action :nothing
  subscribes :set, "service[mysql]", :immediately
end

