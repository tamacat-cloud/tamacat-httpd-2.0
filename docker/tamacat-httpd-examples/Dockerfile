FROM tamacat/tamacat-httpd:2.0-latest

RUN apk update && apk upgrade && apk add --no-cache

COPY ./conf /usr/local/tamacat-httpd/conf
COPY ./webapps /usr/local/tamacat-httpd/webapps
#COPY ./lib /usr/local/tamacat-httpd/lib
#COPY ./htdocs /usr/local/tamacat-httpd/htdocs

