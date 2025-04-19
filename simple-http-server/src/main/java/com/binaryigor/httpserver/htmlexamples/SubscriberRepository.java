package com.binaryigor.httpserver.htmlexamples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SubscriberRepository {

    private final List<Subscriber> db = new CopyOnWriteArrayList<>();

    public void save(Subscriber subscriber) {
        db.add(subscriber);
    }

    public List<Subscriber> all() {
        return new ArrayList<>(db);
    }
}
