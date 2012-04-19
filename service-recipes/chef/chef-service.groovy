service {
    lifecycle {
        preInstall "chef_preInstall.groovy" 
        install "chef_install.groovy"
    }
    customCommands([
        "run_chef": "chef_run.groovy"
    ])
}
