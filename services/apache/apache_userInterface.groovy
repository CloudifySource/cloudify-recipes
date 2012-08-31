userInterface {
	metricGroups = ([
		metricGroup {

			name "process"

			metrics([
				"Total Process Cpu Time"					
			])
		} 
	])

	widgetGroups = ([								
		widgetGroup {
			name "Total Process Cpu Time"
			widgets([
				balanceGauge{metric = "Total Process Cpu Time"},
				barLineChart {
					metric "Total Process Cpu Time"
					axisYUnit Unit.REGULAR
				}
			])
		}
	])
}