FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY mvnw .
COPY src ./src
RUN mvn clean package -DskipTests -B
FROM eclipse-temurin:17-jre-jammy
LABEL maintainer="Brock Loader"
LABEL description="OpenGalaxy Game Server"
LABEL org.opencontainers.image.title="OpenGalaxy Server"
LABEL org.opencontainers.image.description="OpenGalaxy private game server"
LABEL org.opencontainers.image.vendor="OpenGalaxy"
RUN groupadd -r opengalaxy && useradd -r -g opengalaxy opengalaxy
WORKDIR /app
COPY --from=build /build/target/supergo2-server*.jar app.jar
RUN mkdir -p /app/logs && chown -R opengalaxy:opengalaxy /app
EXPOSE 90 5150 843 9090
USER opengalaxy
ENV JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"
ENV SPRING_CONFIG_LOCATION=/app/config/application.yml
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.config.location=$SPRING_CONFIG_LOCATION"]
