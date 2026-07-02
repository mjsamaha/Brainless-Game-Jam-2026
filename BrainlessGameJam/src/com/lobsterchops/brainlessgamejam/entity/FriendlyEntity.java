package com.lobsterchops.brainlessgamejam.entity;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.world.Arena;

/**
 * <h4>A friendly swarm entity.</h4>
 *
 * <p>Wanders the arena and flees from the player using {@link SwarmAI}.
 * On collision with the player, this entity is marked inactive and a trail
 * segment is attached via {@link com.lobsterchops.brainlessgamejam.world.TrailSystem}
 * — wired in {@code PlayingScene.enter()} via the {@code EventBus}.</p>
 */
public class FriendlyEntity extends Entity {

	public static final float SIZE = 14f;

	private static final float WANDER_SPEED = 1.5f;
	private static final float FLEE_SPEED   = 3.5f;
	private static final float FLEE_RADIUS  = 120f;

	private final SwarmAI ai;
	private final PlayerEntity player;
	private final Arena arena;

	public FriendlyEntity(Vector2 spawnPos, PlayerEntity player, Arena arena) {
		super(spawnPos, SIZE, SIZE);
		this.player = player;
		this.arena  = arena;
		this.ai     = new SwarmAI(WANDER_SPEED, FLEE_SPEED, FLEE_RADIUS);
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

		g2.setColor(ColorConfig.FRIENDLY);
		g2.fillRect(x, y, (int) getWidth(), (int) getHeight());
	}

}