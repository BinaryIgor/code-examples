package com.binaryigor.vembeddingswithpostgres;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingData;
import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingDataRepository;
import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;
import com.binaryigor.vembeddingswithpostgres.shared.SizedStream;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsDataSource;

import java.util.List;
import java.util.function.Supplier;

public class TestVectorEmbeddingsDataSource implements VectorEmbeddingsDataSource {

    public static final String DATA_TYPE = "TEST";
    private final VectorEmbeddingDataRepository dataRepository;
    private Supplier<List<VectorEmbeddingInputData>> dataSource = List::of;

    public TestVectorEmbeddingsDataSource(VectorEmbeddingDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void dataSource(List<VectorEmbeddingInputData> dataSource) {
        this.dataSource = () -> dataSource;
    }

    @Override
    public String dataType() {
        return DATA_TYPE;
    }

    @Override
    public int load(String path) {
        var dataToSave = dataSource.get().stream()
            .map(d -> new VectorEmbeddingData(d.id(), dataType(), new SingleValueJson(d.data())))
            .toList();
        dataRepository.save(dataToSave);

        return dataToSave.size();
    }

    @Override
    public SizedStream<VectorEmbeddingInputData> get() {
        var dataStream = dataRepository.allOfType(dataType(), SingleValueJson.class)
            .map(d -> new VectorEmbeddingInputData(d.id(), ((SingleValueJson) d.data()).value()));

        var dataSize = dataRepository.countOfType(dataType());

        return new SizedStream<>(dataStream, dataSize);
    }

    record SingleValueJson(String value) {
    }
}
