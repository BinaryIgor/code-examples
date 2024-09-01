#!/bin/bash

curl -X POST "http://localhost:8080/vector-embeddings/generate?model=OPEN_AI_TEXT_3_SMALL&dataType=AmazonReviews&skip=30000"