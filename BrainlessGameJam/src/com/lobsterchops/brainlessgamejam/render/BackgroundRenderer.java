package com.lobsterchops.brainlessgamejam.render;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.config.ScreenConfig;

public class BackgroundRenderer {
	
	public void render(Graphics2D g2) {
		g2.setColor(ColorConfig.BLACK);
		g2.fillRect(0, 0, ScreenConfig.WIDTH, ScreenConfig.HEIGHT);
	}

}
