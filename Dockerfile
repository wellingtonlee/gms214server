# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src/ src/
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /server

COPY --from=build /app/bin/maplestory-2.13.1-jar-with-dependencies.jar server.jar
COPY scripts/ scripts/
COPY resources/ resources/
COPY properties/ properties/

# WZ data and dat cache should be mounted as volumes
VOLUME ["/server/wz", "/server/dat"]

EXPOSE 8483 8484 8585

ENTRYPOINT ["java", "--enable-preview", "-Xms512m", "-Xmx2g", "-XX:+UseG1GC", "-jar", "server.jar"]
