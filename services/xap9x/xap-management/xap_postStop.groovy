import org.cloudifysource.utilitydomain.context.ServiceContextFactory
context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())
println "XAPSTOP STARTING"

new AntBuilder().sequential {

    exec(executable:"pkill", osfamily:"unix",
            output:"pkill-java.${System.currentTimeMillis()}.out",
            error:"pkill-java.${System.currentTimeMillis()}.err"
    ){
        arg("line":"-f ${context.serviceDirectory}")
    }

    exec(executable:"pkill", osfamily:"unix",
            output:"pkill-runxap.${System.currentTimeMillis()}.out",
            error:"pkill-runxap.${System.currentTimeMillis()}.err"
    ){
        arg("line":"-f 10000d")
    }

    println "XAPSTOP EXITING"

}
/*

//Stop butterfly if enabled
if (config.butterflyEnabled) {
    new AntBuilder().sequential {

        exec(executable:"pkill", osfamily:"unix",
                output:"pkill.${System.currentTimeMillis()}.out",
                error:"pkill.${System.currentTimeMillis()}.err"
        ){
            arg("line":"-f 'butterfly.server.py --host=0.0.0.0 --port=8081'")
        }
    }
}

*/
