FROM maven:3-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY src ./src
COPY pom.xml .

RUN mvn clean package

FROM eclipse-temurin:21-alpine
COPY --from=build /build/target/single-app-1.0-SNAPSHOT.jar /single-app.jar
ENTRYPOINT ["java", "-jar", "single-app.jar"]