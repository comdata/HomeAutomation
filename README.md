
# HomeAutomation

[![Build Status](https://travis-ci.org/comdata/HomeAutomation.svg?branch=master)](https://travis-ci.org/comdata/HomeAutomation)
[![Stories in Ready](https://badge.waffle.io/comdata/HomeAutomation.png?label=ready&title=Ready)](https://waffle.io/comdata/HomeAutomation)
[![buddy pipeline](https://app.buddy.works/cmertins/homeautomation/pipelines/pipeline/41420/badge.svg?token=749a9b557f3f7c1d1501841a45579947928dd2762c51f6343db062c8503fcdde "buddy pipeline")](https://app.buddy.works/cmertins/homeautomation/pipelines/pipeline/41420)

This is my first approach to automate my home.

Currently this is using the 433Utils for receiving/sending data and 
the Adafruit DHT library to record temperature.

The system currently supports reading:

- temperature/humidity (DHT, HTU21D and DSB)
- pressure (BMP180)
- number of planes from dump1090
- state of TV (Panasonic)
- communication and control of Z-Wave Window Blinds using FHEM
- Receive measurements via ZeroMQ and MQTT (using a local Moquette Broker)
 
Additionally it can proxy for a MJPEG camera.

The system is split in a webapp (currently tested on Tomcat8) and a client (for collecting the sensor data).

The UI is based on OpenUI5 http://openui5.org . The frontend gets 
update via Websockets from the backend.

Activities can be scheduled by an integrated scheduler.

Any comments are welcome.

This is just a hobby... ;-)
