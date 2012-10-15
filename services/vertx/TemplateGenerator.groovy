import groovy.text.GStringTemplateEngine

class TemplateGenerator {

    def static generateTemplates(String baseFile, String targetFile, bindings = [:]) {
        def engine = new GStringTemplateEngine()
        def builder = new AntBuilder()
        bindings = bindings.withDefault{""}
        def baseFileObj = new File(baseFile)
        if (baseFileObj.isDirectory()) { //directory
            baseFileObj.list().each { file ->
                generateTemplates("${baseFile}/${file}", "${targetFile}/${file}", bindings)
            }
        } else { //file
            targetFile = targetFile.replace(".template", "")
            def targetFileObj = new File(targetFile)
            if (targetFileObj.exists()) {
                builder.move(file:targetFile, toFile:"${targetFile}.back", failonerror:false)
            }
            targetFileObj.parentFile.mkdirs()
            try {
                targetFileObj.text = engine.createTemplate(baseFileObj).make(bindings).toString()
            } catch (e) {
                println "Could not generate file ${targetFile} from template ${baseFile}: ${e}"
            }
        }
    }

}

/*
instances = [["hostAddress":"host1"], ["hostAddress":"host2"]]
hosts = []
instances?.each {
    hosts << it.hostAddress
}

bindings = ["clusterGroupName":"vertx-cluster",
            "clusterGroupPassword":"cluster-password",
            "multicastEnabled":false,
            "tcpIpEnabled":true,
            "awsEnabled":false,
//            "awsAccessKey":"",
//            "awsSecretKey":"" ,
            "hosts":hosts,
            "awsRegion":"us-east-1"]
generateTemplates("/Users/uri1803/dev/CloudifySource/cloudify-recipes/services/vertx/templates", "/Users/uri1803/temp" , bindings)*/
