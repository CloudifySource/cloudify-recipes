#
# Cookbook Name:: jruby
# Recipe:: jruby_gem
#
# Author:: Wes Morgan (<cap10morgan@gmail.com>)
#
# Stolen from the ree_gem definition in the ruby_enterprise cookbook
#

define :jruby_gem, :source => nil, :version => nil do
  gem_package params[:name] do
    gem_binary "#{node[:jruby][:install_path]}/bin/gem"
    source params[:source] if params[:source]
    version params[:version] if params[:version]
  end
end
