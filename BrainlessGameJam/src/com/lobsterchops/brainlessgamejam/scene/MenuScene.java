package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.config.ScreenConfig;
import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.input.Command;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.util.FontLoader;
import com.lobsterchops.brainlessgamejam.world.ScoreSystem;

/**
 * <h4>Placeholder main menu scene.</h4>
 * <p>
 * Intentionally minimal — just enough to prove SceneManager can switch between
 * scenes. Expand this once real menu requirements (title, prompt, input
 * handling to start a run) are decided.
 * </p>
 */

public class MenuScene implements Scene {

	private static final Font FONT_TITLE = FontLoader.load("/fonts/Mojang-Regular.ttf", 48f);
	private static final Font FONT_PROMPT = FontLoader.load("/fonts/Mojang-Regular.ttf", 22f);
	private static final Font FONT_LABEL = FontLoader.load("/fonts/Mojang-Regular.ttf", 14f);

	private static final Color TEXT_SHADOW = new Color(0, 0, 0, 200);

	private static final int SHADOW_OFFSET = 2;

	private boolean drainedOnFirstUpdate = false;

	private final SceneManager sceneManager;
	private final Scene playingScene;
	private final Runnable startCallback;

	private long tick = 0;

	public MenuScene(SceneManager sceneManager, Scene playingScene, Runnable startCallback) {
		this.sceneManager = sceneManager;
		this.playingScene = playingScene;
		this.startCallback = startCallback;
	}

	@Override
	public void enter() {
		tick = 0;
		drainedOnFirstUpdate = false;

	}

	@Override
	public void update(UpdateContext context) {
		// Drain any leftover confirm press on the very first update tick
		if (!drainedOnFirstUpdate) {
			drainedOnFirstUpdate = true;
			ServiceLocator.resolve(InputManager.class).wasConfirmPressed();
			return;
		}

		tick++;
		InputManager input = ServiceLocator.resolve(InputManager.class);
		if (input.wasConfirmPressed()) {
			startCallback.run();
			sceneManager.switchTo(playingScene);
		}
	}

	@Override
	public void render(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		final int W = ScreenConfig.WIDTH;
		final int H = ScreenConfig.HEIGHT;

		// Background
		g2.setColor(ColorConfig.DARK_GREY);
		g2.fillRect(0, 0, W, H);

		String title = "SLIME  DUCKLINGS";
		drawCentred(g2, FONT_TITLE, ColorConfig.HUD_BANNER_TITLE, title, W, H / 2 - 80);

		// Sub-title
		String sub = "BRAINLESS   GAME   JAM   2026";
		drawCentred(g2, FONT_PROMPT, ColorConfig.HUD_WAVE_VALUE, sub, W, H / 2 - 20);

		String credits = "Music  by  Trevor  Lentz";
		drawCentred(g2, FONT_LABEL, ColorConfig.HUD_SCORE_LABEL, credits, W, H / 2 + 20);

		// Pulsing "PRESS ENTER TO START"
		float alpha = 0.5f + 0.5f * (float) Math.sin(tick * 0.05f);
		drawCentredAlpha(g2, FONT_PROMPT, ColorConfig.HUD_SCORE_VALUE, "PRESS  ENTER  TO  START", W, H / 2 + 60, alpha);

		// High score at bottom
		ScoreSystem scoreSystem = ServiceLocator.resolve(ScoreSystem.class);
		int highScore = scoreSystem.getHighScore();
		if (highScore > 0) {
			String best = String.format("BEST  %06d", highScore);
			drawCentred(g2, FONT_LABEL, ColorConfig.HUD_SCORE_LABEL, best, W, H - 48);
		}

		// Debug hint
		String debug = "F3  -  Debug  Overlay";
		drawCentred(g2, FONT_LABEL, ColorConfig.HUD_WAVE_LABEL, debug, W, H - 20);

	}

	private void drawCentred(Graphics2D g2, Font font, Color colour, String text, int screenW, int y) {
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics();
		int x = screenW / 2 - fm.stringWidth(text) / 2;

		g2.setColor(TEXT_SHADOW);
		g2.drawString(text, x + SHADOW_OFFSET, y + SHADOW_OFFSET);
		g2.setColor(colour);
		g2.drawString(text, x, y);
	}

	private void drawCentredAlpha(Graphics2D g2, Font font, Color colour, String text, int screenW, int y,
			float alpha) {
		Color withAlpha = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), (int) (alpha * 255));
		drawCentred(g2, font, withAlpha, text, screenW, y);
	}
}