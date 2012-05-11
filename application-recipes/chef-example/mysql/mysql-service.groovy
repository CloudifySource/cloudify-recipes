service {
    extend "../chef"
    lifecycle {
        install "mysql_install.groovy"
    }
    compute {
        template "MEDIUM_LINUX"
    }
}
