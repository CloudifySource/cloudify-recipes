import groovy.text.SimpleTemplateEngine
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

new AntBuilder().sequential {
    mkdir(dir:"${config.installDir}")
    get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
    unzip(src:"${config.installDir}/${config.zipName}", dest:config.installDir, overwrite:true)
    chmod(dir:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/bin", perm:"+x", includes:"*.sh")
    chmod(dir:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/tools/gs-webui", perm:"+x", includes:"*.sh")
    chmod(dir:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/tools/groovy/bin", perm:"+x", excludes:"*.bat")
}

// Set license if defined
if(config.license!=null && config.license.size()>0){
    def binding=["license":config.license]
    def engine = new SimpleTemplateEngine()
    def gslicense = new File("${context.serviceDirectory}/overwrite/gslicense.xml")
    def template = engine.createTemplate(gslicense).make(binding)

    new File("${context.serviceDirectory}/${config.installDir}/${config.xapDir}/gslicense.xml").withWriter{ out->
        out.write(template.toString())
    }
}else{
    new AntBuilder().sequential {
        delete(file:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/gslicense.xml")
    }
}



new AntBuilder().sequential {
    chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
    exec(executable:"./install.sh", osfamily:"unix",
            output:"install.${System.currentTimeMillis()}.out",
            error:"install.${System.currentTimeMillis()}.err"
    )
}