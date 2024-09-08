package com.binaryigor.vembeddingswithpostgres.data;

import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsDataSource;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsSupportedDataSources;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TheVectorEmbeddingsSupportedDataSources implements VectorEmbeddingsSupportedDataSources {

    private final Collection<String> dataSources;

    public TheVectorEmbeddingsSupportedDataSources(List<VectorEmbeddingsDataSource> dataSources) {
        this.dataSources = dataSources.stream().map(VectorEmbeddingsDataSource::dataType).collect(Collectors.toSet());
    }

    @Override
    public Collection<String> get() {
        return dataSources;
    }
}
