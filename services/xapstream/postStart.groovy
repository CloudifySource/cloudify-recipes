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
import org.openspaces.admin.Admin
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.gsa.GridServiceAgents
import org.openspaces.admin.application.config.ApplicationConfig
import org.openspaces.admin.pu.config.ProcessingUnitConfig

//DEPLOY STREAMSPACE

admin=new AdminFactory().addLocators("127.0.0.1").create()
mgr=admin.getGridServiceManagers().waitForAtLeastOne()

pu=new ProcessingUnitConfig()
pu.setProcessingUnit("lib/streamspace-pu-1.0-SNAPSHOT.jar")

ac=new ApplicationConfig()
ac.setName("streamspace")
ac.addProcessingUnit(pu)
mgr.deploy(ac)
admin.close()
