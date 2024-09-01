#!/bin/bash
set -eu
curl -H 'content-type: application/json' \
  -d '{ "model": "OPEN_AI_TEXT_3_SMALL", "input": "Good product" }' \
  -X POST "http://localhost:8080/vector-embeddings/search"