#!/bin/bash

mvn -DskipTests install
rsync -auv --delete --progress target/HomeAutomation-0.0.1-SNAPSHOT/* /usr/local/Cellar/tomcat/8.5.24/libexec/webapps/HomeAutomation

brew services restart tomcat
