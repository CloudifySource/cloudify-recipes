service {
	extend "../../xap9.x"
	name "xapstream"
	lifecycle {
		postStart "postStart.groovy"
	}
}
