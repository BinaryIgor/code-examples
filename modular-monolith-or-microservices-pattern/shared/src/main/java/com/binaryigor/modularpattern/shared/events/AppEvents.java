package com.binaryigor.modularpattern.shared.events;

public interface AppEvents {

    <T> void subscribe(Class<T> event, EvenSubscriber<T> listener);

    AppEventsPublisher publisher();
}
