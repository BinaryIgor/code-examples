#!/bin/bash
curl -X POST -H "content-type: application/json" \
  -d '{ "model": "OPEN_AI_TEXT_3_SMALL", "dataSource": "BOOKS" }' \
  "http://localhost:8080/vector-embeddings/reindex-ivfflat"