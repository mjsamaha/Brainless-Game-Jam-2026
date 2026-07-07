package com.lobsterchops.brainlessgamejam.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class EventBus {

    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());

    private final Map<Class<?>, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers
                .computeIfAbsent(eventType, k -> new ArrayList<>())
                .add((Consumer<Object>) handler);
    }

    public void publish(Object event) {
        if (event == null) {
            LOGGER.warning("Attempted to publish a null event; ignoring.");
            return;
        }

        List<Consumer<Object>> handlers = subscribers.get(event.getClass());
        if (handlers == null || handlers.isEmpty()) return;

        for (Consumer<Object> handler : handlers) {
            try {
                handler.accept(event);
            } catch (Exception e) {
                LOGGER.warning("Exception in EventBus subscriber for "
                        + event.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    public void clear() {
        subscribers.clear();
    }
}