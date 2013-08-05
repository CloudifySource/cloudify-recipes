import org.cloudifysource.utilitydomain.context.ServiceContextFactory

println "haproxy_addTomcatNode.groovy: start"

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('haproxy-service.properties').toURL())

def proxyConfigFile = new File("${config.configureFile}")
def proxyConfigFileText = proxyConfigFile.text
def url = args[0]
def applicationName = args[1]
def instanceId = args[2]
println "Reading Args. url = ${url}. applicationName = ${applicationName}. instanceId = ${instanceId}"

def serverInstanceText = "server ${applicationName}${instanceId} ${url} check cookie ${applicationName}${instanceId}"
println "NOR Server Instance Text = ${serverInstanceText}"
	
if(proxyConfigFileText.contains("${applicationName}_backend")){
	println "${applicationName} Back Already Present, adding extra node"

	def modifiedConfig = proxyConfigFileText.replace("#${applicationName}_backend_node", "#${applicationName}_backend_node"
						 + System.getProperty("line.separator") + "${serverInstanceText}")
	
	proxyConfigFile.text = modifiedConfig
	
	println "Node added. Modified config is ${proxyConfigFile}"				
} else {
	println "${applicationName} Backend not present, adding backend."
	def clusterConfigFileText = new File("templates/http_cluster.conf").text
	def modifiedClusterConfig  = clusterConfigFileText.replace("applicationNamePlaceHolder", "${applicationName}")	
									 
	modifiedClusterConfig  = modifiedClusterConfig.replace("#serverConfigPlaceHolder",
									 "#${applicationName}_backend_node" + System.getProperty("line.separator") + "${serverInstanceText}")	
									 
	println "Back end config. ${modifiedClusterConfig}"
	
	
	println "Adding frontend routing"
	
	def urlInterceptor = "acl is_${applicationName} path_beg -i /${applicationName}"
	println "url Interceptor = ${urlInterceptor}"

		
	 def modifiedConfig  = proxyConfigFileText.replace("#urlInterceptorPlaceholder",
									 "#urlInterceptorPlaceholder" + System.getProperty("line.separator") + "${urlInterceptor}")	
									 
	def backendRouter = "use_backend ${applicationName}_backend if is_${applicationName}"
	println "backendRouter = ${backendRouter}"
	
	modifiedConfig  = modifiedConfig.replace("#backendRouterPlaceholder",
									 "#backendRouterPlaceholder" + System.getProperty("line.separator") + "${backendRouter}")
									 
									 
	println "Front end config. ${modifiedClusterConfig}"
	
	modifiedConfig = modifiedConfig +  System.getProperty("line.separator") + modifiedClusterConfig
	
	proxyConfigFile.text = modifiedConfig

	println "Backend and Frontend added. Modified config is ${proxyConfigFile.text}"				
}

println "haproxy_addTomcatNode.groovy: Reload configuration file ${proxyConfigFile} ... "

new AntBuilder().sequential {
	echo(message:"haproxy_addTomcatNode.groovy: Chmodding +x ${context.serviceDirectory} ...")
	chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")
	
	exec(executable:"${context.serviceDirectory}/reloadConfiguration.sh", osfamily:"unix") {
		arg(value:"${proxyConfigFile}")
		arg(value:"${config.pidFile}")
	}
}

println "haproxy_addTomcatNode.groovy: Reloaded configuration file ${config.configureFile}."
