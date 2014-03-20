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

new AntBuilder().sequential {
    chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
    exec(executable:"./install.sh", osfamily:"unix",
            output:"install.${System.currentTimeMillis()}.out",
            error:"install.${System.currentTimeMillis()}.err"
    )
}