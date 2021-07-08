FROM yakworks/alpine-jre:8

# FIXME this is not good for security, do we really need it?
USER root

WORKDIR /app

EXPOSE 8080

# ARG LIB_DIR=build/libs
ARG LIB_DIR=.
ARG APP_JAR_DIR=/app/jar

COPY ${LIB_DIR}/BOOT-INF/lib ${APP_JAR_DIR}/lib
COPY ${LIB_DIR}/META-INF ${APP_JAR_DIR}/META-INF
# most changes between deploys are here so above will be cached and thats where the bulk of the size is
COPY ${LIB_DIR}/BOOT-INF/classes ${APP_JAR_DIR}

COPY docker-entrypoint.sh /app/bin/
COPY utils.sh /app/bin/
COPY logback.groovy /app/

# single jar do this
# COPY ${LIB_DIR}/app.jar ${APP_JAR_DIR}/

# see https://github.com/Yelp/dumb-init for dumb-init
ENTRYPOINT ["/usr/bin/dumb-init", "--"]

CMD ["bash", "-c", "/app/bin/docker-entrypoint.sh"]

