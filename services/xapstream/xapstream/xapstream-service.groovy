service {
	extend "../../xap9.1-lite"
	name "xapstream"
	lifecycle {
		postStart "postStart.groovy"

		details {
			def currPublicIP
			
			if (  context.isLocalCloud()  ) {
				currPublicIP = InetAddress.localHost.hostAddress
			}
			else {
				currPublicIP =context.getPublicAddress()
			}
	
			def applicationURL = "http://${currPublicIP}:8999"
			def wcuiURL = "http://${currPublicIP}:8081/wordcount-ui/ui.htm"
		
			return [
					"Management UI":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>",
					"Wordcount UI":"<a href=\"${wcuiURL}\" target=\"_blank\">${wcuiURL}</a>"

				]
		}
	}

}
