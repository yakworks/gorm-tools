FROM alpine:3.11

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

USER root

RUN wget --quiet https://cdn.azul.com/public_keys/alpine-signing@azul.com-5d5dc44c.rsa.pub -P /etc/apk/keys/ && \
    echo "https://repos.azul.com/zulu/alpine" >> /etc/apk/repositories && \
    apk --no-cache add zulu8-jdk

ENV JAVA_HOME=/usr/lib/jvm/zulu8-ca

# Some stuff from CIRCLECI examples and other
# 'shadow' allows the group and user adds below to work
RUN apk add --no-cache git sudo openssh-client parallel \
    curl bash make \
    mysql-client && \
    rm -rf /var/cache/apk/*

# adds in some things needed for java ui and reporting, like fonts
RUN apk add --no-cache dumb-init xvfb fontconfig ttf-dejavu && \
    rm -rf /var/cache/apk/*

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

COPY ${LIB_DIR}/app.jar ${APP_JAR_DIR}/

COPY docker-entrypoint.sh /app/bin/
COPY utils.sh /app/bin/

ENTRYPOINT ["/app/bin/docker-entrypoint.sh"]
