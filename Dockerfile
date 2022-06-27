# syntax=docker/dockerfile:1.4
FROM maven:3.6-jdk-8 AS build
ENV MAVEN_OPTS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
USER root
WORKDIR /opt/ols
COPY --link . .
COPY --link build-fix /root/build-fix
RUN --mount=type=cache,target=/root/.m2 mkdir -p /root/.m2/repository && cp -r /root/build-fix/* /root/.m2/repository/
RUN --mount=type=cache,target=/root/.m2 mvn -B -T 1C package -DskipTests
RUN mkdir -p /package && ls -lah /package

FROM busybox
WORKDIR /package
COPY --link --from=build /opt/ols/ols-apps/*/target/*.jar /package
COPY --link --from=build /opt/ols/*/target/*.war /package
# data container, don't run
ENTRYPOINT ["/bin/true"]
