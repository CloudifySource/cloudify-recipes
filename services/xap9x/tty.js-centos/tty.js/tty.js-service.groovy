service {
    extend "../generic"
    name "tty.js"

    lifecycle {
        install "tty.js_install.groovy"
        start "tty.js_start.groovy"

        details {
            def currPublicIP = context.getPublicAddress()

            def applicationURL = "http://${currPublicIP}:8080"

            return [
                    "Shell URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
            ]
        }
    }
}
