package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.config.ScreenConfig;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
 
/**
 * <h4>Placeholder main menu scene.</h4>
 * <p>Intentionally minimal — just enough to prove SceneManager can switch
 * between scenes. Expand this once real menu requirements (title, prompt,
 * input handling to start a run) are decided.</p>
 */
public class MenuScene implements Scene {
 
	@Override
	public void update(UpdateContext context) {
		// No menu logic yet.
	}
 
	@Override
	public void render(Graphics2D g2) {
		g2.setColor(ColorConfig.DARK_GREY);
		g2.fillRect(0, 0, ScreenConfig.WIDTH, ScreenConfig.HEIGHT);
	}
 
}