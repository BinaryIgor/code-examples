FROM eclipse-temurin:21-alpine
COPY target/sqlite-limits-tests-jar-with-dependencies.jar /sqlite-limits-tests.jar
ENTRYPOINT ["java", "-jar", "/sqlite-limits-tests.jar"]