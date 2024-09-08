#!/bin/bash
set -eu

#echo "{ \"type\": \"AmazonReviews\", \"path\": \"${DATA_BOOKS_FILE_PATH}\" }"
curl -X POST -H "content-type: application/json"  \
  -d "{ \"type\": \"Books\", \"path\": \"${DATA_BOOKS_FILE_PATH}\" }" \
  "http://localhost:8080/vector-embeddings/load-data"