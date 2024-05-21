package com.binaryigor.modularpattern.shared.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class InMemoryAppEvents implements AppEvents {

    private static final Logger log = LoggerFactory.getLogger(InMemoryAppEvents.class);
    private final Map<Class<?>, Collection<EvenSubscriber<Object>>> eventsSubscribers = new HashMap<>();
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
                var exceptions = new ArrayList<RuntimeException>();

                eventsSubscribers.getOrDefault(event.getClass(), List.of())
                        .forEach(s -> handleSubscriber(s, event, exceptions));

                throwExceptionIf(exceptions, event);
            }

            private <T> void handleSubscriber(EvenSubscriber<Object> subscriber,
                                              T event,
                                              List<RuntimeException> exceptions) {
                try {
                    subscriber.handle(event);
                } catch (Exception e) {
                    log.error("Problem while handling {} message:", event.getClass(), e);
                    exceptions.add((RuntimeException) e);
                }
            }

            private <T> void throwExceptionIf(List<RuntimeException> exceptions, T event) {
                if (exceptions.size() == 1) {
                    throw exceptions.getFirst();
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
