package com.binaryigor.vembeddingswithpostgres.data;

import com.binaryigor.vembeddingswithpostgres.shared.SizedStream;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsDataSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Stream;

public class PerformanceTestVectorEmbeddingsDataSource implements VectorEmbeddingsDataSource {

    private static final String DATA_TYPE = "PERFORMANCE_TEST";
    private final int dataSize;

    public PerformanceTestVectorEmbeddingsDataSource(int dataSize) {
        this.dataSize = dataSize;
    }

    @Override
    public String dataType() {
        return DATA_TYPE;
    }

    @Override
    public void load(String path) {

    }

    @Override
    public SizedStream<VectorEmbeddingInputData> get() {
        try {
            var shaDigest = MessageDigest.getInstance("SHA-512");
            var dataStream = Stream.generate(() -> {
                    var id = "test-" + UUID.randomUUID();
                    var data = HexFormat.of().formatHex(shaDigest.digest(id.getBytes(StandardCharsets.UTF_8)));
                    return new VectorEmbeddingInputData(id, data);
                })
                .limit(dataSize);
            return new SizedStream<>(dataStream, dataSize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
