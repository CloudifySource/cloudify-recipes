#!/bin/bash

DB_HOST=$1
DB_USER=$2
DB_PASS=$3
DB_NAME=$4

TOMCAT_VERSION=apache-tomcat-7.0.23
TOMCAT_ZIP=$TOMCAT_VERSION.zip
TOMCAT_INSTALL_DIR=~/.cloudify/tomcat
TOMCAT_ZIP_URL=http://repository.cloudifysource.org/org/apache/tomcat/7.0.23/$TOMCAT_ZIP
TOMCAT_HOME=$TOMCAT_INSTALL_DIR/$TOMCAT_VERSION
TOMCAT_CONF=$TOMCAT_HOME/conf
TOMCAT_LOCAL=$TOMCAT_CONF/Catalina/localhost
TOMCAT_ROOT_XML=$TOMCAT_LOCAL/ROOT.xml
SERVER_XML=$TOMCAT_CONF/server.xml
CONTEXT_XML=$TOMCAT_CONF/context.xml
applicationWar="mysqladmin.war"
WAR_LOCAL_PATH=$TOMCAT_HOME/$applicationWar



wget $TOMCAT_ZIP_URL

mkdir -p $TOMCAT_INSTALL_DIR
unzip $TOMCAT_ZIP -d $TOMCAT_INSTALL_DIR
cp $applicationWar $TOMCAT_HOME/
cd $TOMCAT_HOME/

mkdir -p $TOMCAT_LOCAL
echo "<Context docBase=\"${WAR_LOCAL_PATH}\" />" > $TOMCAT_ROOT_XML
sed -i -e "s+$ORIG_SERVER_RESOURCE+$NEW_SERVER_RESOURCE+g" $SERVER_XML

find / -name "mysql-connector*.jar" | grep usmlib | xargs -I file cp file $TOMCAT_HOME/lib/

sed -i -e "s+$ORIG_CTX+$NEW_CTX+g" $CONTEXT_XML




 
