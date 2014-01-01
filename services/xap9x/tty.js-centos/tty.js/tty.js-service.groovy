service {
    extend "../generic"
    name "tty.js"

    lifecycle {
        start "tty.js_start.groovy"
    }
}
