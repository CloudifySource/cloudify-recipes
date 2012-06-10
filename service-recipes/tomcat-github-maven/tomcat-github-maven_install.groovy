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
import org.cloudifysource.dsl.context.ServiceContextFactory

def config = new ConfigSlurper().parse(new File("tomcat-github-maven-service.properties").toURL())
def context = ServiceContextFactory.getServiceContext()


println "Installing ${context.serviceName} ..."

def ant = new AntBuilder()
def git = new GitBuilder()
def mvn = new MavenBuilder()
def tomcat = new TomcatBuilder()

tomcat.installTomcat()
git.installGit()
mvn.installMaven()
ant.echo("downloading source code from ${config.applicationSrcUrl}")
git.clone(config.applicationSrcUrl,"${context.serviceDirectory}/${config.applicationSrcFolder}", verbose:true)

def pom = "${context.serviceDirectory}/${config.applicationSrcFolder}/pom.xml"
if (!(new File(pom).exists())) {
	throw new java.io.FileNotFoundException(pom + " does not exist");
}
println "Installation complete"