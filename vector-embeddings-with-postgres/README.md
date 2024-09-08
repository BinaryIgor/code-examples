# Vector Embeddings with Postgres

Let's experiment with Vector Embeddings with Postgres to say that it is indeed the Everything Database!

**What is there:**
* Postgres in Docker with [pgvector](https://github.com/pgvector/pgvector) extension installed
* Java 21 app that can
  * generate OpenAI embeddings - `OpenAIVectorEmbeddingsGenerator`
  * generate Google embeddings - `GoogleVectorEmbeddingsGenerator`
  * uses books from Kaggle dataset to demonstrate Semantic Search, possible by the usage of Vector Embeddings: https://www.kaggle.com/datasets/elvinrustam/books-dataset/data?select=BooksDataset.csv
  * can also generate PerformanceTest embeddings to show the limits of non-indexed embeddings and how we can fix it
* Various Bash scripts to automate operations and run operations more smoothly

**What is needed to run it:**
* Docker
* Java 21 with compatible Maven version
* Bash shell
* OpenAI and GoogleAI API_TOKENS, to generate Vector Embeddings
  * https://platform.openai.com
  * https://ai.google.dev

## Vector what?

Vector Embeddings are a simple yet profound concept.

We basically take any two pieces of information from the world - be it text, image or a video even - and turn them into a vector, a number of arbitrary dimensions:
1. `[ 0.1, 0.2, 0.4 ]`
2. `[ 0.0, 0.1, 0.2 ]`

And then, we can measure how similar they are, simply by calculating a distance between them:
```
sqrt((0.1 - 0.0)^2 + (0.2 - 0.1)^2 + (0.4 - 0.2)^2) = 0.244948974
```

## Generate Vector Embeddings

### Postgres

First, build and run Postgres. From the `postgres` dir:
```
bash build_and_run.bash
```
If you don't specify `VOLUME_DIR`, `${HOME}/vector_embeddings_db_volume}` is used.

### Load Books data

Go to https://www.kaggle.com/datasets/elvinrustam/books-dataset/data?select=BooksDataset.csv and download the dataset.
Once you have it, unzip it.


