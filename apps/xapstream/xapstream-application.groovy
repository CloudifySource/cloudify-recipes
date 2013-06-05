application {
	name='xapstream'

	service {
		name = 'zookeeper'
	}

	service {
		name = 'storm-nimbus'
		dependsOn = ['zookeeper']
	}

	service {
		name = 'storm-supervisor'
		dependsOn = ['zookeeper','storm-nimbus']
	}

	service {
		name = 'xapstream'
	}

	service {
		name = 'streamdriver'
	}
}
