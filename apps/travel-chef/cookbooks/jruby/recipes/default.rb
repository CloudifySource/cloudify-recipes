#
# Cookbook Name:: jruby
# Recipe:: default
#
# Copyright 2011, Heavy Water Software Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

include_recipe "java"

version = node[:jruby][:version]

prefix =  node[:jruby][:install_path]

file "/etc/profile.d/jruby.sh" do
  mode "0644"
  content "PATH=\$PATH:" + File.join(prefix, "bin")
  action :nothing
end

# install jruby
install_from_release('jruby') do
  release_url  "http://jruby.org.s3.amazonaws.com/downloads/#{version}/jruby-bin-#{version}.tar.gz"
  home_dir     prefix
  action       [:install, :install_binaries]
  version      version
  checksum node[:jruby][:checksum]
  has_binaries  %w(bin/jgem bin/jruby bin/jirb)
  not_if       { File.exists?(prefix) }
  notifies :create_if_missing, "file[/etc/profile.d/jruby.sh]"
end

if node[:jruby][:nailgun]
  include_recipe "jruby::nailgun"
end

# install all gems defined in the module
node[:jruby][:gems].each do |gem|
  if gem.is_a? Hash
    name = gem.delete(:name)
  else
    name = gem
    gem = nil
  end
  jruby_gem name, gem || {}
end
