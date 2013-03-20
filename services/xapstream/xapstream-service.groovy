service {
	extend "../xap9.1-lite"
	name "xapstream"
	lifecycle {
		postStart "postStart.groovy"
	}
}
