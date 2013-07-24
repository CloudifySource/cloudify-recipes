#
# Author:: Adam Jacob <adam@opscode.com>
# Cookbook Name:: unicorn
# Definition:: unicorn_config
#
# Copyright 2009, Opscode, Inc.
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

define :unicorn_config, 
    :listen               => nil, 
    :working_directory    => nil,
    :worker_timeout       => 60, 
    :preload_app          => false, 
    :worker_processes     => 4,
    :unicorn_command_line => nil, 
    :forked_user          => nil, 
    :forked_group         => nil, 
    :pid                  => nil,
    :before_exec          => nil,
    :before_fork          => nil, 
    :after_fork           => nil, 
    :stderr_path          => nil,
    :stdout_path          => nil, 
    :notifies             => nil, 
    :owner                => nil, 
    :group                => nil,
    :mode                 => nil, 
    :copy_on_write        => false, 
    :enable_stats         => false do

  config_dir = File.dirname(params[:name])

  directory config_dir do
    recursive true
    action :create
  end

  tvars = params.clone
  params[:listen].each do |port, options|
    oarray = Array.new
    options.each do |k, v|
      oarray << ":#{k} => #{v}"
    end
    tvars[:listen][port] = oarray.join(", ")
  end
  
  template params[:name] do
    source "unicorn.rb.erb"
    cookbook "unicorn"
    mode "0644"
    owner params[:owner] if params[:owner]
    group params[:group] if params[:group]
    mode params[:mode]   if params[:mode]
    variables params
    notifies *params[:notifies] if params[:notifies]
  end
  
  # If the user set a group for forked processes but not a user, warn them that
  # we did not set the group. Unicorn does not allow you to drop privileges at
  # the group level only.
  ruby_block "warn-group-no-user" do
    only_if { params[:forked_user].nil? and !params[:forked_group].nil? }
    block do
      Chef::Log.warn "Unable to set the Unicorn 'forked_group' because a "\
        "forked_user' was not specified! Unicorn will be run as root! Please "\
        "see the Unicorn documentation regarding `user` for proper usage."
    end
  end
end
