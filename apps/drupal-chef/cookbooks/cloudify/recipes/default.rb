include_recipe "ohai"
include_recipe "chef_handler"

cookbook_file ::File.join(node["chef_handler"]["handler_path"], "cloudify.rb") do
  source "handlers/cloudify.rb"
end

chef_handler "Cloudify::ChefHandlers::AttributesDumpHandler" do
  source ::File.join(node['chef_handler']['handler_path'], "cloudify.rb")
  action :enable
end

