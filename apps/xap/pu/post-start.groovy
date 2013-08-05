import org.cloudifysource.domain.context.Service
import org.cloudifysource.domain.context.ServiceInstance
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

println "start post-start"

try {

	config = new ConfigSlurper().parse(new File("pu-service.properties").toURL())

    if(config.isEDG.equals(true)){
        println "post-start deploy space ...."
	    def spaceArgs = [config.gscCount, config.dataGrids, config.numberOfPrimaries, config.numberOfsBackupsPerPrimary, config.maxInstancesPerVM, config.maxInstancesPerMachine ] as String[]
	    Binding spaceContext = new Binding(spaceArgs)
	    new GroovyShell(spaceContext).evaluate(new File("deployEDG.groovy"))
    }else{
        println "post-start deploy pu ...."
        def spaceArgs = [config.gscCount, config.puUrl, config.puJars, config.numberOfPrimaries, config.numberOfsBackupsPerPrimary, config.maxInstancesPerVM, config.maxInstancesPerMachine ] as String[]
        Binding spaceContext = new Binding(spaceArgs)
        new GroovyShell(spaceContext).evaluate(new File("deployPU.groovy"))
    }

} catch (Exception e) {
	e.printStackTrace();
}