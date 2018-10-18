#!/bin/bash

#mvn install
rsync -auv --delete --progress target/HomeAutomation-0.0.1-SNAPSHOT/* root@192.168.1.57:/var/lib/tomcat8/webapps/HomeAutomation
