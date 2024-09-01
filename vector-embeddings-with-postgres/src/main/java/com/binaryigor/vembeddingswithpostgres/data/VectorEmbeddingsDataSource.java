package com.binaryigor.vembeddingswithpostgres.data;

import com.binaryigor.vembeddingswithpostgres.shared.SizedStream;

public interface VectorEmbeddingsDataSource {

    String dataType();

    void load(String path);

    SizedStream<VectorEmbeddingInputData> get();
}
