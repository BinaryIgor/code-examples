#!/bin/bash
set -eu

report_name="peers_$(date +"%Y%m%d%H%M").txt"
reports_dir=${REPORTS_DIR:-"/home/bigor/btc-peers"}
report_path="${reports_dir}/${report_name}"
btc_explorer_path=${BTC_EXPLORER_PATH:-"/home/bigor/scripts/btc_explorer.py"}

echo "Creating peers report and saving it under ${report_path} path"
mkdir -p $reports_dir

export BITCOIN_CLI_CMD="/home/bigor/bitcoin-27.0/bin/bitcoin-cli"

python3 ${btc_explorer_path} "get_peers_info" > ${report_path}

echo "Report created."