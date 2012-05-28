/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import org.cloudifysource.dsl.context.ServiceContextFactory
import shell

def context = ServiceContextFactory.getServiceContext() 

bootstrap = ChefBootstrap.getBootstrap(installFlavor:"gem")
bootstrap.runSolo([
    "chef_server": [
        "server_url": "http://localhost:4000",
        "init_style": "runit"
    ],
    "run_list": ["recipe[build-essential]", "recipe[chef-server::rubygems-install]", "recipe[chef-server::apache-proxy]" ]
])


// eventually we will want to use a global attribute
context.attributes.thisApplication["chef_validation.pem"] = shell.sudoReadFile("/etc/chef/validation.pem")

