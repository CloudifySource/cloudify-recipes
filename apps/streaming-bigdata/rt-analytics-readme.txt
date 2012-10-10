=====================================================================================
=== Real Time Analytics for Big Data with GigaSpaces - Twitter analytics Use Case ===
=====================================================================================

1. MOTIVATION

This example demonstrates a solution architecture for real-time analytics for big data based on GigaSpaces.

The solution demonstrates feeding in live tweets from Twitter's public timeline into the system using Spring Social,
then processing them in realtime, calculating counters for realtime analysis, using GigaSpaces XAP,
and also converting to Document-based representation and storing in a document-based NoSQL database (Cassandra)
for historical research analytics on the tweets.
Orchestration of the full stack of the end-to-end solution is performed using GigaSpaces Cloudify.

2. STRUCTURE

The example has three modules:

	a. The Common module includes all items that are shared between the feeder and the processor modules.
	b. The Processor module is a processing unit with performs the real-time workflow of processing the data.
	    The processing of data objects is done using event containers.
	c. The Feeder module is a processing unit that contains two feeders:
		TweetFeeder feeding in tweets from Twitter's public timeline using Spring Social, converting them to a canonical 
		Document-style representation, and writes them to the remote space ,which are in turn processed by the processor module.
		Feeder is a simulation feeder for testing purposes, which simulates tweets locally, avoiding the need to connect 
		to Twitter (e.g. if there's no internet connection).
		Having the feeder as a PU enables dynamically deploying multiple instances of it to scale with the tweet load changes.
	d. The rt_app directory contains the recipes and other scripts required to automatically deploy, monitor and manage the solution 
		together with Cassandra back-end automatically.
	    
3. BUILD AND DEPLOYMENT

The example uses Maven 2 as its build tool. Just follow the standard build lifecycle phases to construct the JARs for the 
processor and the feeder PUs.

Manual deployment:
	You can run the example manually by downloading, installing, configuring and launching Cassandra (see instructions below), 
	launching the Service Grid, and deploying the PUs onto the service grid using the GS-UI or GS CLI (see instructions below).

Automatic deployment:
	You can use GigaSpaces Cloudify (see instructions below) to automatically download, configure and deploy Cassandra, bootstrap the 
	Service Grid and deploy the PUs to it, while enforcing the inter-dependencies between the various services, and then monitor the 
	deployment via the Web Console. Cloudify will also take care of handling fail-over and to scale in/out based on the load as per the 
	defined scaling rules.

In order to manually deploy the example onto the Service Grid:
first install, configure, launch Cassandra DB, and run the cassandra_schema script to define keyspace and column family.
then run the service grid: run gs-agent which will start 
a GSM and *two* GSCs will need to be started (note, we need two GSCs because of the SLA defined 
within the processor module). Next, build the feeder and processor PUs using Maven. This
will generate the processor.jar and the feeder.jar.
Run the GS-UI in order to deploy the jars and see the PU instances deployed.

in order to automatically deploy and manage via Cloudify:
run Cloudify CLI, bootstrap localcloud for running on your own machine (for deployment to cloud - check the documentation),
then execute install-application with path to the rt_app directory.

