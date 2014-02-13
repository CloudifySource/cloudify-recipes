import org.cloudifysource.utilitydomain.context.ServiceContextFactory
context=ServiceContextFactory.serviceContext

println "TTY-STOP STARTING"

new AntBuilder().sequential {

    exec(executable:"pkill", osfamily:"unix",
            output:"pkill-java.${System.currentTimeMillis()}.out",
            error:"pkill-java.${System.currentTimeMillis()}.err"
    ){
        arg("line":"-f ${context.serviceDirectory}")
    }

    exec(executable:"pkill", osfamily:"unix",
            output:"pkill.${System.currentTimeMillis()}.out",
            error:"pkill.${System.currentTimeMillis()}.err"
    ){
        arg("line":"-f tty.js")
    }

    println "TTY-STOP EXITING"

}

