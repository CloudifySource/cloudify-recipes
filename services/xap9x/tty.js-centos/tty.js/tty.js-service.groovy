service {
    extend "../generic"
    name "tty.js"

    lifecycle{
        details {
            def currPublicIP = context.getPublicAddress()

            def shellURL = "\"http://${currPublicIP}:8080"
            return [
                    "Shell":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
            ]
        }
    }
}
