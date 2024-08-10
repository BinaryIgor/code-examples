#!/bin/bash
set -euo pipefail

curl -X POST "http://localhost:8080/internal/send-random-analytics-events?size=100000&concurrency=500"
