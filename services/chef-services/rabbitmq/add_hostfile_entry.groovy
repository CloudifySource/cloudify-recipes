import static Shell.*
/**
 * Append an entry to the hosts file for the newly added rabbitmq instance
 * @author lchen
 *
 */
println "add_hostfile_entry.groovy: start"

config=new ConfigSlurper().parse(new File('rabbitmq-service.properties').toURL())
hostsFileEntry = "\n" + args[0] + "\t" + args[1]
//def hostsFile = new File(config.hostsFile)
//hostsFile.append("\n" + hostsFileEntry)

sudo("echo -e \"${hostsFileEntry}\" >> ${config.hostsFile}")

println "add_hostfile_entry.groovy: end"
