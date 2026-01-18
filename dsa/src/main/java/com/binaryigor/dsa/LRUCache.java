package com.binaryigor.dsa;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LRUCache {

    private final Map<Integer, NodeEntry> entries;
    private final int capacity;
    private final AtomicInteger nextRank = new AtomicInteger();
    private final AtomicReference<NodeEntry> head = new AtomicReference<>();
    private final AtomicReference<NodeEntry> tail = new AtomicReference<>();

    public LRUCache(int capacity) {
        entries = new HashMap<>(capacity);
        this.capacity = capacity;
    }

    public int get(int key) {
        var entry = entries.get(key);
        if (entry == null) {
            return -1;
        }

        updateEntry(key, entry.value);

        return entry.value;
    }

    private void updateEntry(int key, int value) {
        var nodeEntry = new NodeEntry(key, value, nextRank.getAndIncrement());
        var previous = entries.put(key, nodeEntry);
        if (previous != null) {
            unlinkEntry(previous);
        }

        if (head.get() == null) {
            head.set(nodeEntry);
            return;
        }

        var currentTail = tail.get();
        if (currentTail != null) {
            currentTail.setNext(nodeEntry);
            nodeEntry.setPrev(currentTail);
            tail.set(nodeEntry);
            return;
        }

        tail.set(nodeEntry);

        if (head.get() != null) {
            nodeEntry.setPrev(head.get());
            head.get().setNext(nodeEntry);
        }
    }

    private void unlinkEntry(NodeEntry entry) {
        var prevEntry = entry.prevValue();
        if (prevEntry != null) {
            prevEntry.setNext(entry.nextValue());
        }
        var nextEntry = entry.nextValue();
        if (nextEntry != null) {
            nextEntry.setPrev(prevEntry);
        }
        if (entry == head.get()) {
            head.set(nextEntry);
        }
        if (entry == tail.get()) {
            tail.set(prevEntry);
        }
    }

    public void put(int key, int value) {
        updateEntry(key, value);

        if (entries.size() > capacity) {
            var leastUsed = head.get();

            entries.remove(leastUsed.key);

            var newHead = leastUsed.next.get();
            head.set(newHead);

            unlinkEntry(leastUsed);
        }
    }

    record NodeEntry(int key, int value, int rank,
                     AtomicReference<NodeEntry> prev,
                     AtomicReference<NodeEntry> next) {

        NodeEntry {
            validateNotSelfReferenceLink(prev.get(), next.get());
        }

        NodeEntry(int key, int value, int rank) {
            this(key, value, rank, new AtomicReference<>(), new AtomicReference<>());
        }

        private void validateNotSelfReferenceLink(NodeEntry prev, NodeEntry next) {
            if (prev == this) {
                throw new IllegalArgumentException("Cannot link prev node to itself; key=%s, value=%s"
                        .formatted(key, value));
            }
            if (next == this) {
                throw new IllegalArgumentException("Cannot link prev next to itself; key=%s, value=%s"
                        .formatted(key, value));
            }
        }

        void setPrev(NodeEntry prev) {
            validateNotSelfReferenceLink(prev, nextValue());
            this.prev.set(prev);
        }

        void setNext(NodeEntry next) {
            validateNotSelfReferenceLink(prevValue(), next);
            this.next.set(next);
        }

        NodeEntry prevValue() {
            return prev.get();
        }

        NodeEntry nextValue() {
            return next.get();
        }

        @Override
        public String toString() {
            return "NodeEntry[key=%s, value=%s, rank=%s, prev=%s, next=%s]"
                    .formatted(key, value, rank,
                            prev.get() == null ? "null" : prev.get().hashCode(),
                            next.get() == null ? "null" : next.get().hashCode());
        }
    }

    NodeEntry head() {
        return head.get();
    }
}
