FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

# reuse deps across builds
RUN --mount=type=cache,target=/root/.m2 mvn clean package

FROM eclipse-temurin:25-jre-alpine
COPY --from=build /build/target/json-dbs-performance-tests-jar-with-dependencies.jar json-dbs-performance-tests.jar
ENTRYPOINT ["java", "-jar", "json-dbs-performance-tests.jar"]