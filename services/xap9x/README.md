xap9x  --- BETA ---
=================

##XAP 9.x Recipe

### Overview

This folder contains service recipes that run GigaSpace XAP version 9.x.  The recipes consist of a management and container recipe.  When composed in an application recipe, the container recipe should start after the management recipe.

#####Recipe Installation

######Configuration
Both service recipes are configured via a standard service properties file.   The most critical configuration option is the <i>license</i> property, which needs to be set to a valid XAP license key (look in your gs-license.xml file).   The recipe will function without it, but the grid will be limited per the "lite" license (limited memory and instances).  The <i>lusPort</i> property needs to be the same for both container and management recipes.  If multiple instances of the recipe suite are installed simultaneously, this port must be different for each.

#####Installation
The only requirement for installation is that the management recipe be installed prior to the container recipe.  


###Recipe Details
#####Recipe #1 : xap-management
The <i>xap-management</i> recipe launches the XAP management processes: GSA, GSM and LUS, as well as the Web UI.  The LUS port is configurable in the recipe properties, and effectively identifies the cluster.  All clusters (including Cloudify itself) must have unique LUS ports.  

<i>xap-management</i> can support up to 2 instances.  Containers (GSCs) are deployed via the <i>xap-container</i> recipe.  <i>xap-management</i>, on starting, updates the hosts table on the containers (if any) via a custom command.  Containers likewise update their hosts tables on startup to avoid static IP configuration in the recipes.
Note that in localcloud mode the webui url is http://localhost:9099

The recipe provides a link to the XAP Web UI in the details section of the Cloudify UI.

The <i>xap-management</i> recipe launches a butterfly server by default (see <a href="#recipe-3-butterfly">butterfly</a> below).
Set the flag "butterflyEnabled" in the properties file to true/false to enable/disable launching butterfly server.
<br/>Url: http://ip:8081.

###### Custom Commands

The recipe provides several custom commands:

<dl>
<dt>deploy-pu</dt>
<dd> Deploys a stateful/grid processing unit.  Usage: <i>deploy-pu puurl schema partitions backups max-per-vm max-per-machine name</i>.  Arguments (all args are required):
<ul>
<li><i>name</i>: The deployd name for the processing unit.  Defaults to the pu file name unless overridden.</li>
<li><i>puurl</i>: A URL where the processing unit jar can be found</li>
<li><i>schema</i>: The cluster schema (e.g. partitioned-sync2backup)</li>
<li><i>partitions</i>: The number of partitions. Ignored if not partitioned.</li>
<li><i>backups</i>: The number of backups per partition. Ignored if not partitioned.</li>
<li><i>max-per-vm</i>: Maximum instances per JVM/container.  See <a href="http://wiki.gigaspaces.com/wiki/display/XAP96/Configuring+the+Processing+Unit+SLA">here</a> for details</li>
<li><i>max-per-machine</i>: Maximum instances per physical machine/cloud vm.   See <a href="http://wiki.gigaspaces.com/wiki/display/XAP96/Configuring+the+Processing+Unit+SLA">here</a> for details</li>
</dd>
<dt>deploy-pu-basic</dt>
<dd>A convenience command that provides defaults to deploy-pu for a basic installation. Arguments (all args are required):
<ul>
<li><i>puurl</i>: A URL where the processing unit jar can be found</li>
</ul>
Notes: deploys a non-partitioned, single instance cluster.  Useful for simple applications or testing.
</dd>
<dt>deploy-grid</dt>
<dd>Deploys a space.  Usage: <i>deploy-grid spaceName schema partitions backups max-per-vm max-per-machine</i>.
<dd>Notes: see <i>deploy-pu</i> for argument meanings.</dd>
<dt>deploy-grid-basic</dt>
<dd>Deploys a space with one primary and one backup instances.  Usage: <i>deploy-grid-basic spaceName</i>.
<dt>undeploy-grid</dt>
<dd>Undeploys a grid by name.  Usage: <i>undeploy-grid name.</i>  Args:</dd>
<ul>
<li><i>name</i>: The name of the grid as assigned in the <i>deploy-grid</i> command.</li>
</ul>
<dt>deploy-gateway</dt>
<dd>Creates and deploys a WAN gateway.   --COMING SOON--</dd>
<dt>get-lookuplocators</dt>
<dd>Returns the LOOKUPLOCATORS environment variable which XAP starts with.
</dl>

#####Recipe #2: xap-container

The <i>xap-container</i> recipe starts a single GSC. <br/>
When it starts it locates the management nodes and updates it's lookuplocators by invoking <i>get-lookuplocators</i>.
The recipe has effectively no upper limit on instances and is elastic.  It has no custom commands intended for public use.<br/>
After starting (postStart), it invokes <i>deploy-grid-basic myDataGrid</i>

#####Recipe #3: butterfly
The <i>butterfly</i> recipe starts a web terminal based on websocket and tornado (butterfly).
When it starts it locates the management nodes and updates it's lookuplocators by invoking <i>get-lookuplocators</i>.
<br/>Url: <a href="http://localcloud:8080">http://localcloud:8080</a>
<br/>Works only on Linux.

Butterfly runs a Groovy script that prompts the user to choose:<br/>
(1) Start a demo that connects to the myDataGrid space and performs write, read, read with JDBC, read with SQLQuery on Pojos and Documents Objects. When it ends, it opens a Groovy Shell as in option (2) below.<br/>
(2) Open Groovy Interactive Shell. All XAP's classes are imported to the Groovy Shell.
