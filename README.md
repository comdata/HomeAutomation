# HomeAutomation

[[https://travis-ci.org/comdata/HomeAutomation.svg?branch=Main]]

This is my approach to automate my home.

The system currently supports reading:

    temperature/humidity (DHT, HTU21D and DSB)
    pressure (BMP180)
    number of planes from dump1090
    state of TV (Panasonic)
    communication and control of Z-Wave Window Blinds using FHEM
    Receive measurements MQTT (using a local broker)
    Zigbee via zigbee2mqtt

Additionally it can proxy for a MJPEG camera.

The system is split in a webapp (currently tested on Tomcat8) and a client (for collecting the sensor data).

The UI is based on OpenUI5 http://openui5.org . The frontend gets update via Websockets from the backend.

Activities can be scheduled by an integrated scheduler.

Any comments are welcome.

This is just a hobby... ;-)

# This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
Running the application in dev mode

You can run your application in dev mode that enables live coding using:

./mvnw quarkus:dev

# Packaging and running the application

The application is packageable using ./mvnw package. It produces the executable code-with-quarkus-1.0.0-SNAPSHOT-runner.jar file in /target directory. Be aware that it’s not an über-jar as the dependencies are copied into the target/lib directory.

The application is now runnable using java -jar target/HomeAutomation-2.0.0-SNAPSHOT-runner.jar 