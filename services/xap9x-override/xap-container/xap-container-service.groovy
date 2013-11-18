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
import static com.gigaspaces.log.LogEntryMatchers.lastN
import static com.gigaspaces.log.LogEntryMatchers.continuous
import com.gigaspaces.log.LogEntries
import com.gigaspaces.log.LogEntry
import com.gigaspaces.log.LogEntryMatcher
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.domain.context.Service
import org.cloudifysource.domain.context.ServiceInstance
import org.openspaces.admin.gsc.GridServiceContainer
import org.openspaces.admin.machine.Machine
import org.openspaces.admin.Admin
import org.openspaces.admin.AdminFactory


service {
	extend "../../xap9x/xap-container"

    def maxinstances=context.isLocalCloud()?1:200
    numInstances 2
    minAllowedInstances 1
    maxAllowedInstances 2

    lifecycle{
        start "xap_start.groovy"
        postStart {
            locators = context.attributes.thisApplication["locators"]
            admin = new AdminFactory().addLocator(locators).create()
        }
    }

    customCommands([
            "killByPID": {
                String pids ->
                    def command = "sudo kill -9 " + pids
                    proc = command.execute()
                    def swOut = new StringWriter()
                    def swErr = new StringWriter()
                    proc.consumeProcessOutput(swOut, swErr)
                    proc.waitFor()

                    res = swOut.toString() + "\n"
                    res += "ERROR: \n" + swErr.toString()

                    println res
            }
            ])
}


