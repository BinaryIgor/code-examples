#!/bin/bash
set -eu
curl -H 'content-type: application/json' \
  -d "{ \"model\": \"OPEN_AI_TEXT_3_SMALL\", \"embeddingId\": \"${EMBEDDING_ID}\"}" \
  -X POST "http://localhost:8080/vector-embeddings/similar-to-embedding-search"