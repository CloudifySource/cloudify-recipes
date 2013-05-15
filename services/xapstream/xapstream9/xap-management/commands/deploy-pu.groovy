/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
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
import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.openspaces.admin.AdminFactory


context=ServiceContextFactory.serviceContext

puurl=context.attributes.thisInstance["deploy-pu-puurl"]
schema=context.attributes.thisInstance["deploy-pu-schema"]
partitions=context.attributes.thisInstance["deploy-pu-partitions"]
backups=context.attributes.thisInstance["deploy-pu-backups"]
maxpervm=context.attributes.thisInstance["deploy-pu-maxpervm"]
maxpermachine=context.attributes.thisInstance["deploy-pu-maxpermachine"]

//DEPLOY

// find xap management
mgmt_service=context.waitForService("xap-management",10,TimeUnit.SECONDS)
assert mgmt_service!=null
locators=""
mgmt_service.instances.each{locators+="${it.hostAddress},"}


// find gsm
admin=new AdminFactory().addLocators(locators).createAdmin();
gsm=admin.gridServiceManagers.waitForAtLeastOne(10,TimeUnit.SECONDS)
assert gsm!=null

// grab file

// deploy it
