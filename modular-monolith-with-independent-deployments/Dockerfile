FROM maven:3-eclipse-temurin-21-alpine AS build

COPY /commons/spring-parent/pom.xml /build/spring-parent/pom.xml
WORKDIR /build/spring-parent
RUN mvn clean install

COPY build_settings.xml /usr/share/maven/ref/settings.xml
COPY /application/ /build/application/

WORKDIR /build/application

RUN mvn clean install

# FROM eclipse-temurin:21 AS run

# WORKDIR /app

# COPY --from=build /build/target target/

#E NTRYPOINT java -jar app.jar