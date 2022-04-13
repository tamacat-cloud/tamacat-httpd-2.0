

# Docker image
```
FROM tamacat/tamacat-httpd:2.0-latest
```

## docker start
```
docker run --rm -it -d -p 80:80 tamacat/tamacat-httpd:2.0-latest 
```
* There is no contents in the initial state. (404 Not Found)

### Configure service.json
  * emvironment:
    - SERVICE_JSON=service.json
      * /path/to/service.json (or in CLASSPATH: /usr/local/tamacat-httpd/conf/)
      * default: /usr/local/tamacat-httpd/conf/service.json

### Container bind port
  * emvironment:
    - BIND_PORT=80
   
### webapps
  * Add Servlet/JSP Web Application in /usr/local/tamacat-httpd/webapps/
  * Common Libraries in /usr/local/tamacat-httpd/lib/

### Dockerfile
```
FROM tamacat/tamacat-httpd:2.0-latest

COPY ./conf /usr/local/tamacat-httpd/conf
COPY ./webapps /usr/local/tamacat-httpd/webapps
```

# docker-compose
```
docker-compose up -d --build
```

## Examples Web Application
* http://localhost/examples/
* http://localhost/examples/request.jsp
