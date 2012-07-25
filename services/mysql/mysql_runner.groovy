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
def static runMysqlQuery(binFolder,execFile,osName,currQuery,dbName,dbUser,debugMsg) {	

  try {		
	def builder = new AntBuilder()
	builder.sequential {		 
      echo(message:"runMysqlQuery: os ${osName}: ${debugMsg}")
      echo(message:"runMysqlQuery: ${binFolder}/${execFile} -u ${dbUser} -D ${dbName} -e ${currQuery}")
      exec(executable:"${binFolder}/${execFile}", osfamily:"${osName}") {	  
	    arg(line:"-u ${dbUser} -D ${dbName} -e ${currQuery}")
     }	
   }		
  } 
  catch (Exception ioe) {
	println "runMysqlQuery: Connection Failed!"
	ioe.printStackTrace();
  } 
  
  println "runMysqlQuery: Ended"
}	


def static importMysqlDB(binFolder,execFile,osName,importedFile,dbName,dbUser,debugMsg) {	

  try {		
	def builder = new AntBuilder()
	builder.sequential {		 
      echo(message:"importMysqlDB: ${debugMsg}")
      exec(executable:"${binFolder}/${execFile}", osfamily:"${osName}" ,input:"${importedFile}") {        
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
  
  println "importMysqlDB: Ended"
}	


def static runMysqlAdmin(binFolder,execFile,osName,actionName,dbName,dbUser,debugMsg) {	

  try {		
	def builder = new AntBuilder()
	builder.sequential {		 
      echo(message:"runMysqlAdmin: ${debugMsg}")
      exec(executable:"${binFolder}/${execFile}", osfamily:"${osName}") {        
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
  
  println "runMysqlAdmin: Ended"
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
	println "groovy: Replacing REPLACE_WITH_DB_NAME with ${config.dbName} in ${context.serviceDirectory}/${importedFile} ..."
	currImportFile.text = importText.replace("REPLACE_WITH_DB_NAME",currActionDbName)	   

	importMysqlDB(binFolder,osConfig.mysqlProgram,currOsName,fullPathToImport,currActionDbName,currActionUser,currDebugMsg)
}	
