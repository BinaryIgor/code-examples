#!/bin/bash
skip="${VEMBEDDINGS_SKIP:-0}"
curl -X POST "http://localhost:8080/vector-embeddings/generate?model=GOOGLE_TEXT_004&dataType=Books&skip=${skip}&batchSize=50&rateLimitDelay=500"