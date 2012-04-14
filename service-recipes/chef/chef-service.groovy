service {
    lifecycle {
        preInstall "chef_preInstall.groovy" 
        install "chef_install.groovy"
    }
    customCommands([
        "run_chef": { 
            bootstrap = ChefBootstrap.getBootstrap(
                serverURL:context.attributes.thisApplication["chefServerURL"]
            ).getBootstrap().runClient()
        }
    ])
}
