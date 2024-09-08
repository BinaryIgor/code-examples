#!/bin/bash
skip="${VEMBEDDINGS_SKIP:-0}"
curl -X POST "http://localhost:8080/vector-embeddings/generate?model=OPEN_AI_TEXT_3_SMALL&dataType=Books&skip=${skip}&batchSize=500&rateLimitDelay=1000"