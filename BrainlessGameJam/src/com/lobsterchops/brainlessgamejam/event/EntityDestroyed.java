package com.lobsterchops.brainlessgamejam.event;

import com.lobsterchops.brainlessgamejam.entity.GameObject;

/**
 * Published when an entity is destroyed (marked inactive) during gameplay.
 *
 * <p>Typical publishers: {@code CollisionSystem} handler, enemy AI, player death logic.</p>
 * <p>Typical subscribers: score system, particle system, audio service (death SFX),
 * wave manager (enemy count tracking).</p>
 *
 * @param entity the object that was destroyed; will be inactive by the time
 *               subscribers receive this event
 */
public record EntityDestroyed(GameObject entity) {
}
 