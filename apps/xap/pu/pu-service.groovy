import org.cloudifysource.dsl.utils.ServiceUtils
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.machine.*
import org.openspaces.admin.gsa.*
import org.openspaces.admin.vm.*
import java.util.concurrent.TimeUnit

String gsHome = null

service {
    name "pu"

    numInstances 1
    maxAllowedInstances 2

    compute {
        template "SMALL_LINUX"
    }

    lifecycle {
        install "pu-install.groovy"
        start "pu-start.groovy"
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
				
				if (gridServiceAgent != null) {
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

    customCommands([
            "killByPID": {
                String pids ->
                    def command = "sudo kill -9 " + pids
                    proc = command.execute()
                    def swOut = new StringWriter()
                    def swErr = new StringWriter()
                    proc.consumeProcessOutput(swOut, swErr)
                    proc.waitFor()

                    res = swOut.toString() + "\n"
                    res += "ERROR: \n" + swErr.toString()

                    println res
            },
            "executeShellCommand": {
                String command ->
                    println "Inside closure"
                    File file = new File("./tmp.sh")

                    command.split(";").each {
                        file << ("${it}\n")
                    }
                    Process proc = "sh ./tmp.sh".execute()
                    def swOut = new StringWriter()
                    def swErr = new StringWriter()
                    proc.consumeProcessOutput(swOut, swErr)
                    proc.waitFor()

                    res = swOut.toString() + "\n"
                    res += "ERROR: \n" + swErr.toString()

                    file.delete()
                    return res
            },
            "deployPU" : "deployPU.groovy",
            "deployEDG" : "deployEDG.groovy",
            "undeploy" : "undeploy.groovy"
    ])
}
