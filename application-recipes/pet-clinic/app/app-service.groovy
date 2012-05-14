service {
    extend "../../../service-recipes/chef"
    name "app"
    type "APP_SERVER"
    numInstances 1
    
    lifecycle {
        start "run.groovy" 
		startDetection {
			!ServiceUtils.arePortsFree([8080] )
		}
    }
}
