package com.lobsterchops.brainlessgamejam.world;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
 
import com.lobsterchops.brainlessgamejam.entity.Collidable;
import com.lobsterchops.brainlessgamejam.entity.GameObject;
 
/**
 * <h4>Broadphase collision detection.</h4>
 *
 * <p>Each tick, {@link #resolve(List)} iterates all active {@link Collidable}
 * objects, performs pairwise {@code Bounds.intersects} checks (O(n²), fine
 * for a jam), and dispatches detected pairs to a registered listener.</p>
 *
 * <p>This class is intentionally mechanical — it detects overlaps and reports
 * them, but makes no decisions about what should happen. Game-specific reactions
 * (damage, destruction, score) belong in the listener, not here.</p>
 *
 * <h4>Usage</h4>
 * <pre>
 * collisionSystem.onCollision((a, b) -> {
 *     if (a instanceof Bullet bullet && b instanceof Enemy enemy) {
 *         bullet.markInactive();
 *         enemy.takeDamage(bullet.getDamage());
 *     }
 * });
 * </pre>
 *
 * <h4>Wiring</h4>
 * <p>Instantiated by {@link GameSystem}, called from {@code updateSystems()}
 * each tick. Listeners are registered after construction, typically in
 * {@code GameContext} or a scene's {@code enter()}.</p>
 */
public class CollisionSystem {
 
    private BiConsumer<GameObject, GameObject> listener;
 
    /**
     * Registers the listener that receives each detected collision pair.
     * Only one listener is supported — replace if needed.
     *
     * @param listener receives (a, b) for every overlapping pair this tick
     */
    public void onCollision(BiConsumer<GameObject, GameObject> listener) {
        this.listener = listener;
    }
 
    /**
     * Runs the pairwise check over all active collidable objects in the list.
     * Call once per tick from {@link GameSystem#updateSystems}.
     *
     * @param objects the current live object list from GameSystem
     */
    public void resolve(List<GameObject> objects) {
        if (listener == null) return;
 
        List<GameObject> collidables = collectCollidables(objects);
        int size = collidables.size();
 
        for (int i = 0; i < size - 1; i++) {
            GameObject a = collidables.get(i);
            Collidable ca = (Collidable) a;
 
            for (int j = i + 1; j < size; j++) {
                GameObject b = collidables.get(j);
                Collidable cb = (Collidable) b;
 
                if (ca.getBounds().intersects(cb.getBounds())) {
                    listener.accept(a, b);
                }
            }
        }
    }
 
    /**
     * Filters the object list down to active objects that implement Collidable.
     */
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