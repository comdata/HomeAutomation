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
FROM ghcr.io/comdata/docker-image:latest
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV AB_ENABLED=jmx_exporter
COPY target/*-runner.jar /app.jar
COPY target/*-runner /runner

COPY entrypoint.java.sh entrypoint.java.sh
COPY entrypoint.native.sh entrypoint.native.sh

RUN     arch="$(uname -m)"; \
        case "$arch" in \
                x86_64) cp entrypoint.native.sh  /entrypoint.sh;; \
                *) cp entrypoint.java.sh  /entrypoint.sh;;  \
        esac;

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["sh", "-c", "/entrypoint.sh" ]

HEALTHCHECK CMD curl --fail http://127.0.0.1/overview/get|| exit 1