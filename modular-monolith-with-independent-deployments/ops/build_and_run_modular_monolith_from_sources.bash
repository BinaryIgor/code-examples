#!/bin/bash

cd ../

cd commons/spring-parent
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

cd application
mvn clean install

export BUDGET_DB_URL="jdbc:postgresql://localhost:5555/budget";
export BUDGET_DB_USERNAME="budget_module";
export BUDGET_DB_PASSWORD="budget_module";

export CAMPAIGN_DB_URL="jdbc:postgresql://localhost:5555/campaign";
export CAMPAIGN_DB_USERNAME="campaign_module";
export CAMPAIGN_DB_PASSWORD="campaign_module";

export INVENTORY_DB_URL="jdbc:postgresql://localhost:5555/inventory";
export INVENTORY_DB_USERNAME="inventory_module";
export INVENTORY_DB_PASSWORD="inventory_module";

exec java -jar target/modular-app.jar