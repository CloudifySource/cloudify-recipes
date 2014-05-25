import org.cloudifysource.utilitydomain.context.ServiceContextFactory
context=ServiceContextFactory.serviceContext

println "XAPSTOP STARTING"

/*
new AntBuilder().sequential {

    exec(executable:"pkill", osfamily:"unix",
            output:"pkill-java.${System.currentTimeMillis()}.out",
            error:"pkill-java.${System.currentTimeMillis()}.err"
    ){
        arg("line":"-f ${context.serviceDirectory}")
    }

    println "XAPSTOP EXITING"

}
*/

