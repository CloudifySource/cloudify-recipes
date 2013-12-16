import java.util.concurrent.TimeUnit
import java.util.regex.*

// Find this service instance

def static getThisService(context){
	def thisService = context.waitForService(context.getServiceName(), 60, TimeUnit.SECONDS)
	def instances = getServiceInstances(context,context.getServiceName(),thisService.getNumberOfPlannedInstances())
	assert (instances != null)
	def instanceID = context.getInstanceId()			                       
	for( it in instances){
	   if ( instanceID == it.instanceId ) {
			return it
		}
	}
	assert false,"local instance not found"
}

// Find instances of named service

def static getServiceInstances(context,serviceName,count){
	def service = context.waitForService(serviceName, 60, TimeUnit.SECONDS)
	def instances = service.waitForInstances(count,60, TimeUnit.SECONDS	)
	return instances
}

// invoke command in current instance.  "args" is a map to be passed
// to the target closure via the attributes API

def static invokeLocal(context,name,args){
	args.each{ key,val->
			context.attributes.thisInstance[key] = val
	}
	println "calling getThisService"
	getThisService(context).invoke(name)
}

//Puts quotes around alpha-num substrings in parameter

def static quoteAlnum(unquoted){
	def p=Pattern.compile('([a-zA-Z0-9_\\.]+)')
	def m=p.matcher(unquoted)
	return m.replaceAll("\"\$1\"")
}
