service {
    extend "../generic"
    name "tty.js"

    lifecycle {
        install "tty.js_install.groovy"
        start "tty.js_start.groovy"
    }
}
