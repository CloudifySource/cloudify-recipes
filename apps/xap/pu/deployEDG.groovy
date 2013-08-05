import org.cloudifysource.domain.context.Service
import org.cloudifysource.domain.context.ServiceInstance
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.gsm.GridServiceManagers
import org.openspaces.admin.pu.ProcessingUnit;

import java.util.concurrent.TimeUnit
import org.openspaces.admin.space.SpaceDeployment;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

def gscCount = args[0]
def dataGrids = args[1]
def dataGridsPrimary = args[2]
def dataGridsBackups = args[3]
def maxInstancesPerVM = args[4]
def maxInstancesPerMachine = args[5]

def context = ServiceContextFactory.getServiceContext()
locators = context.attributes.thisApplication["locators"]
admin = new AdminFactory().useDaemonThreads(true).addLocator(locators).create();
String gsHome = context.attributes.thisApplication["home"]

println "start deployEDG"
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

		StringTokenizer grids = new StringTokenizer(dataGrids, ",");
		StringTokenizer primaries = new StringTokenizer(dataGridsPrimary, ",");
		StringTokenizer backups = new StringTokenizer(dataGridsBackups, ",");
		
		while (grids.hasMoreElements()) {
			def gridName = grids.nextElement();
			def primaryPartitions = primaries.nextElement();
			def backupPartitions = backups.nextElement();
			println "curr grid name is " + gridName + " with [" + primaryPartitions + "," + backupPartitions + "]";
			
			SpaceDeployment spaceDeployment = new SpaceDeployment(gridName);
			spaceDeployment.numberOfInstances(Integer.parseInt(primaryPartitions.trim()));
			spaceDeployment.numberOfBackups(Integer.parseInt(backupPartitions.trim()));
            spaceDeployment.maxInstancesPerVM(Integer.parseInt(maxInstancesPerVM.trim()));
            spaceDeployment.maxInstancesPerMachine(Integer.parseInt(maxInstancesPerMachine.trim()));
            ProcessingUnit pu= gridServiceManagers.deploy(spaceDeployment);
            pu.waitFor(pu.plannedNumberOfInstances, 120, TimeUnit.SECONDS);
		}
}

			
println "deployEDG closing admin"
admin.close();

println "deployEDG End"