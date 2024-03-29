# The tamacat-httpd-2.0 - Java HTTP Server / Reverse Proxy
The tamacat-httpd-2.0 is a open source Java Web Server software, powered by "Apache HttpComponents-5.1".

This is a customizable HTTP/HTTPS Server framework.

<a href="https://tamacat.cloud/">https://tamacat.cloud/</a>

### Features:
- Standards based, pure Java HTTP Server, implementation of HTTP versions 1.0 and 1.1
- Pluggable architecture for custom request/response handler and filters
- It provides a default handler: Reverse Proxy, Thymeleaf Page and Static Contents Web Server
- Jetty Integration (Servlet/JSP)
- Required Apache HttpComponents 5.1
- Required Java Platform, Standard Edition 17 (JRE/JDK)

### Source code
- https://github.com/tamacat-cloud/tamacat-httpd-2.0


* Example: ClassicHttpd

```java
public static void main(String[] args) {
    ClassicHttpd.startup(
        ServerConfig.create().port(80)
            .service(ServiceConfig.create().path("/"))
            .service(ServiceConfig.create().path("/examples/")
                .reverse(ReverseConfig.create().url("http://localhost:8080/examples/")
            )
        )
    );
}
```

### DockerHub
- https://hub.docker.com/r/tamacat/tamacat-httpd

### License:
The tamacat-httpd is licensed under the terms of the Apache License, Version 2.0.
