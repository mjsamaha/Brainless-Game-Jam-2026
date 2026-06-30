package com.lobsterchops.brainlessgamejam.entity;

import com.lobsterchops.brainlessgamejam.math.Bounds;
import com.lobsterchops.brainlessgamejam.math.Vector2;

public abstract class Entity implements GameObject, Collidable, Renderable {

	private Vector2 position;
	private Vector2 velocity;
	private final float width;
	private final float height;
	private boolean active = true;

	protected Entity(Vector2 position, float width, float height) {
		this.position = position;
		this.velocity = Vector2.ZERO;
		this.width = width;
		this.height = height;
	}

	@Override
	public void update(UpdateContext context) {
		position = position.add(velocity);
	}

	@Override
	public Bounds getBounds() {
		return Bounds.fromCenter(position, width, height);
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public void markInactive() {
		active = false;
	}

	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}
}