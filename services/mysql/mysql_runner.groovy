/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import java.text.SimpleDateFormat

def static runMysqlQuery(binFolder,execFile,osName,currQuery,dbName,dbUser,debugMsg,outputpropertyName,displayOutputProperty) {	
	return runMysqlQuery(binFolder,execFile,osName,currQuery,dbName,dbUser,"",debugMsg,outputpropertyName,displayOutputProperty)
}

def static runMysqlQuery(binFolder,execFile,osName,currQuery,dbName,dbUser,dbUserPassword,debugMsg,outputpropertyName,displayOutputProperty) {	

  def outputPropertyStr
  def builder
  def currPassword = ""

  if ( dbUserPassword.length() > 0  ) {
	currPassword = "-p${dbUserPassword}"
  }

  
  try {		
	builder = new AntBuilder()
	builder.sequential {		 
      echo(message:"runMysqlQuery: os ${osName}: ${debugMsg}")
      echo(message:"runMysqlQuery: ${binFolder}/${execFile} -u ${dbUser} ${currPassword} -D ${dbName} -e ${currQuery}")
      exec(executable:"${binFolder}/${execFile}", osfamily:"${osName}", outputproperty:"${outputpropertyName}") {	  
	    arg(line:"-u ${dbUser} ${currPassword} -D ${dbName} -e ${currQuery}")
     }	
   }		
  } 
  catch (Exception ioe) {
	println "runMysqlQuery: Connection Failed!"
	ioe.printStackTrace();
  }
  
  outputPropertyStr = builder.project.properties."${outputpropertyName}"
  if ( displayOutputProperty ) {
	println "runMysqlQuery: outputproperty (${outputpropertyName}) is : ${outputPropertyStr}"
  }
  
  println "runMysqlQuery: Ended"
  return outputPropertyStr
}	


def static importMysqlDB(binFolder,execFile,osName,importedFile,dbName,dbUser,debugMsg,outputpropertyName,displayOutputProperty) {	

  def outputPropertyStr
  def builder
  try {		
	builder = new AntBuilder()
	builder.sequential {		 
      echo(message:"importMysqlDB: ${debugMsg}")
      exec(executable:"${binFolder}/${execFile}", osfamily:"${osName}" ,input:"${importedFile}", outputproperty:"${outputpropertyName}") {       
		arg(value:"-u")
		arg(value:"${dbUser}")
		arg(value:"${dbName}")
	  }
   }		
  } 
  catch (Exception ioe) {
	println "importMysqlDB: Connection Failed!"
	ioe.printStackTrace();
  } 
  
  outputPropertyStr = builder.project.properties."${outputpropertyName}"
  if ( displayOutputProperty ) {
	println "importMysqlDB: outputproperty (${outputpropertyName}) is : ${outputPropertyStr}"
  }  
  println "importMysqlDB: Ended"
  return outputPropertyStr
}	


def static runMysqlAdmin(binFolder,execFile,osName,actionName,dbName,dbUser,debugMsg,outputpropertyName,displayOutputProperty) {	

  def outputPropertyStr
  def builder
  try {		
	builder = new AntBuilder()
	builder.sequential {		 
      echo(message:"runMysqlAdmin: ${debugMsg}")
      exec(executable:"${binFolder}/${execFile}", osfamily:"${osName}", outputproperty:"${outputpropertyName}") {	
		arg(value:"-u")
		arg(value:"${dbUser}")
		arg(value:"${actionName}")
		arg(value:"${dbName}")
	  }
   }		
  } 
  catch (Exception ioe) {
	println "runMysqlAdmin: Connection Failed!"
	ioe.printStackTrace();
  } 
  
  outputPropertyStr = builder.project.properties."${outputpropertyName}"
  if ( displayOutputProperty ) {
	println "runMysqlAdmin: outputproperty (${outputpropertyName}) is : ${outputPropertyStr}"
  } 
  
  println "runMysqlAdmin: Ended"
  return outputPropertyStr
}	

def static runMysqlDump(binFolder,execFile,osName,actionArgs,dbName,dbUser,debugMsg,dumpFolder,dumpPrefix) {		

  try {		
      
	def dbFlag
	if ( "${dbName}" == "" ) {
		dbFlag = "--all-databases"
	}
	else {
		dbFlag = "--databases ${dbName}"
	}
	
	def argsLine = "-u ${dbUser} ${actionArgs} ${dbFlag} "
		
	def currMillis=System.currentTimeMillis()
	def currTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(currMillis)
	
	def baseDumpName = "${dumpPrefix}${currTime}"	
	def sqlFileName = "${baseDumpName}.sql"
	def baseRootName = "${dumpFolder}/${baseDumpName}"
	def dumpSqlFullPath = "${baseRootName}.sql"
	def dumpZipFullPath = "${baseRootName}.zip"
	  
	def builder = new AntBuilder()
	builder.sequential {		 
      echo(message:"runMysqlDump: ${debugMsg}")
      exec(executable:"${binFolder}/${execFile}", osfamily:"${osName}", output:"${dumpSqlFullPath}") {
		arg(line:"${argsLine}")
	  }	  
	  zip(destFile:"${dumpZipFullPath}", basedir: "${dumpFolder}" ,includes:"${sqlFileName}", update:true )	    			
	  delete(file:"${dumpSqlFullPath}")
   }		
  } 
  catch (Exception ioe) {
	println "runMysqlDump: Connection Failed!"
	ioe.printStackTrace();
  } 
  
  println "runMysqlDump: Ended"
}	

def static importFileToDB(binFolder,osConfig,currOsName,currActionDbName,currImportZip,importedFile,importedFileUrl,builder,context,currActionUser,currDebugMsg,config) {
	builder.sequential {	  
		echo(message:"importFileToDB: Getting ${importedFileUrl} to ${currImportZip} ...")
		get(src:"${importedFileUrl}", dest:"${currImportZip}", skipexisting:true)
		echo(message:"importFileToDB: Unzipping ${currImportZip} to ${context.serviceDirectory} ...")
		unzip(src:"${currImportZip}", dest:"${context.serviceDirectory}", overwrite:true)	 
	}

	def fullPathToImport="${context.serviceDirectory}/${importedFile}"
	def currImportFile = new File(fullPathToImport) 
	def importText = currImportFile.text
	println "importFileToDB: Replacing REPLACE_WITH_DB_NAME with ${config.dbName} in ${context.serviceDirectory}/${importedFile} ..."
	currImportFile.text = importText.replace("REPLACE_WITH_DB_NAME",currActionDbName)	   

	importMysqlDB(binFolder,osConfig.mysqlProgram,currOsName,fullPathToImport,currActionDbName,currActionUser,currDebugMsg,"dummy",false)
}

def static showMasterStatus(context,binFolder,execFile,currOsName,currQuery,currActionDbName,currActionUser,currDebugMsg,outputpropertyName,displayOutputProperty) {
              
	def binLogData = runMysqlQuery(binFolder,execFile,currOsName,currQuery,currActionDbName,currActionUser,currDebugMsg,outputpropertyName,displayOutputProperty)

	def binLogName="mysql-bin"
	
	println "showMasterStatus: binLogData is "+binLogData
	println "showMasterStatus: binLogData length is "+binLogData.length()
	
	if  ( binLogData.length() > 0 && binLogData.contains("Position") ) { 
		println "showMasterStatus: splitting binLogData ... "
		def rawData = binLogData.split(binLogName)[1]
		println "showMasterStatus: splitting rawData ... "
		def dataArr = rawData.split()

		println "showMasterStatus: setting binLog ... "
		def binLog=binLogName+dataArr[0].trim()
		
		println "showMasterStatus: setting logPos ... "
		def logPos=dataArr[1].trim()
		
		println "showMasterStatus: masterBinLogFile is "+binLog
		println "showMasterStatus: masterBinLogPos is "+logPos

		context.attributes.thisApplication["masterBinLogFile"] = binLog
		context.attributes.thisApplication["masterBinLogPos"]  = logPos
		return true
	}
	else {
		println "showMasterStatus: master is NOT ready yet"
		return false
	}
}	
