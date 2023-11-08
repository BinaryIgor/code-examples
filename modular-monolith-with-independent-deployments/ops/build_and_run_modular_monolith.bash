#!/bin/bash

bash build_modular_monolith.bash

cd ../application/target

sudo chown 755 modular-app.jar

exec java -jar modular-app.jar