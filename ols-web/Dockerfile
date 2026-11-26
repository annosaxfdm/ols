# syntax=docker/dockerfile:1
FROM eclipse-temurin:8-jre-alpine
# libgcc is needed by libsunec.so; without it EC TLS handshakes fail when downloading ontologies
RUN apk add --no-cache bash libgcc
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/package/ols-boot.war"]
