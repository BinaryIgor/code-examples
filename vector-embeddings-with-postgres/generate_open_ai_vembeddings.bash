#!/bin/bash
set -eu
skip="${VEMBEDDINGS_SKIP:-0}"
batch_size=${VEMBEDDINGS_BATCH_SIZE:-500}
rate_limit_delay=${VEMBEDDINGS_RATE_LIMIT_DELAY:-1000}
curl -X POST "http://localhost:8080/vector-embeddings/generate?model=OPEN_AI_TEXT_3_SMALL&dataType=BOOKS&skip=${skip}&batchSize=${batch_size}&rateLimitDelay=${rate_limit_delay}"