
service { 
    extend "../puppet"
    name "puppet-agent"
    icon "puppet.png"

    customCommands([
        "run_agent": { tags ->
            tagAry = tags.any() ? tags.split(",") : []
            PuppetBootstrap.getBootstrap(context:context).puppetAgent(tagAry)
        }
    ])
}
