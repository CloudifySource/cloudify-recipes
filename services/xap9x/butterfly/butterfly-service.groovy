import org.cloudifysource.utilitydomain.context.ServiceContextFactory
service {
    name "butterfly"
    icon "butterfly.gif"
    type "APP_SERVER"

    numInstances 1

    compute {
        template computeTemplate
    }
    lifecycle{
        install "butterfly_install.groovy"
        start "butterfly_start.groovy"
        stop "butterfly_stop.groovy"
        details {
            def currPublicIP = context.getPublicAddress()
            def xapInstallationDir = "${context.serviceDirectory}/${installDir}/${name}/"
            def interactiveShellURL = "http://${currPublicIP}:8080/wd/${xapInstallationDir}/bin"
            def groovyShellURL = "http://${currPublicIP}:8080/wd/${xapInstallationDir}/tools/groovy/bin"
            return [
                    "GigaSpaces Interactive Shell URL":"<a href=\"${interactiveShellURL}\" target=\"_blank\">${interactiveShellURL}</a>",
                    "Groovy Interactive Shell URL":"<a href=\"${groovyShellURL}\" target=\"_blank\">${groovyShellURL}</a>"
            ]
        }
    }
    network {
        template "APPLICATION_NET"
        accessRules {
            incoming ([
                    accessRule {
                        type "PUBLIC"
                        portRange 8080
                    },
                    accessRule {
                        type "APPLICATION"
                        portRange "4242-4342"
                    }
            ])
        }
    }
}