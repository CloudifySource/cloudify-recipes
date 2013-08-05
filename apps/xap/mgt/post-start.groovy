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
import static com.gigaspaces.log.LogEntryMatchers.lastN
import static com.gigaspaces.log.LogEntryMatchers.continuous
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

println "start post-start"

context = ServiceContextFactory.getServiceContext()
locators = context.attributes.thisApplication["locators"]
gsHome = context.attributes.thisApplication["home"]