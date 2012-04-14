service {
    lifecycle {
        preInstall { 
            ChefBootstrap.getBootstrap(
                serverURL:context.attributes.thisApplication["chefServerURL"]
            ).install()
        }
        install {
            bootstrap = ChefBootstrap.getBootstrap(
                serverURL:context.attributes.thisApplication["chefServerURL"]
            ).getBootstrap().runClient(runList) // override the runList somewhere.
        }
    }
    customCommands([
        "run_chef": { 
            bootstrap = ChefBootstrap.getBootstrap(
                serverURL:context.attributes.thisApplication["chefServerURL"]
            ).getBootstrap().runClient()
        }
    ])
}
