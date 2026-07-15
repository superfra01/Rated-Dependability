# Build the application in an isolated stage. The resulting image is therefore
# self-contained and does not rely on a WAR file from the host machine.
FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp package -DskipTests


FROM tomcat:9.0.120-jdk17-temurin-jammy

RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf "${CATALINA_HOME}/webapps/"* \
    && useradd --system --uid 1001 --home-dir "${CATALINA_HOME}" --shell /usr/sbin/nologin tomcat \
    && chown -R tomcat:tomcat "${CATALINA_HOME}"

COPY --from=build --chown=tomcat:tomcat /workspace/target/rated-app.war ${CATALINA_HOME}/webapps/ROOT.war

USER tomcat

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl --fail --silent http://localhost:8080/ > /dev/null || exit 1

CMD ["catalina.sh", "run"]
