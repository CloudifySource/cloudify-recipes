##XAP/Storm Wordcount demo

This app contains all services needed to demonstrate the XAP/Storm integration using a simple word counting Storm topology.  Note, currently there is a precondition:  A MACHINE TEMPLATE NAMED "MEDIUM_LINUX" (at least 4GB) MUST EXIST IN THE TARGET CLOUD.

After the app is installed via "install-application", the demo is ready to go. 

1. Open the wordcount-ui.  This provides a graphical representation of the data flowing through the system.  Locate the public IP of the "xap-container" VM, and in a web browser access:  http://<container-ip>:8081/wordcount-ui/ui.htm.

2. Run a custom command to drive load : invoke streamdriver wordcount-demo wordstream <seconds> <tuples/sentences/sec>


The custom command will return immediately, but the load will be generated in the background for as many seconds as specified.

The "streamdriver" service connects to / creates a stream in the XAP9x instance in the cloud, and write sentences from the novel "Moby Dick".  From there, Storm connects via the XAP streaming API and starts processing the sentences, each sentence representing an input tuple.  The Storm topology then splits and aggregates the sentences into words and counts them, storing them in state objects in XAP.  While all this is occuring, the UI is calling a REST API on XAP to gather statistics and display them using D3 for the amusement of an audience.
