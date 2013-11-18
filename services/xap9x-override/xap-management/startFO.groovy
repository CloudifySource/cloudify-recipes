import static com.gigaspaces.log.LogEntryMatchers.lastN
import static com.gigaspaces.log.LogEntryMatchers.continuous
import com.gigaspaces.log.LogEntries
import com.gigaspaces.log.LogEntry
import com.gigaspaces.log.LogEntryMatcher
import org.cloudifysource.domain.context.Service
import org.cloudifysource.domain.context.ServiceInstance
import org.openspaces.admin.gsc.GridServiceContainer
import org.openspaces.admin.machine.Machine
import org.openspaces.admin.Admin
import org.openspaces.admin.AdminFactory
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

def static getIsStopped(context) {
    return context.attributes.thisService["stopped"]
}
def static setIsStopped(context, stopped) {
    context.attributes.thisService["stopped"] = stopped
}

Boolean stopped = false

context=ServiceContextFactory.serviceContext
foTimeInMin=context.attributes.thisInstance["foTimeInMin"]

Long longfoTimeInMin = Long.valueOf(foTimeInMin)
List<Integer> pids = new ArrayList<Integer>();
locators = context.attributes.thisApplication["locators"]
home = context.attributes.thisApplication["home"]

setIsStopped(context, false)
ip=InetAddress.getLocalHost().getHostAddress()
admin = new AdminFactory()
        .useDaemonThreads(true)
        .addLocators("${ip}:4242")
        .create();

Thread.start{
    LogEntryMatcher matcher = continuous(lastN(100));
    while (!getIsStopped(context)) {
        try {
            admin.gridServiceContainers.any { gsc ->
                final LogEntries logs = gsc.logEntries(matcher).each {LogEntry log ->
                    if(log.text.contains("LRMI Connection resources are at critical level")){
                        setIsStopped(context,true)
                        println "### found suspicious string [ "	+ matcher + " ] ###"
                        throw new Exception("exception!")
                    }
                }
            }
        }catch(Exception e){
            println "caught exception th1 "+e.toString()
            break //we found the regexp and want to stop failover
        }
        Thread.sleep(1000);
    }
    println "Breaking 1!"
}

Thread.start {
    stopped = getIsStopped(context)
    println "inside thread " + stopped
    try {
        while (!(stopped = getIsStopped(context))) {
            println "inside while " + stopped

            println "find agent service"
            Service agentService = context.waitForService("xap-container", 180, TimeUnit.SECONDS)
            if (agentService == null) {
                println "agent service is null"
                throw new IllegalStateException("agent service not found.");
            }
            println "found agent service ${agentService}, find agentHostInstances"
            agentHostInstances = agentService.waitForInstances(agentService.numberOfPlannedInstances, 30, TimeUnit.SECONDS)

            println "find agents machines ${agentService.numberOfPlannedInstances}"
            admin.machines.waitFor(agentService.numberOfPlannedInstances, 30, TimeUnit.SECONDS)
            println "Iam here!"
            if (agentHostInstances == null) {
                println "agent host instances are null"
                throw new IllegalStateException("Non agentServiceInstance was found.");
            }

            println "find agentServiceInstance"
            ServiceInstance agentServiceInstance = agentService.instances.getAt(new Random(System.currentTimeMillis()).nextInt(agentService.instances.length))
            admin.machines.machines.each { Machine machine ->
                if (machine.hostAddress.equalsIgnoreCase(agentServiceInstance.hostAddress)) {
                    machine.gridServiceContainers.containers.each { GridServiceContainer gsc ->
                        pids.add(gsc.virtualMachine.details.pid)
                    }
                }
            }
            def prcs = ""
            pids.each { pid ->
                prcs += pid + " "
            }
            int gscs = admin.getGridServiceContainers().getContainers().length

            if(getIsStopped(context)){
                break
            }
            println " killing GSCs with pids " + prcs
            agentServiceInstance.invoke("killByPID", prcs)
            Thread.sleep(longfoTimeInMin * 60 * 1000)
            pids.clear()
        }
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    println "Breaking 2!"
}