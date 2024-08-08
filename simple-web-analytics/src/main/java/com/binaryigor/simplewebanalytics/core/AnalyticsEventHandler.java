package com.binaryigor.simplewebanalytics.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AnalyticsEventHandler {

    public static final int MAX_FIELDS_SIZE = 100;
    public static final int MAX_URL_SIZE = 500;
    public static final int MAX_JSON_DATA_SIZE = 5_000;
    private final Semaphore mutex = new Semaphore(1);
    private final List<AnalyticsEvent> toCreateEvents = new LinkedList<>();
    private final AnalyticsEventRepository analyticsEventRepository;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    private final int batchSize;

    public AnalyticsEventHandler(AnalyticsEventRepository analyticsEventRepository,
                                 ObjectMapper objectMapper,
                                 ScheduledExecutorService scheduler,
                                 int batchSize,
                                 int checkBatchDelay) {
        this.analyticsEventRepository = analyticsEventRepository;
        this.objectMapper = objectMapper;
        this.scheduler = scheduler;
        this.batchSize = batchSize;

        scheduler.scheduleWithFixedDelay(this::createEvents, checkBatchDelay, checkBatchDelay, TimeUnit.MILLISECONDS);
    }

    public AnalyticsEventHandler(AnalyticsEventRepository analyticsEventRepository,
                                 ObjectMapper objectMapper,
                                 int batchSize,
                                 int checkBatchDelay) {
        this(analyticsEventRepository, objectMapper, Executors.newScheduledThreadPool(1), batchSize, checkBatchDelay);
    }

    private void createEvents() {
        executeLocking(() -> {
            if (!toCreateEvents.isEmpty()) {
                analyticsEventRepository.create(toCreateEvents);
                toCreateEvents.clear();
            }
        });
    }

    private void executeLocking(Runnable runnable) {
        try {
            mutex.acquire();
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            mutex.release();
        }
    }

    public void handle(AnalyticsEvent event) {
        validateEvent(event);
        addEventOrCreateIfBatchIsFull(event);
    }

    private void validateEvent(AnalyticsEvent event) {
        if (isFieldInvalid(event.ip())) {
            throw AnalyticsEventException.ofField("ip", event.ip());
        }
        if (isFieldInvalid(event.url(), MAX_URL_SIZE)) {
            throw AnalyticsEventException.ofField("url", event.url());
        }
        if (isFieldInvalid(event.browser())) {
            throw AnalyticsEventException.ofField("browser", event.browser());
        }
        if (isFieldInvalid(event.platform())) {
            throw AnalyticsEventException.ofField("platform", event.platform());
        }
        if (isFieldInvalid(event.device())) {
            throw AnalyticsEventException.ofField("device", event.device());
        }
        if (isFieldInvalid(event.type())) {
            throw AnalyticsEventException.ofField("type", event.type());
        }
        if (event.data() == null) {
            return;
        }
        var dataJson = dataJson(event.data());
        if (dataJson.length() > MAX_JSON_DATA_SIZE) {
            throw AnalyticsEventException.ofField("data", dataJson);
        }
    }

    private String dataJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isFieldInvalid(String field) {
        return isFieldInvalid(field, MAX_FIELDS_SIZE);
    }

    private boolean isFieldInvalid(String field, int maxSize) {
        return !hasAnyContent(field) || isLongerThan(field, maxSize);
    }

    private boolean hasAnyContent(String string) {
        return !(string == null || string.isBlank());
    }

    private boolean isLongerThan(String string, int length) {
        return string != null && string.length() > length;
    }

    private void addEventOrCreateIfBatchIsFull(AnalyticsEvent event) {
        executeLocking(() -> {
            toCreateEvents.add(event);
            if (toCreateEvents.size() >= batchSize) {
                analyticsEventRepository.create(toCreateEvents);
                toCreateEvents.clear();
            }
        });
    }

    public void shutdown() {
        scheduler.shutdown();
        createEvents();
    }
}
