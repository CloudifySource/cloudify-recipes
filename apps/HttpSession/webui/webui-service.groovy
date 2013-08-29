service {
	name "webui"
	
	numInstances 1
	maxAllowedInstances 1
	
	compute {
		template "SMALL_LINUX"
	}

	lifecycle {
		install "webui-install.groovy"
		start "webui-start.groovy"
		startDetection {
			ServiceUtils.isPortFree(8099)
		}
		locator {
			[]
		}
	
	}	
}
