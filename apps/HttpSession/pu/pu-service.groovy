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
        startDetection {
            ServiceUtils.isPortFree(4242)
        }
		postStart "post-start.groovy"
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
            "startXAP": {
                group = "largecluster"
                locators = context.attributes.thisApplication["locators"]
                home = context.attributes.thisService["home"]

                def command = "env - ${context.serviceDirectory}/run.sh ${locators} ${group} ${home} >/dev/null 2>/dev/null"
                proc = command.execute()
                return null
            },

            "upgradeXAP": {
                {url, fileNames ->
                    def path
                    fileNames.each { fileName ->
                        if (fileName.equalsIgnoreCase("gs-runtime.jar") || fileName.equalsIgnoreCase("gs-openspaces.jar")) {
                            path = gsHome + "/lib/required/"
                        }
                        if (fileName.equalsIgnoreCase("gs-boot.jar")) {
                            path = gsHome + "/lib/platform/boot"
                        }
                        if (fileName.equalsIgnoreCase("gs-webui.war")) {
                            path = gsHome + "/tools/gs-webui"
                        }
                        if (fileName.equalsIgnoreCase("gs_logging.properties")) {
                            path = gsHome + "/config"
                        }

                        new AntBuilder().sequential {
                            get(src: url + "/" + fileName, dest: path, skipexisting: false)
                        }
                    }
                }
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
    ])
}
