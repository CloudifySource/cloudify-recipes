service {
	name "xapstream-pus"
	lifecycle {
		postStart "install.groovy"


		shutdown "shutdown.groovy"
	}
}
