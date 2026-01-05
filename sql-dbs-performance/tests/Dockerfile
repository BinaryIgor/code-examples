FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

# reuse deps across builds
RUN --mount=type=cache,target=/root/.m2 mvn clean package

FROM eclipse-temurin:25
COPY --from=build /build/target/sql-db-performance-tests-jar-with-dependencies.jar sql-db-performance-tests.jar
ENTRYPOINT ["java", "-jar", "sql-db-performance-tests.jar"]