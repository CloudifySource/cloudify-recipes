/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
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
