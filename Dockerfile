# syntax=docker/dockerfile:1.4
FROM maven:3.6-jdk-8 AS build
ENV MAVEN_OPTS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
USER root
WORKDIR /opt/ols
COPY --link . .
COPY --link build-fix /root/build-fix
RUN --mount=type=cache,target=/root/.m2 if [ ! -d "/root/.m2/repository/org/neo4j/neo4j-cypher-dsl/2.0.1/" ] ; \
    then mkdir -p /root/.m2/repository \
    && cp -r /root/build-fix/org /root/.m2/repository/ \
    ; fi
# For unknown reasons, all jar files under ols-apps/*/target were missing after the run step.
# Many hours were spend trying to identify the problem but in the end the workaround was used to copy it into the /package directory in the builder image.
RUN --mount=type=cache,target=/root/.m2 mvn package -T 1C -DskipTests -Dmaven.artifact.threads=100 \
 && mkdir -p /package \
 && cp /opt/ols/ols-apps/*/target/*.jar /package \
 && cp /opt/ols/*/target/*.war /package

FROM busybox
WORKDIR /package
COPY --link --from=build /package /package
RUN ls -lah /package
## data container, don't run
ENTRYPOINT ["/bin/true"]
