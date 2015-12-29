# HomeAutomation

This is my first approach to automate my home.

Currently this is using the 433Utils for receiving/sending data and 
the Adafruit DHT library to record temperature.

The system currently supports reading:

- temperature (DHT and DSB)
- pressure (BMP180)
- number of planes from dump1090
- state of TV (Panasonic)
 
Additionally it can proxy for a MJPEG camera.

The system is split in a webapp (currently tested on Tomcat8) and a client (for collecting the sensor data).

The UI is based on OpenUI5 http://openui5.org . The frontend gets 
update via Websockets from the backend.

Any comments are welcome.

This is just a hobby... ;-)
