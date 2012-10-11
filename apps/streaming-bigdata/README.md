[![Build Status](https://secure.travis-ci.org/Gigaspaces/rt-analytics.png)](http://travis-ci.org/Gigaspaces/rt-analytics)

#  Real Time Analytics for Big Data with GigaSpaces

## In Depth Tutorial 

An in-depth tutorial for this application can be found [here](http://wiki.gigaspaces.com/wiki/display/XAP91/Your+First+Real+Time+Big+Data+Analytics+Application).

## Usecase

Twitter timeline analytics ! 

## Motivation

This example demonstrates a solution architecture for real-time analytics for big data based on GigaSpaces.

The solution demonstrates feeding in live tweets from Twitter's public timeline into the system using [Spring Social](http://www.springsource.org/spring-social),
then processing them in realtime, calculating counters for realtime analysis, using GigaSpaces XAP,
and also converting to document-based representation and storing in a document-based NoSQL database ([Cassandra](http://cassandra.apache.org/))
for historical research analytics on the tweets.
Orchestration of the full stack of the end-to-end solution is performed using [GigaSpaces Cloudify](http://www.gigaspaces.com/cloudify).

## Structure

The example has three modules:

* The `Common` module includes all items that are shared between the feeder and the processor modules.

* The `Processor` module is a processing unit with performs the real-time workflow of processing the data.
    The processing of data objects is done using event containers.
* The `Feeder` module is a processing unit that contains two feeders:
	TweetFeeder feeding in tweets from Twitter's public timeline using Spring Social, converting them to a canonical 
	Document-style representation, and writes them to the remote space ,which are in turn processed by the processor module.
	Feeder is a simulation feeder for testing purposes, which simulates tweets locally, avoiding the need to connect 
	to Twitter (e.g. if there's no internet connection).
	Having the feeder as a PU enables dynamically deploying multiple instances of it to scale with the tweet load changes.
* The `rt_app` directory contains the recipes and other scripts required to automatically deploy, monitor and manage the solution 
	together with Cassandra back-end automatically.
	    
## Build and deployment

The example uses Maven 2 as its build tool. Just follow the standard build lifecycle phases to construct the JARs for the 
processor and the feeder PUs.

`mvn install`

### Manual deployment:
You can run the example manually by launching Cassandra (see instructions below) and deploying the PUs onto the service grid using 
the GS-UI or GS CLI (see instructions below).

### Automatic deployment:
You can use GigaSpaces Cloudify (see instructions below) to automatically download, configure and deploy Cassandra, bootstrap the 
Service Grid and deploy the PUs to it, while enforcing the inter-dependencies between the various services, and then monitor the 
deployment via the Web Console. Cloudify will also take care of handling fail-over and to scale in/out based on the load as per the 
defined scaling rules.

In order to manually deploy the example onto the Service Grid:

* first install, configure, launch Cassandra DB, 
* run the cassandra_schema script to define keyspace and column family.
* run the service grid: run gs-agent which will start  a GSM and *two* GSCs will need to be started (note, we need two GSCs because of the SLA defined 
within the processor module). 
* Next, build the feeder and processor PUs using Maven. This will generate the processor.jar and the feeder.jar.
* Run the GS-UI in order to deploy the jars and see the PU instances deployed.


