package com.binaryigor.httpserver.htmlexamples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileUploadRepository {

    private final List<FileUpload> db = new CopyOnWriteArrayList<>();

    public void save(FileUpload upload) {
        db.add(upload);
    }

    public List<FileUpload> all() {
        return new ArrayList<>(db);
    }
}
