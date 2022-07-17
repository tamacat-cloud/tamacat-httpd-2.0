FROM eclipse-temurin:17-jre-alpine

#LABEL maintainer="tamacat.org"

USER root

RUN apk update && apk upgrade && apk add --no-cache \
    bash \
    curl

WORKDIR /usr/local/tamacat-httpd

COPY ./lib /usr/local/tamacat-httpd/lib
COPY ./conf /usr/local/tamacat-httpd/conf
COPY ./htdocs /usr/local/tamacat-httpd/htdocs

ENV CLASSPATH=/usr/local/tamacat-httpd/conf:/usr/local/tamacat-httpd/lib/tamacat-httpd-2.0-SNAPSHOT-jar-with-dependencies.jar
ENV SERVER_HOME=/usr/local/tamacat-httpd
ENV SERVICE_JSON="conf/service.json"
ENV BIND_PORT=80

EXPOSE ${BIND_PORT}

USER 1001

CMD java cloud.tamacat.httpd.Httpd ${SERVICE_JSON}
