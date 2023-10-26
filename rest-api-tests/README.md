# Rest API tests

Simple rest api written in Java 21 and Spring Boot 3 to present how easy it is to write integration/e2e tests of REST
API
using modern tools (like [Testcontainers](https://testcontainers.com/)).

## API

Our API is using Postgresql as a database (see schema.sql), and has the following endpoints:

```
POST: /clients
body: {
  "name": String,
  "email": String,
  "status": ClientStatus (one of: TO_CONTACT, FIRST_CONTACT, SECOND_CONTACT, OFFER_PENDING, OFFER_GIVEN, OFFER_REJECTED, OFFER_ACCEPTED)
}
response: 201 and body {
  "id": UUID
}

PUT: /clients/{id}
body: {
  "name": String,
  "email": String,
  "status": ClientStatus
}
response: 200

GET: /clients/{id}
response: 200 and body {
  "id": UUID,
  "name": String,
  "email": String,
  "status": ClientStatus
}

In case of errors, ErrorResponse is returned:
{
  "error": String (ClientValidationException, ClientNotFoundException for example),
  "message": String
}

There are some validation rules around name and email, for details check out Client class.
```
## Tests

To run all tests execute:
```
mvn clean test
```
To run test and generate coverage report (available in target/site/jacoco/index.html) execute:
```
mvn clean package -PtestsReport
```
