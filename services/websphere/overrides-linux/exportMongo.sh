#!/bin/sh

export MONGO_HOST=REPLACE_WITH_MONGO_HOST
export MONGO_PORT=REPLACE_WITH_MONGO_PORT


exit

// These should be in websphere.properties

exportMongoFileName="exportMongo.sh"
exportMongoScript="${installBin}/${exportMongoFileName}"


// These should be in websphere_start.groovy

new AntBuilder().sequential {	
	copy(todir: "${websphereConfig.installBin}/", file:"overrides-linux/${websphereConfig.exportMongoFileName}", overwrite:true)
}

println "Setting Mongos host and port in ${websphereConfig.exportMongoScript} ..."
exportMongoFile = new File("${websphereConfig.exportMongoScript}") 
exportMongoFileText=exportMongoFile.text
exportMongoFileText=exportMongoFileText.replace("REPLACE_WITH_MONGO_HOST","${mongoServiceHost}") 
exportMongoFileText=exportMongoFileText.replace("REPLACE_WITH_MONGO_PORT","${mongoServicePort}") 
exportMongoFile.text=exportMongoFileText

