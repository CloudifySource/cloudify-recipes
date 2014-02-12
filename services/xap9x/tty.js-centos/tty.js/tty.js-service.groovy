service {
    extend "../generic"
    name "tty.js"

    lifecycle {
        install "tty.js_install.groovy"
        start "tty.js_start.groovy"

        details {
            def currPublicIP = context.getPublicAddress()

            def interactiveShellURL = "http://${currPublicIP}:8080"


            return [
                    "GigaSpaces Interactive Shell URL":"<a href=\"${interactiveShellURL}\" target=\"_blank\">${interactiveShellURL}</a>"
            ]
        }
    }
}
