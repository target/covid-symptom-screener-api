FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.5_10_openj9-0.17.0-debian-slim
MAINTAINER Jorge Trujillo <jorge.a.trujillo@target.com>

ENV HOME /root
ENV TIMEZONE=America/Chicago

# Prereqs
RUN apt-get update
RUN apt-get install -y --no-install-recommends curl vim lsof jq dnsutils net-tools python wget \
  && rm -rf /var/lib/apt/lists/*

# App properties and certificates
ENV CERTS_PATH /apps/install/certs
ENV APP_PROPERTIES_PATH /apps/install/app_properties

# Set up entrypoint scripts
RUN mkdir -p /apps/install/core $CERTS_PATH
COPY scripts/*.sh /apps/install/core/
RUN chmod a+x /apps/install/core/*.sh

# App properties
ENV APP_PATH "/apps/install/kelvin-api"
ENV BUILD_FILE kelvin-api.jar
ENV JAR_FILE $APP_PATH/$BUILD_FILE

# Copy app
RUN mkdir -p $APP_PATH
COPY $BUILD_FILE $APP_PATH/

EXPOSE 8080
ENTRYPOINT ["/apps/install/core/docker_startpoint.sh"]

CMD java $GC_FLAGS $JAVA_OPTS -jar $JAR_FILE $SPRING_OPTS $EXTERNAL_CONFIG
