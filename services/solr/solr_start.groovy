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
import groovy.util.ConfigSlurper

config = new ConfigSlurper().parse(new File("solr.properties").toURL())

new AntBuilder().sequential {
	java(jar:"${config.home}/example/start.jar", 
		dir:"${config.home}/example", fork:true) {
		jvmarg value:"-Dcom.sun.management.jmxremote"
		jvmarg value:"-Dcom.sun.management.jmxremote.port=${config.jmxPort}"
		jvmarg value:"-Dcom.sun.management.jmxremote.ssl=false"
		jvmarg value:"-Dcom.sun.management.jmxremote.authenticate=false"
	}
}
