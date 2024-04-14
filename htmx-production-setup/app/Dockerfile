# FROM maven:3-eclipse-temurin-21-alpine AS build
#
# WORKDIR /build
#
# COPY src ./src
# COPY pom.xml .
#
# RUN mvn clean -Pexecutable package

FROM eclipse-temurin:21-alpine
# COPY --from=build /build/target/htmx-production-setup-1.0-SNAPSHOT.jar /htmx-production-setup.jar
COPY dist/target/htmx-production-setup-1.0-SNAPSHOT.jar /htmx-production-setup.jar
ENTRYPOINT exec java -jar htmx-production-setup.jar