service {

 def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}","boinc-service.properties").toURL())

 //---helper methods---  
 def exec = {command->
     
    p = command.execute(null, new File("${context.serviceDirectory}"))
    p.waitFor()
    if (p.exitValue() != 0){
      throw new Exception("""
        "${command}" 
        exit: ${p.exitValue()}
        stderr: ${p.err.text}
        srdout: ${p.in.text}""")
    }
    return p.in.text;
 }

 def boinccmdPath = "/usr/bin/boinccmd"

 def projectAttach= {url,auth->
    exec("${boinccmdPath} --project_attach ${url} ${auth}")
 }

 def projectDetach= {url->
    exec("${boinccmdPath} --project ${url} detach")
 }

 def getProjectStatus= {url->
    exec("${boinccmdPath} --get_project_status ${url}")
 }

 def setRunMode = {mode->
    exec("${boinccmdPath} --set_run_mode ${mode}")
 }

 def quit = { 
    exec("${boinccmdPath} --quit")
 }

 def getState = { 
    exec("${boinccmdPath} --get_state")
 }

 def boinc = {
    exec("boinc")
 }
 //---helper methods---  
 
 name "boinc"
 icon "boinc_logo_3dl_74x34.png"
 type "APP_SERVER"
 numInstances 1

 compute {
 		template "SMALL_UBUNTU"
 }

 lifecycle {
    preInstall {
      if (config.weakAccountKey.contains("WEAK")) {
	throw new IllegalStateException(
            "Please vist " + config.projectUrl+"/weak_auth.php to get your weak account "+
            "key and paste it into  boinc-service.properties");
      }
    }
  
    install ([
      "Linux" : "boinc_install_ubuntu12.sh",
    ])
	
    start ([
      "Linux" : "boinc_start.sh",
    ])

    startDetectionTimeoutSecs 240
    startDetection {
         
      try {
        // check boinc is ready and disable forked child processes
        // cloudify does not scan process tree until startDetection returns true
        setRunMode("never")
        return true;
      }
      catch (Exception e) {
        //boinc not receiving requests yet
        println e.message
        return false;
      }
    }

    postStart {

      if (getProjectStatus(config.projectUrl).contains(config.projectUrl)) {
        projectDetach(config.projectUrl)
      }
      projectAttach(config.projectUrl,config.weakAccountKey)
     
      setRunMode "always"
    }

    preStop {
	 quit
    }

    def lastMetricsTimestamp = 0;
    def lastMetrics = [];

    monitors {
      def periodMillis = System.currentTimeMillis() - lastMetricsTimestamp;
      if (periodMillis < config.monitorThrottelingSeconds * 1000) {
         //throtelling requests. returning stale data.
         return lastMetrics;
      }
       
      state = getState()
      // state parsing assumes one project
      hostTotalCredit = state.find(/.*host_total_credit:(.*)/) {
	match,credit -> credit as Double
      }
      activeTasks = state.count("active_task_state: 1")
      lastMetrics= [
                "Active Tasks":activeTasks,
                "Host Total Credit":hostTotalCredit
      ]
      lastMetricsTimestamp = System.currentTimeMillis();
      return lastMetrics;
   }
 }

  userInterface {
    metricGroups = ([
      metricGroup {
        name "boinc"
        metrics([
          "Active Tasks",
          "Host Total Credit"
        ])
      }
    ])

  widgetGroups = ([
    widgetGroup {
      name "Active Tasks"
        widgets ([
           balanceGauge{metric = "Active Tasks"},
           barLineChart{
             metric "Active Tasks"
             axisYUnit Unit.REGULAR
           }
        ])
    },
    widgetGroup {
      name "Host Total Credit"
        widgets ([
           balanceGauge{metric = "Host Total Credit"},
           barLineChart{
             metric "Active Tasks"
             axisYUnit Unit.REGULAR
           }
        ])
    }])
  }
}
