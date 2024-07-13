#!/bin/bash
set -euo pipefail

cd ..
. config.env
cd scripts

app=$APP
cd ..
cd $app
bash build_and_package.bash