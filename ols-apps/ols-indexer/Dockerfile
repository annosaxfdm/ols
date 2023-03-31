# syntax=docker/dockerfile:1
FROM openjdk:8-jre-alpine
RUN apk add bash
ENV OLS_HOME /mnt/
ENTRYPOINT ["java", "-jar", "/package/ols-indexer.jar"]
