service {
    extend "../generic"
    name "tty.js"

    lifecycle {
        install "tty.js_install.groovy"
        start "tty.js_start.groovy"
        preStop "tty.js_preStop.groovy"
        details {
            def currPublicIP = context.getPublicAddress()

            def interactiveShellURL = "http://${currPublicIP}:8080"


            return [
                    "GigaSpaces Interactive Shell URL":"<a href=\"${interactiveShellURL}\" target=\"_blank\">${interactiveShellURL}</a>"
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
