import org.cloudifysource.dsl.context.ServiceContextFactory

serviceContext = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("nodejs-service.properties").toURL())
script = "${serviceContext.serviceDirectory}/${config.name}"

new AntBuilder().sequential {
    exec(executable: "${script}.sh", osfamily: "unix") {
        arg(value: "${config.jsFileName}")
    }
    exec(executable: "${script}.exe", osfamily: "windows") {
        arg(line:"${config.jsFileName}")
    }
}
