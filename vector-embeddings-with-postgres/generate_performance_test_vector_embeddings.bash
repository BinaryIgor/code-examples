#!/bin/bash
curl -X POST "http://localhost:8080/vector-embeddings/generate?model=PERFORMANCE_TEST&dataType=PERFORMANCE_TEST&batchSize=1000&rateLimitDelay=10"