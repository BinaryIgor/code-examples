#!/bin/bash
set -eu

curl -X POST -H "content-type: application/json"  \
  -d "{ \"type\": \"BOOKS\", \"path\": \"${DATA_BOOKS_FILE_PATH}\" }" \
  "http://localhost:8080/vector-embeddings/load-data"