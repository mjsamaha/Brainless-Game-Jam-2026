package com.lobsterchops.brainlessgamejam.world;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.config.ScreenConfig;
import com.lobsterchops.brainlessgamejam.math.Bounds;
import com.lobsterchops.brainlessgamejam.math.Vector2;

public class Arena {
	
	public static final int INSET = 80;
	public static final int BORDER_WIDTH = 2;
	
	private final float x;
	private final float y;
	private final float width;
	private final float height;
	
	public Arena() {	
		this.x = INSET;
		this.y = INSET;
		this.width = ScreenConfig.WIDTH - (INSET * 2);
		this.height = ScreenConfig.HEIGHT - (INSET * 2);
	}
	
	public Bounds getBounds() {
		return new Bounds(x, y, width, height);
	}
	
	public Vector2 clamp(Vector2 pos, float entityWidth, float entityHeight) {
		float halfW = entityWidth / 2f;
		float halfH = entityHeight / 2f;
		
		float clampedX = Math.max(x + halfW, Math.min(pos.x(), x + width - halfW));
		float clampedY = Math.max(y + halfH, Math.min(pos.y(), y + height - halfH));
		
		return new Vector2(clampedX, clampedY);
	}
	
	public Vector2 getCenter() {
		return new Vector2(x + width / 2f, y + height / 2f);
	}
	
	public void render(Graphics2D g2) {
		// fill
		g2.setColor(ColorConfig.ARENA_BACKGROUND);
		g2.fillRect((int)x, (int)y, (int)width, (int)height);
		
		// border
		g2.setColor(ColorConfig.ARENA_BORDER);
		g2.setStroke(new BasicStroke(BORDER_WIDTH));
		g2.drawRect((int)x, (int)y, (int)width, (int)height);
	}

}
