#!/bin/bash
set -euo pipefail

docker build -t sqlite-browser -<<EOF
FROM ubuntu:24.04
RUN apt-get -y update && apt-get -y upgrade &&  apt-get install -y sqlite3
WORKDIR /db
CMD ["/bin/bash"]
EOF

docker run -it -v /mnt/single_machine_volume/data:/db sqlite-browser