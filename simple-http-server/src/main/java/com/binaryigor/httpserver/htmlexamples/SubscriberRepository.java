package com.binaryigor.httpserver.htmlexamples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriberRepository {

    private final Map<String, Subscriber> db = new ConcurrentHashMap<>();

    public void save(Subscriber subscriber) {
        db.put(subscriber.email(), subscriber);
    }

    public List<Subscriber> all() {
        return new ArrayList<>(db.values());
    }
}
