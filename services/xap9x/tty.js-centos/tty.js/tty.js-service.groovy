service {
    extend "../generic"
    name "tty.js"

    lifecycle {
        install "tty.js_install.groovy"
        start "tty.js_start.groovy"

        details {
            def currPublicIP = context.getPublicAddress()

            def interactiveShellURL = "http://${currPublicIP}:8080"
            def benchmarkShellURL = "http://${currPublicIP}:8081"
            def shellURL = "http://${currPublicIP}:8082"


            return [
                    "GigaSpaces Interactive Shell URL":"<a href=\"${applicationURL}\" target=\"_blank\">${interactiveShellURL}</a>",
                    "Benchmark URL":"<a href=\"${applicationURL}\" target=\"_blank\">${benchmarkShellURL}</a>",
                    "GigaSpaces URL":"<a href=\"${applicationURL}\" target=\"_blank\">${shellURL}</a>"
            ]
        }
    }
}
