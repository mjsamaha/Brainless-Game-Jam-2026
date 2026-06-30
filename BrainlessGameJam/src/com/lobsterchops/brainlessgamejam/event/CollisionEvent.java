package com.lobsterchops.brainlessgamejam.event;


import com.lobsterchops.brainlessgamejam.entity.GameObject;
 
/**
 * Published by {@code CollisionSystem} for every pair of overlapping
 * {@code Collidable} objects detected in a single tick.
 *
 * <p>Order of {@code a} and {@code b} is not meaningful — (a, b) and
 * (b, a) represent the same collision. Subscribers should check both
 * orderings when pattern-matching on concrete types.</p>
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
 *
 * @param a first object in the overlapping pair
 * @param b second object in the overlapping pair
 */
public record CollisionEvent(GameObject a, GameObject b) {
}
 