package com.lobsterchops.brainlessgamejam.world;

import java.util.ArrayList;
import java.util.List;
 
import com.lobsterchops.brainlessgamejam.entity.Collidable;
import com.lobsterchops.brainlessgamejam.entity.GameObject;
import com.lobsterchops.brainlessgamejam.event.CollisionEvent;
import com.lobsterchops.brainlessgamejam.event.EventBus;
 
/**
 * <h4>Broadphase collision detection.</h4>
 *
 * <p>Each tick, {@link #resolve(List)} iterates all active {@link Collidable}
 * objects, performs pairwise {@code Bounds.intersects} checks (O(n²), fine
 * for a jam), and publishes a {@link CollisionEvent} to the {@link EventBus}
 * for every overlapping pair.</p>
 *
 * <p>This class is intentionally mechanical — it detects overlaps and reports
 * them, but makes no decisions about what should happen. Game-specific reactions
 * (damage, destruction, score) belong in EventBus subscribers, not here.</p>
 *
 * <h4>Wiring</h4>
 * <p>Instantiated by {@link GameSystem} with the shared {@link EventBus}.
 * Called from {@code updateSystems()} each tick. Subscribe to
 * {@link CollisionEvent} in your scene's {@code enter()} to react.</p>
 *
 * <h4>Example subscriber</h4>
 * <pre>
 * eventBus.subscribe(CollisionEvent.class, e -> {
 *     if (e.a() instanceof Bullet b && e.b() instanceof Enemy en) {
 *         b.markInactive();
 *         en.takeDamage(b.getDamage());
 *         eventBus.publish(new EntityDestroyed(en));
 *     } else if (e.b() instanceof Bullet b && e.a() instanceof Enemy en) {
 *         b.markInactive();
 *         en.takeDamage(b.getDamage());
 *         eventBus.publish(new EntityDestroyed(en));
 *     }
 * });
 * </pre>
 */
public class CollisionSystem {
 
    private final EventBus eventBus;
 
    public CollisionSystem(EventBus eventBus) {
        this.eventBus = eventBus;
    }
 
    /**
     * Runs the pairwise check over all active collidable objects.
     * Call once per tick from {@link GameSystem}.
     *
     * @param objects the current live object list from GameSystem
     */
    public void resolve(List<GameObject> objects) {
        List<GameObject> collidables = collectCollidables(objects);
        int size = collidables.size();
 
        for (int i = 0; i < size - 1; i++) {
            GameObject a = collidables.get(i);
            Collidable ca = (Collidable) a;
 
            for (int j = i + 1; j < size; j++) {
                GameObject b = collidables.get(j);
                Collidable cb = (Collidable) b;
 
                if (ca.getBounds().intersects(cb.getBounds())) {
                    eventBus.publish(new CollisionEvent(a, b));
                }
            }
        }
    }
 
    private List<GameObject> collectCollidables(List<GameObject> objects) {
        List<GameObject> result = new ArrayList<>();
        for (GameObject obj : objects) {
            if (obj.isActive() && obj instanceof Collidable) {
                result.add(obj);
            }
        }
        return result;
    }
 
}