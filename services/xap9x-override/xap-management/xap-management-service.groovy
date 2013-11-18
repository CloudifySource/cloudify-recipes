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

import org.openspaces.admin.AdminFactory
import org.openspaces.admin.Admin
import java.util.concurrent.TimeUnit

import static com.gigaspaces.log.LogEntryMatchers.lastN
import static com.gigaspaces.log.LogEntryMatchers.continuous
import com.gigaspaces.log.LogEntries
import com.gigaspaces.log.LogEntry
import com.gigaspaces.log.LogEntryMatcher
import org.cloudifysource.domain.context.Service
import org.cloudifysource.domain.context.ServiceInstance
import org.openspaces.admin.gsc.GridServiceContainer
import org.openspaces.admin.machine.Machine
import util
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

service {
    extend "../../xap9x-27/xap-management"
    numInstances (context.isLocalCloud()?1:2 )
    minAllowedInstances 1
    maxAllowedInstances 2

    lifecycle{
        postStart {
            util.setIsStopped(context, true)
        }
        startDetectionTimeoutSecs 400
    }

	customCommands ([
//Public entry points
            "startFO": { foTimeInMin ->
                util.setIsStopped(context,false)
                Thread.start {
                    util.invokeLocal(context,"_startFO", [
                            "foTimeInMin":foTimeInMin
                    ])
                }
                return "Fail over started!"
            },
            "_startFO"	: "startFO.groovy",
            "stopFO": {
                util.setIsStopped(context,true)
                Thread thread = context.attributes.thisService["killingThread"];
                if (thread!=null)
                    thread.interrupt();

                return "Fail over stopped"
            }])
}


