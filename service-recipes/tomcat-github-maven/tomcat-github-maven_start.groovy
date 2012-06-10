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
import org.cloudifysource.dsl.utils.ServiceUtils;
import java.util.concurrent.*

class CustomThreadFactory implements ThreadFactory {
    
	String name;
	
	Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
		thread.setName(name)
        return thread;
    }
}

def context = ServiceContextFactory.getServiceContext()
def instanceID = context.getInstanceId()
def serviceName = context.getServiceName()
println "tomcat_start.groovy: This ${serviceName} instance ID is ${instanceID}"
def config=new ConfigSlurper().parse(new File("${serviceName}-service.properties").toURL())

def ant = new AntBuilder()
def git = new GitBuilder(workingDir:"${context.serviceDirectory}/${config.applicationSrcFolder}")
def mvn = new MavenBuilder(workingDir:"${context.serviceDirectory}/${config.applicationSrcFolder}")
def tomcat = new TomcatBuilder()

context.attributes.thisInstance["git-head"]=config.gitHead;

def update= { githead->

 //update from remote repository
 git.checkout "master"
 git.fetch "origin"
 git.merge "origin/master"
 
 //create a branch for the specified commit ${githead}
 git.branch "build", githead, force:true
 git.checkout "build"
 
 //build and deploy
 mvn.cleanPackage(skipTests:false)
 ant.echo "deploying war file"
 def war="${context.serviceDirectory}/${config.applicationSrcFolder}/${config.applicationTargetWar}"
 tomcat.update(war)
}

def gitHead = null
CountDownLatch latch = new CountDownLatch(1)
def executor = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory(name:"update-thread"));
executor.scheduleWithFixedDelay({
    try {
    //update git if head configuration changed
	def currentGitHead = context.attributes.thisInstance["git-head"]
	if (gitHead == null || !gitHead.equals(currentGitHead)) {
		update(currentGitHead);
		gitHead = currentGitHead;
		latch.countDown();
	} 
	} catch (Throwable t) {
		System.err.println("Error updating git: "+t);
        //t = StackTraceUtils.sanitizeStackTrace(t);
		t.printStackTrace(System.err)
		System.err.println("Sleeping for 1 minute before retrying");
		sleep(60*1000)
	}
},0,10,TimeUnit.SECONDS)

println "waiting for initial war deployment to complete"
latch.await()

tomcat.run();

executor.shutdown();