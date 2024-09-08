package com.binaryigor.vembeddingswithpostgres.data;

import com.binaryigor.vembeddingswithpostgres.shared.CsvFile;
import com.binaryigor.vembeddingswithpostgres.shared.Extensions;
import com.binaryigor.vembeddingswithpostgres.shared.SizedStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;

public class BooksVectorEmbeddingsDataSource implements VectorEmbeddingsDataSource {

    private static final String DATA_TYPE = "Books";
    private final VectorEmbeddingDataRepository dataRepository;
    private final int batchLoadSize;

    public BooksVectorEmbeddingsDataSource(VectorEmbeddingDataRepository dataRepository, int batchLoadSize) {
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
        var dataBatch = new HashMap<String, VectorEmbeddingData>();

        try (var reader = Files.newBufferedReader(Path.of(path))) {

            var hashDigest = MessageDigest.getInstance("SHA-256");

            var line = reader.readLine();
            while (true) {
                if (header) {
                    header = false;
                    continue;
                }

                if (line == null) {
                    break;
                }

                var embeddingData = embeddingDataFromCsvRow(line, hashDigest);
                dataBatch.put(embeddingData.id(), embeddingData);
                if (dataBatch.size() >= batchLoadSize) {
                    dataRepository.save(dataBatch.values());
                    dataBatch.clear();
                }

                line = reader.readLine();
            }

            dataRepository.save(dataBatch.values());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private VectorEmbeddingData embeddingDataFromCsvRow(String row, MessageDigest hashDigest) {
        var columns = CsvFile.columns(row, ",");

        var title = columns.get(0);
        var authors = columns.get(1);
        var description = columns.get(2);
        var category = columns.get(3);
        var publisher = columns.get(4);
        var publishDate = columns.get(5);
        var price = columns.get(6);

        var review = new BookRecord(title, authors, description, category, publisher, publishDate, price);

        var id = Extensions.hashBasedId(hashDigest, 32, title, publisher);

        return new VectorEmbeddingData(id, DATA_TYPE, review);
    }

    @Override
    public SizedStream<VectorEmbeddingInputData> get() {
        var data = dataRepository.allOfType(dataType(), BookRecord.class)
            .map(d -> new VectorEmbeddingInputData(d.id(), ((BookRecord) d.data()).embeddingData()));
        var size = dataRepository.countOfType(DATA_TYPE);

        return new SizedStream<>(data, size);
    }

    public record BookRecord(String title,
                             String authors,
                             String description,
                             String category,
                             String publisher,
                             String publishDate,
                             String price) {

        public String id() {
            return HexFormat.of().formatHex((title + publisher + publishDate + price).getBytes(StandardCharsets.UTF_8));
        }

        public String embeddingData() {
            return title + "\n\n" + description;
        }
    }
}
