FROM maven:3-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src
COPY dist/static ./src/main/resources/static

RUN mvn clean -Pexecutable -DskipTests package

FROM eclipse-temurin:21-alpine
COPY --from=build /build/target/htmx-production-setup-1.0-SNAPSHOT.jar /htmx-production-setup.jar
ENTRYPOINT exec java -jar htmx-production-setup.jar