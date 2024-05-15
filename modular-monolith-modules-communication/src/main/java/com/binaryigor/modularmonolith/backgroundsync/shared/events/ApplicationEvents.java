package com.binaryigor.modularmonolith.backgroundsync.shared.events;

import java.util.function.Consumer;

public interface ApplicationEvents {
    <E> void subscribe(Class<E> event, Consumer<E> subscriber);
}
