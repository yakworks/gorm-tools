FROM yakworks/alpine-jre:8

USER root

# keeps the volumes from run to run
# VOLUME /tmp
# VOLUME /app/conf
# VOLUME /app/resources
# VOLUME /app/logs

WORKDIR /app

EXPOSE 8080

# COPY build/libs/*.jar app.jar
# ENTRYPOINT ["java","-Dloader.path=/extData/conf/", "-jar","/app.jar"]

# ARG LIB_DIR=build/libs
ARG LIB_DIR=.
ARG APP_JAR_DIR=/app/jar

COPY docker-entrypoint.sh /app/bin/
COPY utils.sh /app/bin/
COPY logback.groovy /app/

# single jar do this
# COPY ${LIB_DIR}/app.jar ${APP_JAR_DIR}/

# COPY ${LIB_DIR}/BOOT-INF/lib ${APP_JAR_DIR}/lib
# COPY ${LIB_DIR}/META-INF ${APP_JAR_DIR}/META-INF
# COPY ${LIB_DIR}/BOOT-INF/classes ${APP_JAR_DIR}

COPY ${LIB_DIR}/BOOT-INF/lib ${APP_JAR_DIR}/lib
COPY ${LIB_DIR}/META-INF ${APP_JAR_DIR}/META-INF
COPY ${LIB_DIR}/BOOT-INF/classes ${APP_JAR_DIR}

# exploded
# COPY ${LIB_DIR}/BOOT-INF/lib ${APP_JAR_DIR}/BOOT-INF/lib
# COPY ${LIB_DIR}/org ${APP_JAR_DIR}/org
# /META-INF has version in it so it wont get changes unless version bumps
# COPY ${LIB_DIR}/META-INF ${APP_JAR_DIR}/META-INF
# the default resources for demo
# COPY ${LIB_DIR}/resources /app/resources
# the app classes where the bulk of the changes will be, so do this last so the above can be layer cached
# COPY ${LIB_DIR}/BOOT-INF/classes ${APP_JAR_DIR}/BOOT-INF/classes

# see https://github.com/Yelp/dumb-init for dumb-init
ENTRYPOINT ["/usr/bin/dumb-init", "--"]

CMD ["bash", "-c", "/app/bin/docker-entrypoint.sh"]

