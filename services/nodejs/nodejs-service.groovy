//todo when stopping, the processes just detach from the gs tree and are not dying


service {
    name "nodejs"
    icon "nodejs.gif"
    type "WEB_SERVER"

    elastic true
    numInstances 1
    minAllowedInstances 1
    maxAllowedInstances 2

//    def portIncrement = context.isLocalCloud() ? context.getInstanceId() - 1 : 0
//    def currHttpPort = port + portIncrement

//    compute {
//        template "SMALL_LINUX"
//    }

    lifecycle {
        install "nodejs_install.groovy"
        start "nodejs_start.groovy"

        startDetectionTimeoutSecs 10
        startDetection { return !ServiceUtils.ProcessUtils.getPidsWithName("nodejs").isEmpty() }
        locator { return ServiceUtils.ProcessUtils.getPidsWithName("nodejs") }

//        monitors {
//            def returnMessage = ""
//
//            def url = new URL("http://localhost:1337/monitoring/hits")
//            def connection = url.openConnection()
//            connection.setRequestMethod("GET")
//            connection.connect()
//
//            if (connection.responseCode == 200 || connection.responseCode == 201){
//                returnMessage = connection.content.text
//            }
//
//            return ["Hits":returnMessage]
//        }
    }

//    userInterface {
//        metricGroups = ([
//                metricGroup {
//                    name "process"
//                    metrics([
//                            "Total Process Cpu Time"
//                    ])
//                },
//                metricGroup {
//                    name "Monitoring"
//                    metrics([
//                            "Hits"
//                    ])
//                }
//        ])
//
//        widgetGroups = ([
//                widgetGroup {
//                    name "Total Process Cpu Time"
//                    widgets([
//                            balanceGauge {metric = "Total Process Cpu Time"},
//                            barLineChart {
//                                metric "Total Process Cpu Time"
//                                axisYUnit Unit.REGULAR
//                            }
//                    ])
//                },
//                widgetGroup {
//                    name "Monitoring"
//                    widgets([
//                            balanceGauge {metric = "Hits"},
//                            barLineChart {
//                                metric "Hits"
//                                axisYUnit Unit.REGULAR
//                            }
//                    ])
//                }
//        ])
//    }

//    network {
//        port = currHttpPort
//        protocolDescription = "HTTP"
//    }

//    scaleCooldownInSeconds 20
//    samplingPeriodInSeconds 1

    // Defines an automatic scaling rule based on "counter" metric value
//    scalingRules([
//            scalingRule {
//
//                serviceStatistics {
//                    metric "Total Requests Count"
//                    statistics Statistics.maximumThroughput
//                    movingTimeRangeInSeconds 20
//                }
//
//                highThreshold {
//                    value 1
//                    instancesIncrease 1
//                }
//
//                lowThreshold {
//                    value 0.2
//                    instancesDecrease 1
//                }
//            }
//    ])
}