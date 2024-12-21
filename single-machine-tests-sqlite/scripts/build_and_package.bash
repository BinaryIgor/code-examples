#!/bin/bash
set -euo pipefail

app=$APP
cd ..
cd $app
bash build_and_package.bash