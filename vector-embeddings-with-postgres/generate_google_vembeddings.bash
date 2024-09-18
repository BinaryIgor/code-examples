#!/bin/bash
set -eu
skip="${VEMBEDDINGS_SKIP:-0}"
batch_size=${VEMBEDDINGS_BATCH_SIZE:-50}
rate_limit_delay=${VEMBEDDINGS_RATE_LIMIT_DELAY:-500}
curl -X POST "http://localhost:8080/vector-embeddings/generate?model=GOOGLE_TEXT_004&dataType=BOOKS&skip=${skip}&batchSize=${batch_size}&rateLimitDelay=${rate_limit_delay}"