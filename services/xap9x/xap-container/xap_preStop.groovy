//This is necessary in case xap-container deployed myDataGrid but the installation fails because of timeout
context=ServiceContextFactory.serviceContext
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import org.openspaces.admin.AdminFactory
println "XAP-PRESTOP STARTING"

locators = context.attributes.thisInstance["xaplookuplocators"]
admin=new AdminFactory().useDaemonThreads(true).addLocators(locators).createAdmin();
gsm=admin.gridServiceManagers.waitForAtLeastOne(2,TimeUnit.MINUTES)
dataGridPU = admin.getProcessingUnits().waitFor("myDataGrid", 2, TimeUnit.MINUTES);
if (dataGridPU != null) {
    undeployed = dataGridPU.undeployAndWait(1, TimeUnit.MINUTES);
    if (undeployed == false) {
        println "WARNING: Failed to undeploy myDataGrid."
    }
}

println "XAP-PRESTOP FINISHED"