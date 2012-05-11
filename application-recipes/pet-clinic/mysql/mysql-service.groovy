service {
    extend "../../../service-recipes/chef"
    name "mysql"
    type "DB_SERVER"
    numInstances 1
    lifecycle {
        start "run.groovy"
    }
    compute {
        template "MEDIUM_LINUX"
    }
}
