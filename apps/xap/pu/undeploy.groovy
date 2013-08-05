import org.cloudifysource.domain.context.Service
import org.cloudifysource.domain.context.ServiceInstance
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.gsm.GridServiceManagers
import org.openspaces.admin.pu.ProcessingUnit;

import java.util.concurrent.TimeUnit;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

def pus = args[0]

def context = ServiceContextFactory.getServiceContext()
locators = context.attributes.thisApplication["locators"]
admin = new AdminFactory().useDaemonThreads(true).addLocator(locators).create();
String gsHome = context.attributes.thisApplication["home"]

println "start undeploy"
def mgtService = context.waitForService("mgt", 180, TimeUnit.SECONDS)
mgtInstances = mgtService.waitForInstances(mgtService.numberOfPlannedInstances, 30, TimeUnit.SECONDS)
def puService = context.waitForService("pu", 180, TimeUnit.SECONDS)

instanceID = context.getInstanceId();

if (instanceID == 1) {
  println "found instance"
	
	println "waiting for gsm ..."
	GridServiceManagers gridServiceManagers = admin.getGridServiceManagers();
	gridServiceManagers.waitFor(1, 120, TimeUnit.SECONDS);
	println "gsm is ready..."

	StringTokenizer puss = new StringTokenizer(pus, ",");
		
	while (puss.hasMoreElements()) {
		def pu = puss.nextElement();
        admin.processingUnits.waitFor(pu, 30, TimeUnit.SECONDS)
		admin.processingUnits.getProcessingUnit(pu).undeployAndWait(30, TimeUnit.SECONDS)
	}
}

			
println "undeploy closing admin"
admin.close();

println "undeploy End"
