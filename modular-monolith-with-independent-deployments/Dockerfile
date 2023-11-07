FROM maven:3-eclipse-temurin-21-alpine AS build

COPY /commons/spring-parent /build/spring-parent
COPY /application /build/application

COPY build_settings.xml /usr/share/maven/ref/settings.xml

WORKDIR /build/spring-parent
RUN mvn clean install

WORKDIR /build/application
RUN ls -l
RUN mvn clean install

# FROM eclipse-temurin:21 AS run

# WORKDIR /app

# COPY --from=build /build/target target/

#E NTRYPOINT java -jar app.jar