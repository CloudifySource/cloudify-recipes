application {
    name = "pet-clinic"
    service {
        name = "chef-server"
    }
    service {
        name = "app"
        dependsOn = ["mysql", "chef-server"]
    }
    service {
        name = "mysql"
        dependsOn = ["chef-server"]
    }
}
