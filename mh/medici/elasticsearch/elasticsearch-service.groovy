service {
	extend "../../services/elasticsearch"
	numInstances 1

	compute {
		template "SMALL_LINUX"
	}
}