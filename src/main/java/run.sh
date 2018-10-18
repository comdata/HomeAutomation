#!/bin/bash

java -classpath "../lib/*:./" cm.homeautomation.services.base.StandAloneSensor 2 http://192.168.1.57:8080/HomeAutomation/services/sensors/forroom/save
