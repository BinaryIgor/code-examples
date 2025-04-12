#!/bin/bash
set -eu

proton_file_forwarded_port=$(cat "/run/user/1000/Proton/VPN/forwarded_port")
forwarded_port=${proton_file_forwarded_port:-8333}
external_ip=$(curl -s ifconfig.me)

echo "Publicly available address is: $external_ip:$forwarded_port!"

./bitcoind -externalip=$external_ip -port=$forwarded_port -daemon