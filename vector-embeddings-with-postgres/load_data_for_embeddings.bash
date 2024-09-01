#!/bin/bash
set -eu

curl -X POST -h "content-type: application/json" \
  -d "{ \"type\": \"AmazonReviews\", \"path\": \"${DATA_AMAZON_REVIEWS_FILE_PATH\"" }" \
  "http://localhost:8080/vector-embeddings/load-data"