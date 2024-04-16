#!/bin/bash
set -euo pipefail

export ENV=local

bash build_and_package.bash

cd dist
bash load_and_run_app.bash