package com.lobsterchops.brainlessgamejam.entity;

import java.awt.Color;
import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.RenderLayer;

public class SlimeParent extends Entity {
 
	private static final float SIZE   = 32f;
	private static final float SPEED  = 2.5f;
	private static final Color COLOUR = new Color(255, 220, 0); // yellow
 
	public SlimeParent(Vector2 position) {
		super(position, SIZE, SIZE);
	}
 
	@Override
	public void update(UpdateContext context) {
		InputManager input = ServiceLocator.resolve(InputManager.class);
		Vector2 direction = input.movementDirection();
		setVelocity(direction.multiply(SPEED));
		super.update(context); // applies velocity to position
	}
 
	@Override
	public void render(Graphics2D g2) {
		int x = Math.round(getPosition().x() - getWidth()  / 2f);
		int y = Math.round(getPosition().y() - getHeight() / 2f);
 
		g2.setColor(COLOUR);
		g2.fillRect(x, y, (int) getWidth(), (int) getHeight());
	}
 
	@Override
	public RenderLayer getRenderLayer() {
		return RenderLayer.ENTITIES;
	}
 
}
 