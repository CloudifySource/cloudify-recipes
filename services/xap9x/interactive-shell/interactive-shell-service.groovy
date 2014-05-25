import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import util

service {
    name "interactive-shell"
    icon "butterfly.gif"
    type "APP_SERVER"

    numInstances 1

    def serviceIP = "127.0.0.1"

    compute {
        template computeTemplate
    }
    lifecycle{
        init {
            if (!context.isLocalCloud()) {
                serviceIP = context.getPrivateAddress()
            }
            context.attributes.thisInstance.service_ip = serviceIP
        }

        install "install.groovy"
        start "start.groovy"

        locator {
            uuid=context.attributes.thisInstance.uuid
            i=0
            while (uuid==null){
                Thread.sleep 1000
                uuid=context.attributes.thisInstance.uuid
                if (i>20){
                    println "LOCATOR TIMED OUT"
                    break
                }
                i=i+1
            }
            if(i>21)return null

            i=0
            def pids=[]
            while(pids.size()==0){
                pids=ServiceUtils.ProcessUtils.getPidsWithQuery("Args.*.ct=${uuid}");
                i++;
                if(i>20){
                    println "PROCESS NOT DETECTED"
                    break
                }
                Thread.sleep(1000)
            }
            return pids
        }
        details {
            def currPublicIP = context.getPublicAddress()
            def demoURL = "http://${currPublicIP}:8080/"
            return [
                    "GigaSpaces Interactive Shell URL":"<a href=\"${demoURL}\" target=\"_blank\">${demoURL}</a>"
            ]
        }
    }
    customCommands ([
        //Public entry points

        "update-hosts": {String...line ->
            util.invokeLocal(context,"_update-hosts", [
                    "update-hosts-hostsline":line
            ])
        },

        //Actual parameterized calls
        "_update-hosts"	: "commands/update-hosts.groovy"
    ])

    network {
        template "APPLICATION_NET"
        accessRules {
            incoming ([
                    accessRule {
                        type "PUBLIC"
                        portRange "${bf_uiPort}"
                    },
                    accessRule {
                        type "APPLICATION"
                        portRange "${bindPort}"
                    }
            ])
        }
    }
}