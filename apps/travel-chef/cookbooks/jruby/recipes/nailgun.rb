#
# Cookbook Name:: jruby
# Recipe:: nailgun
#
# Copyright 2012, Intelie
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

execute "configure nailgun" do
  command "./configure"
  cwd File.join(prefix, "tool/nailgun")
  creates File.join(prefix, "tool/nailgun/Makefile")
end

execute "build nailgun" do
  command "make"
  cwd File.join(prefix, "tool/nailgun")
  creates File.join(prefix, "tool/nailgun/ng")
end
