application {
    name = "pet-clinic"
    service {
        name = "chef-server"
    }
    service {
        name = "app"
        dependsOn = ["chef-server", "mysql"]
    }
    service {
        name = "mysql"
        dependsOn = ["chef-server"]
    }
}
