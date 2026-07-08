package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Color;
import java.awt.Graphics2D;
 
import com.lobsterchops.brainlessgamejam.audio.AudioService;
import com.lobsterchops.brainlessgamejam.config.ScreenConfig;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
 
/**
 * <h4>Pause overlay scene.</h4>
 * <p>Darkens the screen while paused. Audio is paused on enter and
 * resumed on exit. Expand with a menu or prompt as needed.</p>
 */
public class PausedScene implements Scene {
 
	private static final Color OVERLAY = new Color(0, 0, 0, 150);
 
	private final AudioService audioService;
	private final SceneManager sceneManager;
	private final Scene playingScene;
 
	public PausedScene(AudioService audioService, SceneManager sceneManager, Scene playingScene) {
		this.audioService = audioService;
		this.sceneManager = sceneManager;
		this.playingScene = playingScene;
	}
 
	@Override
	public void update(UpdateContext context) {
		// Nothing ticks while paused.
	}
 
	@Override
	public void render(Graphics2D g2) {
		// Draw the frozen gameplay underneath, then dim it.
		playingScene.render(g2);
 
		g2.setColor(OVERLAY);
		g2.fillRect(0, 0, ScreenConfig.WIDTH, ScreenConfig.HEIGHT);
 
		// Placeholder — add a "PAUSED" label or menu here when ready.
	}
 
	public void unpause() {
		sceneManager.switchTo(playingScene);
	}
 
}