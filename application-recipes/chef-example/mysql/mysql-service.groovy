service {
    extend "../chef"
    lifecycle {
        install "mysql_install.groovy"
        start { while(true) { sleep 5000 } }
    }
    compute {
        template "MEDIUM_LINUX"
    }
}
