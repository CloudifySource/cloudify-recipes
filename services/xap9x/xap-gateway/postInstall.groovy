//set up nat mapper class

import groovy.util.ConfigSlurper;

config = new ConfigSlurper().parse(new File("xap-gateway-service.properties").toURL())

//copy file, append property
new AntBuilder().sequential{
	copy(file:"lib/nat-mapper.jar",todir:"${config.installDir}/${config.name}/lib/required");
}
