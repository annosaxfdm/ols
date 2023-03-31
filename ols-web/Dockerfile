# syntax=docker/dockerfile:1
FROM openjdk:8-jre-alpine
RUN apk add bash
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/package/ols-boot.war"]
