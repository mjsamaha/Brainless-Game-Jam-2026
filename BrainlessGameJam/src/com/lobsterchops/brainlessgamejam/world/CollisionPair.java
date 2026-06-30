package com.lobsterchops.brainlessgamejam.world;

import com.lobsterchops.brainlessgamejam.entity.Collidable;
import com.lobsterchops.brainlessgamejam.entity.GameObject;
 
/**
 * A pair of objects whose bounds overlapped during a collision pass.
 *
 * <p>Both objects are guaranteed to be active and implement {@link Collidable}
 * at the time of detection. Order within the pair is not meaningful —
 * (a, b) and (b, a) represent the same collision.</p>
 *
 * @param <A> first object
 * @param <B> second object
 */
public record CollisionPair<A extends GameObject & Collidable,
                            B extends GameObject & Collidable>(A a, B b) {
}
 