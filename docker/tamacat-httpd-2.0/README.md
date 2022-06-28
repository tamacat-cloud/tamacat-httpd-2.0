
# tamacat/tamacat-httpd-2.0

- The tamacat-httpd-2.0 is a open source Java Web Server software, powered by "Apache HttpComponents-5.1".
- This is a customizable HTTP/HTTPS Server framework.
- Base image: eclipse-temurin:17-jre-alpine (OpenJDK 17)

## GitHub
https://github.com/tamacat-cloud/tamacat-httpd-2.0


## DIRECTORY
- /usr/local/tamacat-httpd/conf
- /usr/local/tamacat-httpd/lib
- /usr/local/tamacat-httpd/htdocs/root
- /usr/local/tamacat-httpd/webapps


## ENVIRONMENT
- SERVICE_JSON=service.json
- BIND_PORT=80


- /usr/local/tamacat-httpd/conf/service.json
```
{
  "protocol": "http",
  "port": 80,
  "host": "localhost",
  "maxTotal": 1000,
  "maxPerRoute": 200,
  "soTimeout": 60,
  "keepAlive": true,
  "services": [
    {
      "path": "/",
      "type": "thymeleaf",
      "id": "default",
      "docsRoot": "htdocs/root"
    },
    {
      "path": "/examples/",
      "type": "jetty",
      "id": "examples",
      "reverse": {
        "url": "http://127.0.0.1:8080/examples/"
      }
    }
  ]
}
```
