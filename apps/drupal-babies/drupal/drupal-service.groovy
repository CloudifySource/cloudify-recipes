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
service {
	extend "../../../services/apache"	
	name "drupal"
	icon "drupal.jpg"
	lifecycle {
		postInstall "drupal_postInstall.groovy"
		
		details {
			def publicIP
			
			if (  context.isLocalCloud()  ) {
				publicIP =InetAddress.getLocalHost().getHostAddress()
			}
			else {
				publicIP =System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			}
				
			def hostAndPort="http://${publicIP}:${port}"
		
			return [				
				"Drupal Site": "<a href=\"${hostAndPort}\" target=\"_blank\">${hostAndPort}</a>" , 
				"admin": "admin" ,
				"password": "1234"
			]		
		}	
	}

	customCommands ([
		/* 
			This custom command enables users to upload a module ,theme or file to their site.
			Usage :  
				invoke drupal cmd upload [moudle|theme] zip_full_url_path
				  or
				invoke drupal cmd upload file file_full_url_path  destination_folder_relative_to_home
			Examples: 
				invoke drupal cmd upload module http://ftp.drupal.org/files/projects/views-7.x-3.5.zip
				invoke drupal cmd upload module http://my1stStorageSite.com/myNewModule-7.x-1.4.zip
				invoke drupal cmd upload theme http://ftp.drupal.org/files/projects/sasson-7.x-2.7.zip
				invoke drupal cmd upload theme http://my2ndStorageSite.com/myNewTheme-7.x-2.2.zip
				invoke drupal cmd upload file http://my3rdStoragSite.com/myFile sites/default/files/pictures
		*/
	
		"cmd" : "drupal_commands.groovy" 
	])
	
}