#!/bin/bash
set -e

docker build -t btc-core-builder .

docker rm btc-core-builder || true

btc_core_src_path="${BTC_CORE_SRC_PATH:-/home/igor/ws/code/CProjects/bitcoin}"

# -u option is to cascade current user and their group permission
docker run -it \
  -v "$btc_core_src_path:/home/builder/bitcoin" \
  -v "$HOME/.ccache:/ccache" \
  -w /home/builder/bitcoin \
  --name btc-core-builder btc-core-builder