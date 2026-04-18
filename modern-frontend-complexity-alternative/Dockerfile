FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY src/main/java ./src/main/java
COPY dist/src/main/resources ./src/main/resources

# reuse deps across builds
RUN --mount=type=cache,target=/root/.m2 mvn clean package

FROM eclipse-temurin:25-jre-alpine
COPY --from=build /build/target/modern-frontend-complexity-alternative.jar modern-frontend-complexity-alternative.jar
ENTRYPOINT ["java", "-jar", "modern-frontend-complexity-alternative.jar"]