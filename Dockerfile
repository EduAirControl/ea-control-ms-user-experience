FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN sed -i 's/\r$//' mvnw && bash mvnw dependency:go-offline

COPY src src

RUN sed -i 's/\r$//' mvnw && bash mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN (getent group 1000 >/dev/null 2>&1 || groupadd -g 1000 appgroup) \
 && (id -u 1000 >/dev/null 2>&1 || useradd -u 1000 -g 1000 -r -s /usr/sbin/nologin appuser) \
 && chown -R 1000:1000 /app

COPY --from=builder /app/target/*.jar app.jar
RUN chown 1000:1000 app.jar

USER 1000

EXPOSE 3006

HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:3006/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

