package com.lobsterchops.brainlessgamejam.entity;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.world.Arena;
import com.lobsterchops.brainlessgamejam.world.TrailSystem;

public class PlayerEntity extends Entity {

	public static final float SIZE = 20f;

	private static final float SPEED = 6f;

	private final InputManager input;
	private final Arena areana;
	private final TrailSystem trailSystem;

	public PlayerEntity(Vector2 spawnPos, InputManager input, Arena arena, TrailSystem trailSystem) {
		super(spawnPos, SIZE, SIZE);
		this.input = input;
		this.areana = arena;
		this.trailSystem = trailSystem;

	}
	
	@Override
	public void update(UpdateContext context) {
		Vector2 direction = input.movementDirection();
		setVelocity(direction.multiply(SPEED));
		
		super.update(context);
		
		setPosition(areana.clamp(getPosition(), getWidth(), getHeight()));
		
		trailSystem.record(getPosition());
	}
	
	@Override
	public void render(Graphics2D g2) {
		int x = Math.round(getPosition().x() - getWidth() / 2f);
		int y = Math.round(getPosition().y() - getHeight() / 2f);
		
		g2.setColor(ColorConfig.PLAYER_COLOR);
		g2.fillRect(x, y, (int)getWidth(), (int)getHeight());
	}

}
