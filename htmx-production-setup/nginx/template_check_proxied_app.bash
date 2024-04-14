#!/bin/bash
set -euo pipefail

echo "Checking proxied app connection.."
curl --fail --retry-connrefused --retry 10 --retry-delay 1 ${app_health_check_url}
echo

echo "Proxied connection checked, see if it is what it should be!"