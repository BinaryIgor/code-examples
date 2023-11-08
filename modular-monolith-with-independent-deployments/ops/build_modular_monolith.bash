#!/bin/bash
cd ..

docker rm modular-monolith-with-independent-deployments-build

docker build -f DockerfileBuild . -t modular-monolith-with-independent-deployments-build

docker volume create --name maven-repo

# -U (Update snapshots) always recheck for new modules snapshot versions!
docker run -it --network=host -v "maven-repo:/root/.m2" -v "$PWD/application:/build/application" \
  --name modular-monolith-with-independent-deployments-build modular-monolith-with-independent-deployments-build \
  mvn clean package -U

