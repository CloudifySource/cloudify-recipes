service {
    extend "../generic"
    name "tty.js"

    lifecycle{
        details {
            def currPublicIP = context.getPublicAddress()

            def shellURL = "http://${currPublicIP}:8080"
            return [
                    "Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
            ]
            return [
                    "Shell":"<a href=\"${shellURL}\" target=\"_blank\">${shellURL}</a>"
            ]
        }
    }
}
