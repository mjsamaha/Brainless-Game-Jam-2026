package com.lobsterchops.brainlessgamejam.render;

import java.awt.Color;
import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.entity.PlayerEntity;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.world.TrailSystem;

public class TrailRenderer {
	
	private final TrailSystem trailSystem;
	
	public TrailRenderer(TrailSystem trailSystem) {
		this.trailSystem = trailSystem;
	}
	
	public void render(Graphics2D g2) {
		int count = trailSystem.getSegmentCount();
		if (count == 0) return;
		
		int size = (int) PlayerEntity.SIZE;
		
		for (int i = 0; i < count; i++) {
			Vector2 pos = trailSystem.getSegmentPosition(i);
			if (pos == null) continue;
			
			Color color = trailSystem.getSegmentColor(i);
			
			int x = Math.round(pos.x() - size / 2f);
			int y = Math.round(pos.y() - size / 2f);
			
			g2.setColor(color);
			g2.fillRect(x, y, size, size);
		}
	}

}
