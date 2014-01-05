service {
    extend "../generic"
    name "tty.js"

    lifecycle {
        install "tty.js_install.groovy"
        start "tty.js_start.groovy"

        details {
            def currPublicIP = context.getPublicAddress()

            def applicationURL = "http://${currPublicIP}"

            return [
                    "GigaSpaces Interactive Shell URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}:8080</a>",
                    "Benchmark URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}:8081</a>",
                    "GigaSpaces URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}:8082</a>"
            ]
        }
    }
}
