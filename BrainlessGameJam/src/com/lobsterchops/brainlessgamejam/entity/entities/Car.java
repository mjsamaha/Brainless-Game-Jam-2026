package com.lobsterchops.brainlessgamejam.entity.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.Entity;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.graphics.AssetManager;
import com.lobsterchops.brainlessgamejam.graphics.Gfx;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.RenderLayer;
import com.lobsterchops.brainlessgamejam.world.TileMap;

public class Car extends Entity {

	public static final float WIDTH = 48f;
	public static final float HEIGHT = 24f;

	// Colours cycled by lane index for readability
	private static final String[] LANE_SPRITES = { Gfx.CAR_RED, Gfx.CAR_BLUE, Gfx.CAR_ORANGE, Gfx.CAR_RED, // cycles
																											// back for
																											// lanes 3+
			Gfx.CAR_BLUE, };

	private final float speed;
	private final BufferedImage sprite;

	public Car(Vector2 position, float speed, int laneIndex) {
		super(position, WIDTH, HEIGHT);
		this.speed = speed;
		String spritePath = LANE_SPRITES[Math.abs(laneIndex) % LANE_SPRITES.length];
		this.sprite = AssetManager.get(spritePath, (int) WIDTH, (int) HEIGHT);
	}

	@Override
	public void update(UpdateContext context) {
		setVelocity(new Vector2(speed, 0));
		super.update(context); // applies velocity → position
		wrapAroundWorld();
	}

	@Override
	public void render(Graphics2D g2) {
		int drawX = (int) (getPosition().x() - WIDTH / 2f);
		int drawY = (int) (getPosition().y() - HEIGHT / 2f);

		if (sprite != null) { // Draw the car sprite if available
			drawSprite(g2, drawX, drawY);

		} else { // Fallback: draw a magenta rectangle if the sprite is missing
			g2.setColor(java.awt.Color.MAGENTA);
			g2.fillRect(drawX, drawY, (int) WIDTH, (int) HEIGHT);
		}
	}

	private void drawSprite(Graphics2D g2, int drawX, int drawY) {
	    if (speed > 0) {
	        g2.drawImage(sprite, drawX + (int) WIDTH, drawY, -(int) WIDTH, (int) HEIGHT, null);
	    } else {
	        g2.drawImage(sprite, drawX, drawY, null);
	    }
	}

	@Override
	public RenderLayer getRenderLayer() {
		return RenderLayer.ENTITIES;
	}

	private void wrapAroundWorld() {
		TileMap tileMap = ServiceLocator.resolve(TileMap.class);
		float worldWidth = tileMap.worldWidth();
		float x = getPosition().x();
		float y = getPosition().y();

		// Moving right — wrap when fully off the right edge
		if (speed > 0 && x - WIDTH / 2f > worldWidth) {
			setPosition(new Vector2(-WIDTH / 2f, y));
		}
		// Moving left — wrap when fully off the left edge
		else if (speed < 0 && x + WIDTH / 2f < 0) {
			setPosition(new Vector2(worldWidth + WIDTH / 2f, y));
		}
	}
}