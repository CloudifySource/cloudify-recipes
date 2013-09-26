/*******************************************************************************
* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
import java.util.UUID
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.application.config.ApplicationConfig
import org.openspaces.admin.pu.config.ProcessingUnitConfig
import org.openspaces.admin.space.SpaceDeployment
import groovy.util.ConfigSlurper;
import groovy.text.SimpleTemplateEngine
import org.openspaces.core.gateway.GatewayTarget
import org.openspaces.admin.space.Space


def context=ServiceContextFactory.serviceContext
def config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

def puname=context.attributes.thisInstance["deploy-gateway-puname"]
def spacename=context.attributes.thisInstance["deploy-gateway-spacename"]
def localgwname=context.attributes.thisInstance["deploy-gateway-localgwname"]
def targets=context.attributes.thisInstance["deploy-gateway-targets"]
def sources=context.attributes.thisInstance["deploy-gateway-sources"]
lookups=[]
i=0
while (true){
	def lookup=context.attributes.thisInstance["deploy-gateway-lookup"+i]
	if(lookup==null)break
	fields=lookup.split(",")
	assert fields.length==4,"bad lookup config, need {gwname,address,discoport,commpot}"
	lookups.add(['gwname':fields[0],'address':fields[1],'discoport':fields[2],'commport':fields[3]])
	i++
}


println "deploy-gateway called: puname='${puname}' spacename='${spacename}' localgwname='${localgwname}' targets='${targets}' sources='${sources}'" 

assert (spacename!=null),"space name must not be null"
assert (localgwname!=null),"local gateway name must not be null"

//CREATE PU
pudir=config.installDir+"/gwpu/META-INF/spring"
new AntBuilder().sequential(){
	delete(dir:pudir)
	mkdir(dir:pudir)
}

def binding=[:]
binding['localgwname']=localgwname
binding['localspaceurl']="jini://${context.privateAddress}:${config.lusPort}/${spacename}"
binding['targets']=targets.length()>0?targets.split(","):null
binding['sources']=sources.length()>0?sources.split(","):null
binding['lookups']=lookups	

def engine = new SimpleTemplateEngine()
def putemplate = new File('templates/gateway-pu.xml')
def template = engine.createTemplate(putemplate).make(binding)
new File("${pudir}/pu.xml").withWriter{ out->
	out.write(template.toString())
}

//DEPLOY

// find gsm
def admin=new AdminFactory().addLocators("127.0.0.1:${config.lusPort}").createAdmin();
def gsm=admin.gridServiceManagers.waitForAtLeastOne(10,TimeUnit.SECONDS)
assert gsm!=null

// make sure there are GSCs
def gscs=admin.gridServiceContainers
gscs.waitFor(1,5,TimeUnit.SECONDS)
assert (gscs.size!=0),"no containers found"

//deploy
def pu=new ProcessingUnitConfig()
pu.setProcessingUnit("${config.installDir}/gwpu")
pu.setName(puname)
gsm.deploy(pu)

// update admin
if(targets!=null && targets.trim().length()>0){
	GatewayTarget gwTarget = new GatewayTarget(localgwname)
	Space space=admin.getSpaces().waitFor(spacename,10,TimeUnit.SECONDS)

	if(space==null)gsm.undeploy(puname)
	assert space!=null,"failed to locate space ${spacename}"

	space.getReplicationManager().addGatewayTarget(gwTarget)
}

admin.close()

return true

