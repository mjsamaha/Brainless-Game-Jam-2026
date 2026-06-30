package com.lobsterchops.brainlessgamejam.event;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;
 
/**
 * <h4>Lightweight synchronous event bus.</h4>
 *
 * <p>Systems publish plain event objects; other systems subscribe by event type.
 * Decouples publishers from subscribers — a collision resolving a bullet hit
 * can publish {@code EntityDestroyed} without knowing whether audio, score,
 * or particles are listening.</p>
 *
 * <h4>Usage</h4>
 * <pre>
 * // Subscribe (typically in a scene's enter()):
 * eventBus.subscribe(EntityDestroyed.class, e -> score.add(e.entity()));
 *
 * // Publish (from CollisionSystem, WaveManager, etc.):
 * eventBus.publish(new EntityDestroyed(enemy));
 * </pre>
 *
 * <h4>Design notes</h4>
 * <ul>
 *   <li>Synchronous — subscribers run on the same thread as the publisher,
 *       inside the game loop tick. No threading surprises.</li>
 *   <li>Exceptions in one subscriber are caught and logged so they cannot
 *       prevent other subscribers from running.</li>
 *   <li>Call {@link #clear()} when resetting a run to drop all subscriptions
 *       registered by the previous scene.</li>
 * </ul>
 */
public class EventBus {
 
    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());
 
    private final Map<Class<?>, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();
 
    /**
     * Subscribes {@code handler} to receive events of exactly {@code eventType}.
     *
     * @param eventType the class of the event (e.g. {@code EntityDestroyed.class})
     * @param handler   called synchronously when a matching event is published
     * @param <T>       event type
     */
    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers
                .computeIfAbsent(eventType, k -> new ArrayList<>())
                .add((Consumer<Object>) handler);
    }
 
    /**
     * Publishes {@code event} to all subscribers registered for its exact type.
     *
     * @param event the event to dispatch; must not be null
     */
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
 
    /**
     * Removes all subscribers. Call when resetting between runs so stale
     * scene-level subscriptions don't linger into the next run.
     */
    public void clear() {
        subscribers.clear();
    }
 
}