package com.binaryigor.modularpattern.shared.events;

public interface EvenSubscriber<T> {
    void handle(T event);
}
