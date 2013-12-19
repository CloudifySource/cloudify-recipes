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
import java.io.File
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

println "start post-start"

try {
	println "post-start deploy pu ...."
	
	config = new ConfigSlurper().parse(new File("pu-service.properties").toURL())
	
	def spaceArgs = [config.gscCount, config.puUrl, config.puJars, config.puGrids, config.puPrimary, config.puBackup] as String[]
	Binding spaceContext = new Binding(spaceArgs)
	new GroovyShell(spaceContext).evaluate(new File("deployPU.groovy"))
	
	/*def spaceArgs = ['https://s3.amazonaws.com/RonZ', 'space.jar'] as String[]
	Binding spaceContext = new Binding(spaceArgs)
	new GroovyShell(spaceContext).evaluate(new File("deployPU.groovy"))

	def warArgs = ['https://s3.amazonaws.com/RonZ', 'SpaceAccess.war'] as String[]
	Binding warContext = new Binding(warArgs)
	new GroovyShell(warContext).evaluate(new File("deployPU.groovy"))
	*/
} catch (Exception e) {
	e.printStackTrace();
}