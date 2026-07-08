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
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.event.GameOverEvent;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.util.FontLoader;
import com.lobsterchops.brainlessgamejam.world.ScoreSystem;

public class GameOverScene implements Scene {

	private boolean active = false;
	private boolean drainedOnFirstUpdate = false;

	private static final Font FONT_TITLE = FontLoader.load("/fonts/Mojang-Regular.ttf", 48f);
	private static final Font FONT_VALUE = FontLoader.load("/fonts/Mojang-Regular.ttf", 22f);
	private static final Font FONT_LABEL = FontLoader.load("/fonts/Mojang-Regular.ttf", 14f);

	private static final Color OVERLAY = new Color(0, 0, 0, 180);
	private static final Color TEXT_SHADOW = new Color(0, 0, 0, 200);

	private static final int SHADOW_OFFSET = 2;

	private final SceneManager sceneManager;
	private final Scene menuScene;
	private final Runnable restartCallback;

	// Populated when GameOverEvent is received
	private int finalScore = 0;
	private int waveReached = 1;

	private long tick = 0;

	public GameOverScene(SceneManager sceneManager, Scene menuScene, Runnable restartCallback, EventBus eventBus) {
		this.sceneManager = sceneManager;
		this.menuScene = menuScene;
		this.restartCallback = restartCallback;

		eventBus.subscribe(GameOverEvent.class, this::onGameOver);
	}

	@Override
	public void enter() {
		tick = 0;
		drainedOnFirstUpdate = false;
	}

	@Override
	public void update(UpdateContext context) {
		if (!active)
			return;

		if (!drainedOnFirstUpdate) {
			drainedOnFirstUpdate = true;
			ServiceLocator.resolve(InputManager.class).wasConfirmPressed();
			return;
		}

		tick++;
		InputManager input = ServiceLocator.resolve(InputManager.class);
		if (input.wasConfirmPressed()) {
			active = false;
			restartCallback.run();
			sceneManager.switchTo(menuScene);
		}
	}

	@Override
	public void render(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		final int W = ScreenConfig.WIDTH;
		final int H = ScreenConfig.HEIGHT;

		// Dark full-screen overlay
		g2.setColor(OVERLAY);
		g2.fillRect(0, 0, W, H);

		int centreY = H / 2;

		// "GAME OVER" title
		drawCentred(g2, FONT_TITLE, ColorConfig.HUD_BANNER_TITLE, "GAME OVER", W, centreY - 90);

		// Score
		drawCentred(g2, FONT_LABEL, ColorConfig.HUD_SCORE_LABEL, "SCORE", W, centreY - 20);
		drawCentred(g2, FONT_VALUE, ColorConfig.HUD_SCORE_VALUE, String.format("%06d", finalScore), W, centreY + 10);

		// Wave reached
		drawCentred(g2, FONT_LABEL, ColorConfig.HUD_WAVE_LABEL, "WAVE REACHED", W, centreY + 46);
		drawCentred(g2, FONT_VALUE, ColorConfig.HUD_WAVE_VALUE, String.valueOf(waveReached), W, centreY + 72);

		// High score — highlight if just beaten
		ScoreSystem scoreSystem = ServiceLocator.resolve(ScoreSystem.class);
		int highScore = scoreSystem.getHighScore();
		boolean newBest = finalScore >= highScore && highScore > 0;

		String bestLabel = newBest ? "NEW BEST!" : String.format("BEST  %06d", highScore);
		Color bestColor = newBest ? ColorConfig.HUD_BANNER_PERFECT : ColorConfig.HUD_SCORE_LABEL;
		drawCentred(g2, FONT_LABEL, bestColor, bestLabel, W, centreY + 110);

		// Pulsing restart prompt
		float alpha = 0.5f + 0.5f * (float) Math.sin(tick * 0.05f);
		drawCentredAlpha(g2, FONT_VALUE, ColorConfig.HUD_SCORE_VALUE, "PRESS  ENTER  TO  RESTART", W, centreY + 160,
				alpha);
	}

	private void onGameOver(GameOverEvent event) {
		if (active)
			return;
		finalScore = event.finalScore();
		waveReached = event.waveReached();
		active = true;
		sceneManager.switchTo(this);
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
