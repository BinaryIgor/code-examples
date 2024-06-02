package com.binaryigor.modularpattern.shared.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class InMemoryAppEvents implements AppEvents {

    private static final Logger log = LoggerFactory.getLogger(InMemoryAppEvents.class);
    private final Map<Class<?>, List<EvenSubscriber<Object>>> eventsSubscribers = new HashMap<>();
    private final Supplier<AppEventsPublisher> publisher = new Supplier<>() {

        private AppEventsPublisher publisher;

        @Override
        public AppEventsPublisher get() {
            if (publisher == null) {
                publisher = newPublisher();
            }
            return publisher;
        }
    };

    private AppEventsPublisher newPublisher() {
        return new AppEventsPublisher() {

            @Override
            public <T> void publish(T event) {
                var subscribers = eventsSubscribers.getOrDefault(event.getClass(), List.of());
                if (subscribers.isEmpty()) {
                    return;
                }

                Collection<RuntimeException> exceptions;
                if (subscribers.size() == 1) {
                    exceptions = new ArrayList<>();
                    handleSubscriber(subscribers.getFirst(), event, exceptions);
                } else {
                    exceptions = Collections.newSetFromMap(new ConcurrentHashMap<>());
                    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                        subscribers.forEach(s -> executor.submit(() -> {
                            handleSubscriber(s, event, exceptions);
                        }));
                    }
                }

                throwExceptionIf(exceptions, event);
            }

            private <T> void handleSubscriber(EvenSubscriber<Object> subscriber,
                                              T event,
                                              Collection<RuntimeException> exceptions) {
                try {
                    subscriber.handle(event);
                } catch (Exception e) {
                    log.error("Problem while handling {} message:", event.getClass(), e);
                    exceptions.add((RuntimeException) e);
                }
            }

            private <T> void throwExceptionIf(Collection<RuntimeException> exceptions, T event) {
                if (exceptions.size() == 1) {
                    throw exceptions.iterator().next();
                } else if (exceptions.size() > 1) {
                    var combinedException = new RuntimeException(
                        "There were problems while handling %s message".formatted(event.getClass()));

                    exceptions.forEach(combinedException::addSuppressed);

                    throw combinedException;
                }
            }
        };
    }

    @Override
    public AppEventsPublisher publisher() {
        return publisher.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> event, EvenSubscriber<T> subscriber) {
        eventsSubscribers.computeIfAbsent(event, k -> new ArrayList<>())
            .add((EvenSubscriber<Object>) subscriber);
    }


}
