//----------------------------------------------------------------------------------
// Deploys a "xapstream" topology.  This is not much different from deploying
// a "normal" storm topology, except a xapstream topology requires a stream name
// and a XAP host to connect to as argument.
//----------------------------------------------------------------------------------

println "deploy.groovy: Starting..."

def context=null
try{
context = org.cloudifysource.dsl.context.ServiceContextFactory.getServiceContext()
}
catch(e){
context = org.cloudifysource.utilitydomain.context.ServiceContextFactory.getServiceContext()
}

config  = new ConfigSlurper().parse(new File("${context.serviceDirectory}/storm-service.properties").toURL())

instanceID = context.getInstanceId()
installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID

topoName=context.attributes.thisService["topoName"]

topoUrl=context.attributes.thisService["topoUrl"] 
println "deploy.groovy: topoFile is ${topoUrl}"

className=context.attributes.thisService["className"] 

topoPath=new java.net.URL(topoUrl).getFile()
topoFile=topoPath.substring(topoPath.lastIndexOf("/")+1)

args = context.attributes.thisService["args"]
println "deploy.groovy: args = ${args}"

xapHost=context.attributes.thisService["xapHost"]
println "deploy.groovy: xapHost = ${xapHost}"

line="jar ${topoFile} ${className} ${topoName} ${xapHost} ${args}"

new AntBuilder().sequential {
	
	echo(message:"deploy.groovy: Getting ${topoUrl} ...")
	exec(executable: "wget", osfamily: "unix", failonerror:true){
		arg(line:"-N --no-check-certificate ${topoUrl}")
	}
	
	echo(message:"deploy.groovy: deploying ... ${line}")
	exec(executable:"${config.script}", osfamily:"unix",failonerror:true) {
		arg(line:"${line}")
	}
}

println "deploy.groovy: End"
return true


