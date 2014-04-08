import groovy.text.SimpleTemplateEngine
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

if(!new File("${config.installDir}/${config.zipName}").exists()){
    new AntBuilder().sequential {
        mkdir(dir:"${config.installDir}")
        get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
        unzip(src:"${config.zipName}", dest:config.installDir, overwrite:true)
        chmod(dir:"${config.xapDir}/bin", perm:"+x", includes:"*.sh")
        chmod(dir:"${config.xapDir}/tools/gs-webui", perm:"+x", includes:"*.sh")
        chmod(dir:"${config.xapDir}/tools/groovy/bin", perm:"+x", excludes:"*.bat")
    }


    // Set license if defined
    if(config.license!=null && config.license.size()>0){
        def binding=["license":config.license]
        def engine = new SimpleTemplateEngine()
        def gslicense = new File("${config.installDir}/overwrite/gslicense.xml")
        def template = engine.createTemplate(gslicense).make(binding)

        new File("${config.installDir}/${config.xapDir}/gslicense.xml").withWriter{ out->
            out.write(template.toString())
        }
    }else{
        new AntBuilder().sequential {
            delete(file:"${config.installDir}/${config.xapDir}/gslicense.xml")
        }
    }
}


    new AntBuilder().sequential {
        chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
        exec(executable:"./install.sh", osfamily:"unix",
                output:"install.${System.currentTimeMillis()}.out",
                error:"install.${System.currentTimeMillis()}.err"
        )
    }
