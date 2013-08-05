import com.gigaspaces.log.LogEntries
import com.gigaspaces.log.LogEntry
import com.gigaspaces.log.LogEntryMatcher
import org.cloudifysource.domain.context.Service
import org.cloudifysource.domain.context.ServiceInstance
import org.openspaces.admin.Admin
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.pu.ProcessingUnit
import org.openspaces.admin.pu.ProcessingUnitDeployment
import org.openspaces.admin.GridComponent
import org.openspaces.admin.dump.DumpResult
import org.openspaces.admin.gsc.GridServiceContainer
import org.openspaces.admin.machine.Machine
import org.openspaces.admin.vm.VirtualMachine
import java.util.concurrent.TimeUnit
import static com.gigaspaces.log.LogEntryMatchers.lastN
import static com.gigaspaces.log.LogEntryMatchers.continuous
import org.openspaces.admin.machine.*
import org.openspaces.admin.gsa.*
import org.openspaces.admin.vm.*

Admin admin = null
String gsHome = null
String locators = null
Boolean stopped = false
File excelOutputDir = null

class MyMockException extends Exception {}

service {
    name "mgt"

    numInstances 1
    maxAllowedInstances 1

    compute {
        template "SMALL_LINUX"
    }

    lifecycle {
        install "mgt-install.groovy"
        start "mgt-start.groovy"
        postStart "post-start.groovy"
        startDetection {
            ServiceUtils.isPortFree(4242)
        }

        locator {
			println "####start locators"
			def ip = context.getPrivateAddress();
			println "####in locators and ip is " + ip;
			
			locators = context.attributes.thisApplication["locators"]
			admin = new AdminFactory().useDaemonThreads(true).addLocator(locators).create();

			def mgtService = context.waitForService("mgt", 180, TimeUnit.SECONDS)
			mgtInstances = mgtService.waitForInstances(mgtService.numberOfPlannedInstances, 30, TimeUnit.SECONDS)
			def puService = context.waitForService("pu", 180, TimeUnit.SECONDS)
			def allGscInstances = mgtService.numberOfPlannedInstances + puService.numberOfPlannedInstances;
			println "####in locators and allGscInstances is " + allGscInstances + " waiting for agents...";			
			admin.getGridServiceManagers().waitFor(allGscInstances, 120, TimeUnit.SECONDS);
			println "####in locators finished waiting";
			
			Machine machine = admin.getMachines().getMachineByHostAddress(ip);
			
			if (machine != null) {
				GridServiceAgent gridServiceAgent = machine.getGridServiceAgent();
				
				if(gridServiceAgent != null) {
				   VirtualMachine virtualMachine = gridServiceAgent.getVirtualMachine();
				   VirtualMachineDetails vmDetails = virtualMachine.getDetails();
				   long pid = vmDetails.getPid();
				   println "####in locators GSA's pid: " + pid;
				   return [pid];
				} else {
					return [];				
				}
			} else {
				println "####in locators machine was null";
			}
        }
    }
}
