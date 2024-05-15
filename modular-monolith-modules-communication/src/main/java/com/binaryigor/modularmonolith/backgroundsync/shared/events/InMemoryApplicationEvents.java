package com.binaryigor.modularmonolith.backgroundsync.shared.events;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InMemoryApplicationEvents implements ApplicationEvents, ApplicationEventPublisher {

    private final Map<Class<?>, Collection<Consumer<Object>>> subscribersByType = new ConcurrentHashMap<>();

    @Override
    public <E> void publish(E event) {
        subscribersByType.getOrDefault(event.getClass(), List.of())
                .forEach(s -> s.accept(event));
    }

    @SuppressWarnings("unchecked cast")
    @Override
    public <E> void subscribe(Class<E> event, Consumer<E> subscriber) {
        subscribersByType.computeIfAbsent(event, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add((Consumer<Object>) subscriber);
    }
}
