#!/bin/bash
set -e

cd ../

cd commons/spring-parent
mvn clean install
cd ../contracts
mvn clean install

cd ../../

cd modules

cd budget
mvn clean install

cd ..
cd campaign
mvn clean install

cd ..
cd inventory
mvn clean install

cd ../..

source ops/modular_monolith_db.env

cd application
mvn clean install

exec java -jar target/modular-app.jar