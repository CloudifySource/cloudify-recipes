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

// Mocks for running ChefBootstrap without cloudify runtime environment
// Note, this is not a full mock, it does not implement the original interface

package org.cloudifysource.utilitydomain.context

class Attributes {
    def thisInstance = [:]
}
class ServiceContext {
    def attributes = new Attributes()
    def getServiceDirectory() {
        return [
            System.properties["user.home"],
            "gigaspaces/work/processing-units/travel_chef-server_1/ext"
            ].join(File.separator)
    }
}

class ServiceContextFactory {
    def static getServiceContext() {
        return new ServiceContext()
    }
}
