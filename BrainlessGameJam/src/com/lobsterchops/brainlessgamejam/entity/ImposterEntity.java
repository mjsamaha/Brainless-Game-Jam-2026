package com.lobsterchops.brainlessgamejam.entity;

import java.awt.Graphics2D;
import java.util.Random;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.world.Arena;
import com.lobsterchops.brainlessgamejam.world.TrailSystem;

/**
 * <h4>An imposter swarm entity.</h4>
 *
 * <p>Behaves like {@link FriendlyEntity} but with randomised AI parameters,
 * making each imposter subtly distinct in speed and flee sensitivity.
 * On collision with the player, this entity is marked inactive and a trail
 * segment is briefly attached then detached — wired in {@code PlayingScene.enter()}
 * via the {@code EventBus}.</p>
 */
public class ImposterEntity extends Entity {

	public static final float SIZE = 14f;

	private static final float WANDER_SPEED_MIN = 1.2f;
	private static final float WANDER_SPEED_MAX = 2.2f;

	private static final float FLEE_SPEED_MIN = 3.8f;
	private static final float FLEE_SPEED_MAX = 5.5f;

	private static final float FLEE_RADIUS_MIN = 100f;
	private static final float FLEE_RADIUS_MAX = 160f;

	private final SwarmAI ai;
	private final PlayerEntity player;
	private final Arena arena;

	public ImposterEntity(Vector2 spawnPos, PlayerEntity player, Arena arena) {
		this(spawnPos, player, arena, new Random());
	}

	public ImposterEntity(Vector2 spawnPos, PlayerEntity player, Arena arena, Random random) {
		super(spawnPos, SIZE, SIZE);
		this.player = player;
		this.arena  = arena;
		this.ai     = new SwarmAI(
			randomRange(random, WANDER_SPEED_MIN, WANDER_SPEED_MAX),
			randomRange(random, FLEE_SPEED_MIN,   FLEE_SPEED_MAX),
			randomRange(random, FLEE_RADIUS_MIN,  FLEE_RADIUS_MAX),
			random
		);
	}

	@Override
	public void update(UpdateContext context) {
		Vector2 velocity = ai.computeVelocity(getPosition(), player.getPosition(), context.tick(), arena);
		setVelocity(velocity);

		super.update(context);

		setPosition(arena.clamp(getPosition(), getWidth(), getHeight()));
	}

	@Override
	public void render(Graphics2D g2) {
		int x = Math.round(getPosition().x() - getWidth() / 2f);
		int y = Math.round(getPosition().y() - getHeight() / 2f);

		g2.setColor(ColorConfig.IMPOSTER);
		g2.fillRect(x, y, (int) getWidth(), (int) getHeight());
	}

	private static float randomRange(Random random, float min, float max) {
		return min + random.nextFloat() * (max - min);
	}

}