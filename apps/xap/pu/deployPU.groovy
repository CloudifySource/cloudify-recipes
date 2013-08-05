import org.cloudifysource.domain.context.Service
import org.cloudifysource.domain.context.ServiceInstance
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.pu.ProcessingUnit
import org.openspaces.admin.pu.ProcessingUnitDeployment
import org.openspaces.admin.gsm.GridServiceManagers;
import java.util.concurrent.TimeUnit
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

def gscCount = args[0]
def url = args[1]
def fileNames = args[2]
def puPrimary = args[3]
def puBackups = args[4]
def maxInstancesPerVM = args[5]
def maxInstancesPerMachine = args[6]

def context = ServiceContextFactory.getServiceContext()
locators = context.attributes.thisApplication["locators"]
admin = new AdminFactory().useDaemonThreads(true).addLocator(locators).create();
String gsHome = context.attributes.thisApplication["home"]

println "start deployPU and url is ${url}..."
def mgtService = context.waitForService("mgt", 180, TimeUnit.SECONDS)
mgtInstances = mgtService.waitForInstances(mgtService.numberOfPlannedInstances, 30, TimeUnit.SECONDS)
def puService = context.waitForService("pu", 180, TimeUnit.SECONDS)
def allGscInstances = (mgtService.numberOfPlannedInstances + puService.numberOfPlannedInstances) * Integer.parseInt(gscCount);
println "allGscInstances is " + allGscInstances + " waiting ....";
mgtInstances = puService.waitForInstances(allGscInstances, 60, TimeUnit.SECONDS)
instanceID = context.getInstanceId();

if (instanceID == 1) {
	println "found instance"
	
	println "waiting for gsm ..."
	GridServiceManagers gridServiceManagers = admin.getGridServiceManagers();
	gridServiceManagers.waitFor(1, 120, TimeUnit.SECONDS);
	println "gsm is ready..."
	
	StringTokenizer st = new StringTokenizer(fileNames, ",");
	
	while (st.hasMoreElements()) {
		def fileName = st.nextElement();
		println "curr file is " + fileName;
		
		def destDir = gsHome + "/deploy";
		println "get the file and dir is ${destDir}"
		
		new AntBuilder().sequential {
			get(src: url + "/" + fileName, dest: destDir + "/" + fileName, skipexisting: false)
		}

		println "get pu file and try to deploy"
		File puArchive = new File(destDir + "/" + fileName);
		ProcessingUnit pu = gridServiceManagers.deploy(
			new ProcessingUnitDeployment(puArchive)
                    .numberOfInstances(Integer.parseInt(puPrimary.trim()))
                    .numberOfBackups(Integer.parseInt(puBackups.trim()))
                    .maxInstancesPerVM(Integer.parseInt(maxInstancesPerVM.trim()))
                    .maxInstancesPerMachine(Integer.parseInt(maxInstancesPerMachine.trim())));
        pu.waitFor(pu.plannedNumberOfInstances, 120, TimeUnit.SECONDS)
	}
}
			
println "deployPU closing admin"			
admin.close();

println "deployPU End"