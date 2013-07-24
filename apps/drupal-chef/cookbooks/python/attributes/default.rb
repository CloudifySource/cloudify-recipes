#
# Author:: Seth Chisamore (<schisamo@opscode.com>)
# Cookbook Name:: python
# Attribute:: default
#
# Copyright 2011, Opscode, Inc.
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

default['python']['install_method'] = 'package'

if python['install_method'] == 'package'
  case platform
  when "smartos"
    default['python']['prefix_dir']         = '/opt/local'
  else
    default['python']['prefix_dir']         = '/usr'
  end
else
  default['python']['prefix_dir']         = '/usr/local'
end

default['python']['binary'] = "#{python['prefix_dir']}/bin/python"

default['python']['url'] = 'http://www.python.org/ftp/python'
default['python']['version'] = '2.7.5'
default['python']['checksum'] = '3b477554864e616a041ee4d7cef9849751770bc7c39adaf78a94ea145c488059'
default['python']['configure_options'] = %W{--prefix=#{python['prefix_dir']}}

default['python']['distribute_script_url'] = 'http://python-distribute.org/distribute_setup.py'
default['python']['distribute_option']['download_base'] = 'https://pypi.python.org/packages/source/d/distribute/'
