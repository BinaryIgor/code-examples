#!/bin/bash
set -e

cd ..

docker rm modular-monolith-with-independent-deployments-build || true

docker build -f DockerfileBuild . -t modular-monolith-with-independent-deployments-build

docker volume create --name maven-repo
docker volume create --name modular-monolith-with-independent-deployments-build-volume

# Socket is for running docker in docker (testcontainers tests during build in docker).
# If you have some issues with, build package without tests: mvn clean package -DskipTests
docker run -it --network=host -v "maven-repo:/root/.m2" \
  -v "modular-monolith-with-independent-deployments-build-volume:/build" \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name modular-monolith-with-independent-deployments-build modular-monolith-with-independent-deployments-build \
  /bin/bash -c "mvn clean install -f parent-pom.xml; mvn clean package"

