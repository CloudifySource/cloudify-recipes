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

def user = args[0]
def dbName = args[1]
def password = args[2]
def currQuery = args[3]


def static shellOut(command, Map env=[:]) {
    def proc = startProcess(shellify_cmd(command), env)
    def out = new StringBuilder()
    def err = new StringBuilder()
    proc.waitForProcessOutput(out, err)
    if (out) println "\n$out"
    if (err) println "\n$err"
}

def static startProcess(command, Map env=[:]) {
    ProcessBuilder pb = new ProcessBuilder(command)
    def environment = pb.environment()
    if (!env.isEmpty()) {
        environment += env
    }
    return pb.start()
}

def static shellify_cmd(command) {
    return ["/bin/sh", "-c", command as String]
}

shellOut("mysql -u ${user} -p${password} -D ${dbName} -e \"${currQuery}\"")