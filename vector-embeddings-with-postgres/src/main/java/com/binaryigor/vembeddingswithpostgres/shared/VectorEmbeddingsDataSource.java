package com.binaryigor.vembeddingswithpostgres.shared;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;

public interface VectorEmbeddingsDataSource {

    String dataType();

    void load(String path);

    SizedStream<VectorEmbeddingInputData> get();
}
