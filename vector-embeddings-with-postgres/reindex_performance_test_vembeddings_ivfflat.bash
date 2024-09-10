#!/bin/bash
curl -X POST -H "content-type: application/json" \
  -d '{ "model": "PERFORMANCE_TEST", "dataSource": "PERFORMANCE_TEST" }' \
  "http://localhost:8080/vector-embeddings/reindex-ivfflat"