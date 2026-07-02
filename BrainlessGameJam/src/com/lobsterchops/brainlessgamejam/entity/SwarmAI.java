package com.lobsterchops.brainlessgamejam.entity;

import java.util.Random;

import com.lobsterchops.brainlessgamejam.math.Bounds;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.world.Arena;

/**
 * <h4>Shared wander + flee behaviour for swarm entities.</h4>
 *
 * <p>Each swarm entity owns one {@code SwarmAI} instance. Call
 * {@link #computeVelocity} once per tick to get the velocity the entity
 * should apply that frame.</p>
 *
 * <h4>Behaviour</h4>
 * <ul>
 *   <li><b>Flee</b> — if the player is within {@code fleeRadius}, move
 *       directly away from the player at {@code fleeSpeed}.</li>
 *   <li><b>Wander</b> — otherwise, move toward a random point inside the
 *       arena at {@code wanderSpeed}. A new target is chosen when the entity
 *       arrives within {@link #ARRIVAL_THRESHOLD} pixels, or after
 *       {@link #WANDER_COOLDOWN_TICKS} ticks have elapsed.</li>
 * </ul>
 *
 * <h4>Design</h4>
 * <p>{@code SwarmAI} holds only the config and wander state it needs.
 * Position, velocity, and arena clamping remain the entity's responsibility.</p>
 */
public class SwarmAI {

	private static final float ARRIVAL_THRESHOLD = 8f;
	private static final long  WANDER_COOLDOWN_TICKS = 120L;

	private final float wanderSpeed;
	private final float fleeSpeed;
	private final float fleeRadius;

	private final Random random;

	private Vector2 wanderTarget = null;
	private long    wanderTargetTick = 0L;

	public SwarmAI(float wanderSpeed, float fleeSpeed, float fleeRadius) {
		this(wanderSpeed, fleeSpeed, fleeRadius, new Random());
	}

	public SwarmAI(float wanderSpeed, float fleeSpeed, float fleeRadius, Random random) {
		this.wanderSpeed = wanderSpeed;
		this.fleeSpeed   = fleeSpeed;
		this.fleeRadius  = fleeRadius;
		this.random      = random;
	}

	/**
	 * Returns the velocity this entity should use this tick.
	 *
	 * @param position  the entity's current world position
	 * @param playerPos the player's current world position
	 * @param tick      the current game tick (from {@code UpdateContext})
	 * @param arena     used to pick valid wander targets
	 * @return a velocity vector; never null
	 */
	public Vector2 computeVelocity(Vector2 position, Vector2 playerPos, long tick, Arena arena) {
		if (position.distanceTo(playerPos) <= fleeRadius) {
			return flee(position, playerPos);
		}
		return wander(position, tick, arena);
	}


	private Vector2 flee(Vector2 position, Vector2 playerPos) {
		return playerPos.directionTo(position).multiply(fleeSpeed);
	}

	private Vector2 wander(Vector2 position, long tick, Arena arena) {
		if (shouldPickNewTarget(position, tick)) {
			wanderTarget     = randomPointInArena(arena);
			wanderTargetTick = tick;
		}
		return position.directionTo(wanderTarget).multiply(wanderSpeed);
	}

	private boolean shouldPickNewTarget(Vector2 position, long tick) {
		if (wanderTarget == null) return true;
		if (position.distanceTo(wanderTarget) < ARRIVAL_THRESHOLD) return true;
		if (tick - wanderTargetTick >= WANDER_COOLDOWN_TICKS) return true;
		return false;
	}

	private Vector2 randomPointInArena(Arena arena) {
		Bounds bounds = arena.getBounds();
		float x = bounds.x() + random.nextFloat() * bounds.width();
		float y = bounds.y() + random.nextFloat() * bounds.height();
		return new Vector2(x, y);
	}
}