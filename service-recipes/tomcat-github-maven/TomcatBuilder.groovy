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
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.dsl.context.ServiceContextFactory;
import org.cloudifysource.dsl.context.ServiceContext;

class TomcatBuilder {
		
	final String installDir
	final ConfigObject config
	final ServiceContext context
	final AntBuilder ant
	final int shutdownPort
	final int httpPort
	final int ajpPort
	final int jmxPort
	final String workingDir
	
	TomcatBuilder() {
		ant = new AntBuilder()
		context = ServiceContextFactory.getServiceContext()
		config=new ConfigSlurper().parse(new File("${context.serviceDirectory}/tomcat.properties").toURL())
	    installDir = "${System.properties["user.home"]}/.cloudify/${context.applicationName}_${context.serviceName}_${context.instanceId}"
		
		workingDir = "${context.serviceDirectory}/${config.name}"
		
		def portIncrement = 0
		if (context.isLocalCloud()) {
		  portIncrement = context.instanceId - 1
		  println "tomcat port increment is " + portIncrement
		}
		httpPort = config.port + portIncrement
		shutdownPort = config.shutdownPort + portIncrement
		ajpPort = config.ajpPort + portIncrement
		jmxPort = config.jmxPort + portIncrement
	}
		
	void installTomcat() {

		ant.mkdir(dir:installDir)
		ant.get(src:config.downloadPath, dest:"${installDir}/${config.zipName}", skipexisting:true)
		ant.unzip(src:"${installDir}/${config.zipName}", dest:context.serviceDirectory, overwrite:true)
		ant.chmod(dir:"${workingDir}/bin", perm:'+x', includes:"*.sh")

		def serverXmlFile = new File("${workingDir}/conf/server.xml") 
		def serverXmlText = serverXmlFile.text	
		
		serverXmlText = serverXmlText.replace("port=\"${config.port}\"", "port=\"${httpPort}\"")
		serverXmlText = serverXmlText.replace("port=\"${config.ajpPort}\"", "port=\"${ajpPort}\"")
		serverXmlText = serverXmlText.replace("port=\"${config.shutdownPort}\"", "port=\"${shutdownPort}\"")
		serverXmlText = serverXmlText.replace('unpackWARs="true"', 'unpackWARs="false"')
		serverXmlFile.write(serverXmlText)
	}
	
	void catalina(catalinaarg) {
	 
	 ant.echo("catalina ${catalinaarg}")
	 
	 if (ServiceUtils.isWindows()) {
	  ant.exec(executable:"cmd", failonerror:true) { 
	   env(key:"CATALINA_HOME", value: "${workingDir}")
	   env(key:"CATALINA_BASE", value: "${workingDir}")
	   env(key:"CATALINA_TMPDIR", value: "${workingDir}/temp")
	   env(key:"CATALINA_OPTS", value:"-Dcom.sun.management.jmxremote.port=${jmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false")
	   arg(value:"/c")
	   arg(value:"\"${workingDir}/bin/catalina.bat ${catalinaarg}\"")
	  }
	 }
	 else {
	  ant.exec(executable:${workingDir}/bin/catalina.sh, failonerror:true) { 
	   env(key:"CATALINA_HOME", value: "${workingDir}")
	   env(key:"CATALINA_BASE", value: "${workingDir}")
	   env(key:"CATALINA_TMPDIR", value: "${workingDir}/temp")
	   env(key:"CATALINA_OPTS", value:"-Dcom.sun.management.jmxremote.port=${jmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false")
	   arg(value:catalinaarg)
	  }
	 }
	 ant.echo("done")
	}
	
	void run() {
	 catalina("run")
	}
	
	void stop() {
	  catalina("stop")
	  println "tomcat stopped"
    }
	
	void update(war) {
	  ant.copy todir: "${workingDir}/webapps", file:war, overwrite:true
	}
	
}
