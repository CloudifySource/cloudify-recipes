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
        postInstall "butterfly_postInstall.groovy"
        start "butterfly_start.groovy"
        stop "butterfly_stop.groovy"
        details {
            def currPublicIP = context.getPublicAddress()
            def demoURL = "http://${currPublicIP}:8080/wd/${context.serviceDirectory}/"
            return [
                    "GigaSpaces Interactive Shell URL":"<a href=\"${demoURL}\" target=\"_blank\">${demoURL}</a>"
            ]
        }
    }
    network {
        template "APPLICATION_NET"
        accessRules {[
            incoming ([
                    accessRule {
                        type "PUBLIC"
                        portRange 8080
                    },
                    accessRule {
                        type "APPLICATION"
                        portRange "4242-4342"
                    },
                    accessRule {
                        type "APPLICATION"
                        portRange "1-65000"
                    }
            ]),
            outgoing ([
                    accessRule {
                        type "APPLICATION"
                        portRange "1-65000"
                        target "0.0.0.0/0"
                    }
            ])
        ]}
    }
}