package com.binaryigor.modularmonolith.backgroundsync.shared.events;

public interface ApplicationEventPublisher {
    <E> void publish(E event);
}
