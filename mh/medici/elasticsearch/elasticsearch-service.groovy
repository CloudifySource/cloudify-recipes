service {
	extend "../../services/elasticsearch"
	numInstances 2

	compute {
		template "SMALL_LINUX"
	}
}