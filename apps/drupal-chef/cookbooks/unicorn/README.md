Description
===========

Installs and configures unicorn, and provides a definition to manage
configuration file for Rack apps running under unicorn.

Requirements
============

Should work anywhere.

Definitions
===========

unicorn\_config
---------------

The unicorn\_config definition manages the configuration template for
an application running under unicorn.

### Parameters:

* `listen` - Default is nil.
* `working_directory` - Default is nil.
* `worker_timeout` - Default is 60.
* `preload_app` - Default is false.
* `worker_processes` - Number of worker processes to spawn. Default is
  4.
* `unicorn_command_line` - If set, specifies the unicorn commandline to set
  in the config file.  Usefull when sandboxing your unicorn installation.
* `forked_user` - User to run children as. Default is nil.
* `forked_group` - Group to run children as. You *must* specify a `forked_user`
  as well to use this attribute. Default is nil.
* `before_exec` - Default is nil.
* `before_fork` - Default is nil.
* `after_fork` - Default is nil.
* `pid` - Pidfile location. Default is nil.
* `stderr_path` - Where stderr gets logged. Default is nil.
* `stdout_path` - Where stdout gets logged. Default is nil.
* `notifies` - How to notify another service if specified. Default is nil.
* `owner` - Owner of the template. Default is nil.
* `group` - group of the template. Default is nil.
* `mode` - mode of the template. Default is nil.
* `unicorn_command_line` - Specify the command-line for the unicorn
  binary as a string. Populates `Unicorn::HttpServer::START_CTX[0]`.
  Default is nil.
* `copy_on_write` - Whether the app should take advantage of REE Copy
  On Write feature. Default is false.
* `enable_stats` - Whether the app should have GC profiling enabled
  for instrumentation. Default is false.

For more information on `copy_on_write` and `enable_stats`, see:

* http://www.rubyenterpriseedition.com/faq.html#adapt_apps_for_cow
* https://newrelic.com/docs/ruby/ruby-gc-instrumentation

Respectively.

### Examples:

Setting some custom attributes in a recipe (this is from Opscode's
`application::unicorn`.

    node.default[:unicorn][:worker_timeout] = 60
    node.default[:unicorn][:preload_app] = false
    node.default[:unicorn][:worker_processes] = [node[:cpu][:total].to_i * 4, 8].min
    node.default[:unicorn][:preload_app] = false
    node.default[:unicorn][:before_fork] = 'sleep 1'
    node.default[:unicorn][:port] = '8080'
    node.set[:unicorn][:options] = { :tcp_nodelay => true, :backlog => 100 }

    unicorn_config "/etc/unicorn/#{app['id']}.rb" do
      listen({ node[:unicorn][:port] => node[:unicorn][:options] })
      working_directory ::File.join(app['deploy_to'], 'current')
      worker_timeout node[:unicorn][:worker_timeout]
      preload_app node[:unicorn][:preload_app]
      worker_processes node[:unicorn][:worker_processes]
      before_fork node[:unicorn][:before_fork]
    end

License and Author
==================

- Author: Adam Jacob <adam@opscode.com>

- Copyright 2009-2013, Opscode, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
