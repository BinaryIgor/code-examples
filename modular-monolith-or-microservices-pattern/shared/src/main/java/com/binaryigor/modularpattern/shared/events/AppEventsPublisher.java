package com.binaryigor.modularpattern.shared.events;

public interface AppEventsPublisher {

    <T> void publish(T event);
}
