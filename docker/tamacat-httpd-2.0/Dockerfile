FROM eclipse-temurin:17-jre-alpine

#LABEL maintainer="tamacat.org"

RUN apk update && apk upgrade && apk add --no-cache \
    bash \
    curl

RUN mkdir -p /usr/local/tamacat-httpd/lib
WORKDIR /usr/local/tamacat-httpd

COPY ./lib /usr/local/tamacat-httpd/lib
COPY ./conf /usr/local/tamacat-httpd/conf
COPY ./htdocs /usr/local/tamacat-httpd/htdocs
COPY ./webapps /usr/local/tamacat-httpd/webapps

ENV CLASSPATH=/usr/local/tamacat-httpd/conf:/usr/local/tamacat-httpd/lib/tamacat-httpd-2.0-SNAPSHOT-jar-with-dependencies.jar

ENV SERVICE_JSON="service.json"
ENV BIND_PORT=80

EXPOSE ${BIND_PORT}

CMD java cloud.tamacat.httpd.Httpd ${SERVICE_JSON}
