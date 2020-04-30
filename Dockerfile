####
# This Dockerfile is used in order to build a container that runs the Quarkus application in JVM mode
#
# Before building the docker image run:
#
# mvn package
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile.jvm -t quarkus/getting-started-jvm .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/getting-started-jvm
#
###
FROM maven:3.6.1-jdk-8-alpine
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV AB_ENABLED=jmx_exporter
COPY target/lib/* /lib/
COPY src/main/java/log4j.xml /log4j.xml
COPY target/*-runner.jar /app.jar

HEALTHCHECK CMD curl --fail http://127.0.0.1/overview/get|| exit 1

ENTRYPOINT ["sh", "-c", "java -Dlog4j.error -Dlog4j.configuration=file:/log4j.xml -jar /app.jar" ]

