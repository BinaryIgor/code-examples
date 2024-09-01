package com.binaryigor.vembeddingswithpostgres.data;

import com.binaryigor.vembeddingswithpostgres.shared.SizedStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

public class AmazonReviewsVectorEmbeddingsDataSource implements VectorEmbeddingsDataSource {

    private static final String DATA_TYPE = "AmazonReviews";
    private final VectorEmbeddingDataRepository dataRepository;
    private final int batchLoadSize;

    public AmazonReviewsVectorEmbeddingsDataSource(VectorEmbeddingDataRepository dataRepository, int batchLoadSize) {
        this.dataRepository = dataRepository;
        this.batchLoadSize = batchLoadSize;
    }

    @Override
    public String dataType() {
        return DATA_TYPE;
    }

    @Override
    public void load(String path) {
        var header = true;
        var dataBatch = new LinkedList<VectorEmbeddingData>();

        try (var reader = Files.newBufferedReader(Path.of(path))) {
            var line = reader.readLine();
            while (true) {
                if (header) {
                    header = false;
                    continue;
                }

                if (line == null) {
                    break;
                }

                dataBatch.add(embeddingDataFromCsvRow(line));
                if (dataBatch.size() >= batchLoadSize) {
                    dataRepository.save(dataBatch);
                    dataBatch.clear();
                }

                line = reader.readLine();
            }

            dataRepository.save(dataBatch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private VectorEmbeddingData embeddingDataFromCsvRow(String row) {
        var columns = row.split(",", 10);

        var id = columns[0];
        var userProfile = columns[3];
        var score = columns[6];
        var time = columns[7];
        var summary = columns[8];
        var text = columns[9];

        var review = new ReviewRecord(id, userProfile, score, time, summary, text);

        return new VectorEmbeddingData(review.id(), DATA_TYPE, review);
    }

    @Override
    public SizedStream<VectorEmbeddingInputData> get() {
        var data = dataRepository.allOfType(dataType(), ReviewRecord.class)
            .map(d -> new VectorEmbeddingInputData(d.id(), ((ReviewRecord) d.data()).embeddingData()));
        var size = dataRepository.countOfType(DATA_TYPE);

        return new SizedStream<>(data, size);
    }

    public record ReviewRecord(String id, String userProfile, String score, String time, String summary, String text) {

        public String embeddingData() {
            return score + "\n" + summary + "\n" + text;
        }
    }
}
