FROM maven:3.6-jdk-8 AS build
ENV MAVEN_OPTS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
RUN mkdir /opt/ols
COPY . /opt/ols/ 
COPY build-fix/. /root/build-fix
RUN --mount=type=cache,target=/root/.m2 mkdir -p /root/.m2/repository && cp -r /root/build-fix/* /root/.m2/repository/ && cd /opt/ols && ls && mvn -B clean package -DskipTests

FROM openjdk:8-jre-alpine
RUN apk add bash
COPY --from=build /opt/ols/ols-apps/ols-indexer/target/ols-indexer.jar /opt/ols-indexer.jar
EXPOSE 8080

ENV OLS_HOME /mnt/

ENTRYPOINT ["java", "-jar", "/opt/ols-indexer.jar"]


