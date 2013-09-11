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
 ****************************************************************************** */
import static Shell.*

service {
    name = serviceName

    compute {
        template computeTemplate
    }

    lifecycle {
        init initScript
        install installScript
        start {
            if (useSudo)
                sudo(startScript, [env: env?: [:])
            else 
                sh([env: env?: [:], startScript)
            return null
        }

        locator {
            //avoid monitoring started processes by cloudify
            return [] as LinkedList
        }
    }

    customCommands([

    ])
}
