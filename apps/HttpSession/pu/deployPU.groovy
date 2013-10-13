import com.gigaspaces.log.LogEntries
import com.gigaspaces.log.LogEntry
import com.gigaspaces.log.LogEntryMatcher
import org.cloudifysource.dsl.context.Service
import org.cloudifysource.dsl.context.ServiceInstance
import org.openspaces.admin.Admin
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.pu.ProcessingUnit
import org.openspaces.admin.pu.ProcessingUnitDeployment
import org.openspaces.admin.GridComponent
import org.openspaces.admin.dump.DumpResult
import org.openspaces.admin.gsc.GridServiceContainer
import org.openspaces.admin.machine.Machine
import org.openspaces.admin.vm.VirtualMachine
import org.openspaces.admin.gsm.GridServiceManagers;
import java.util.concurrent.TimeUnit
import org.openspaces.admin.space.SpaceDeployment;
import static com.gigaspaces.log.LogEntryMatchers.lastN
import static com.gigaspaces.log.LogEntryMatchers.continuous
import java.io.File
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.StringTokenizer

def gscCount = args[0]
def url = args[1]
def fileNames = args[2]
def dataGrids = args[3]
def dataGridsPrimary = args[4]
def dataGridsBackups = args[5]
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
			new ProcessingUnitDeployment(puArchive));
	}
	
	if (dataGrids != null) {
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
			gridServiceManagers.deploy(spaceDeployment);
		}
	}
}
			
println "deployPU closing admin"			
admin.close();

println "deployPU End"