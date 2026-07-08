package com.lobsterchops.brainlessgamejam.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.lobsterchops.brainlessgamejam.config.ScreenConfig;
import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.entities.SlimeChild;
import com.lobsterchops.brainlessgamejam.util.FontLoader;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.ScoreSystem;
import com.lobsterchops.brainlessgamejam.world.WaveManager;
import com.lobsterchops.brainlessgamejam.world.common.GameState;

/**
 * Draws the game HUD in screen space (no camera transform).
 *
 * <h4>Layout</h4>
 * 
 * <pre>
 * ┌──────────────────────────────────────────────────────────────┐  48px
 * │  ● ● ● ● ○ ○ ○ ○      WAVE  3        SCORE  004200         │
 * └──────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * Filled circles = children alive, hollow = children lost this wave.
 *
 * <p>
 * While {@code GameState == WAVE_COMPLETE} a centred banner fades in and then
 * out over the 3-second inter-wave delay.
 * </p>
 *
 * <h4>Wiring</h4> Constructed by {@code RenderPipeline}; resolves
 * {@code ScoreSystem}, {@code WaveManager}, and {@code GameSystem} from
 * {@code ServiceLocator} so that {@code RenderPipeline}'s constructor signature
 * stays unchanged.
 */
public class HudRenderer {

	private static final int BAR_HEIGHT = 48;
	private static final int SHADOW_OFFSET = 2;

	// Child silhouette dots
	private static final int DOT_RADIUS = 7;
	private static final int DOT_GAP = 5;
	private static final int DOT_MARGIN_X = 16;
	private static final int MAX_DISPLAYED_CHILDREN = 20;

	private static final Color LIVES_VALUE = new Color(255, 100, 100);
	private static final Color LIVES_LABEL = new Color(190, 70, 70);
	private static final Color BAR_BG = new Color(10, 10, 16, 210);
	private static final Color BAR_BORDER = new Color(255, 255, 255, 25);
	private static final Color SCORE_VALUE = new Color(255, 235, 100);
	private static final Color SCORE_LABEL = new Color(180, 160, 80);
	private static final Color WAVE_VALUE = new Color(120, 210, 255);
	private static final Color WAVE_LABEL = new Color(80, 150, 190);
	private static final Color TEXT_SHADOW = new Color(0, 0, 0, 200);

	private static final Color DOT_ALIVE = new Color(90, 200, 80);
	private static final Color DOT_ALIVE_GLOW = new Color(140, 255, 120, 80);
	private static final Color DOT_DEAD = new Color(55, 55, 55);
	private static final Color DOT_DEAD_RING = new Color(80, 80, 80);

	private static final Color BANNER_BG = new Color(0, 0, 0, 170);
	private static final Color BANNER_TITLE = new Color(255, 235, 100);
	private static final Color BANNER_PERFECT = new Color(100, 255, 130);
	private static final Color BANNER_DELTA = new Color(220, 220, 220);

	private static final Font FONT_VALUE = FontLoader.load("/fonts/Mojang-Regular.ttf", 22f);
	private static final Font FONT_LABEL = FontLoader.load("/fonts/Mojang-Regular.ttf", 11f);
	private static final Font FONT_BANNER = FontLoader.load("/fonts/Mojang-Regular.ttf", 36f);
	private static final Font FONT_BANNER_SUB = FontLoader.load("/fonts/Mojang-Regular.ttf", 18f);

	public HudRenderer() {
	}

	public void render(Graphics2D g2) {
		GameSystem gameSystem = ServiceLocator.resolve(GameSystem.class);
		ScoreSystem scoreSystem = ServiceLocator.resolve(ScoreSystem.class);
		WaveManager waveManager = ServiceLocator.resolve(WaveManager.class);

		enableAntialiasing(g2);
		renderTopBar(g2, gameSystem, scoreSystem, waveManager);

		if (gameSystem.getState() == GameState.WAVE_COMPLETE) {
			renderInterWaveBanner(g2, scoreSystem, waveManager);
		}
	}

	private void renderTopBar(Graphics2D g2, GameSystem gameSystem, ScoreSystem score, WaveManager wave) {
		drawBarBackground(g2);
		renderChildDots(g2, gameSystem, wave);
		renderWaveIndicator(g2, wave.getCurrentWave());
		renderLivesIndicator(g2, score.getLives());
		renderScoreIndicator(g2, score.getScore());
	}

	private void drawBarBackground(Graphics2D g2) {
		final int W = ScreenConfig.WIDTH;
		g2.setColor(BAR_BG);
		g2.fillRect(0, 0, W, BAR_HEIGHT);
		g2.setColor(BAR_BORDER);
		g2.drawLine(0, BAR_HEIGHT - 1, W, BAR_HEIGHT - 1);
	}

	private void renderWaveIndicator(Graphics2D g2, int waveNumber) {
		int anchorX = ScreenConfig.WIDTH / 2;
		drawLabelValuePair(g2, "WAVE", String.valueOf(waveNumber), WAVE_LABEL, WAVE_VALUE, anchorX, Anchor.CENTRE);
	}

	private void renderLivesIndicator(Graphics2D g2, int lives) {
		int anchorX = ScreenConfig.WIDTH * 3 / 4;
		drawLabelValuePair(g2, "LIVES", String.valueOf(lives), LIVES_LABEL, LIVES_VALUE, anchorX, Anchor.CENTRE);
	}

	private void renderScoreIndicator(Graphics2D g2, int score) {
		int anchorX = ScreenConfig.WIDTH - 16;
		drawLabelValuePair(g2, "SCORE", String.format("%06d", score), SCORE_LABEL, SCORE_VALUE, anchorX, Anchor.RIGHT);
	}

	/**
	 * Draws a "LABEL value" pair horizontally, anchored by {@code Anchor}. CENTRE
	 * anchors the block's midpoint to anchorX; RIGHT anchors its right edge.
	 */
	private void drawLabelValuePair(Graphics2D g2, String label, String value, Color labelColour, Color valueColour,
			int anchorX, Anchor anchor) {
		final int centreY = BAR_HEIGHT / 2;
		final int gap = 6;

		g2.setFont(FONT_LABEL);
		int labelW = g2.getFontMetrics().stringWidth(label);

		g2.setFont(FONT_VALUE);
		FontMetrics valueFm = g2.getFontMetrics();
		int blockW = labelW + gap + valueFm.stringWidth(value);
		int blockX = anchor == Anchor.CENTRE ? anchorX - blockW / 2 : anchorX - blockW;
		int baseline = centreY + valueFm.getAscent() / 2 - 2;

		drawShadowedString(g2, FONT_LABEL, labelColour, label, blockX, baseline);
		drawShadowedString(g2, FONT_VALUE, valueColour, value, blockX + labelW + gap, baseline);
	}

	private enum Anchor {
		CENTRE, RIGHT
	}

	private void renderChildDots(Graphics2D g2, GameSystem gameSystem, WaveManager wave) {
		int totalSlots = Math.min(wave.getWaveStartChildCount(), MAX_DISPLAYED_CHILDREN);
		int aliveSlots = Math.min(countLivingChildren(gameSystem), totalSlots);

		int startX = DOT_MARGIN_X + DOT_RADIUS;
		int centreY = BAR_HEIGHT / 2;

		for (int i = 0; i < totalSlots; i++) {
			int cx = startX + i * (DOT_RADIUS * 2 + DOT_GAP);
			if (i < aliveSlots) {
				drawAliveDot(g2, cx, centreY);
			} else {
				drawDeadDot(g2, cx, centreY);
			}
		}
	}

	private int countLivingChildren(GameSystem gameSystem) {
		return (int) gameSystem.getObjects().stream().filter(o -> o instanceof SlimeChild && o.isActive()).count();
	}

	private void drawAliveDot(Graphics2D g2, int cx, int cy) {
		int d = DOT_RADIUS * 2;
		g2.setColor(DOT_ALIVE_GLOW);
		g2.fillOval(cx - DOT_RADIUS - 2, cy - DOT_RADIUS - 2, d + 4, d + 4);
		g2.setColor(DOT_ALIVE);
		g2.fillOval(cx - DOT_RADIUS, cy - DOT_RADIUS, d, d);
	}

	private void drawDeadDot(Graphics2D g2, int cx, int cy) {
		int d = DOT_RADIUS * 2;
		g2.setColor(DOT_DEAD);
		g2.fillOval(cx - DOT_RADIUS, cy - DOT_RADIUS, d, d);
		g2.setColor(DOT_DEAD_RING);
		g2.setStroke(new BasicStroke(1.5f));
		g2.drawOval(cx - DOT_RADIUS, cy - DOT_RADIUS, d, d);
	}

	private void renderInterWaveBanner(Graphics2D g2, ScoreSystem score, WaveManager wave) {
		float alpha = computeBannerAlpha(wave.getInterWaveRemainingMs(), WaveManager.INTER_WAVE_DELAY_MS);
		if (alpha <= 0f)
			return;

		Composite saved = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		drawBannerBackdrop(g2);
		int titleY = drawBannerTitle(g2);
		drawBannerDelta(g2, score, titleY);

		g2.setComposite(saved);
	}

	private void drawBannerBackdrop(Graphics2D g2) {
		final int bH = 130;
		final int bY = ScreenConfig.HEIGHT / 2 - bH / 2;
		g2.setColor(BANNER_BG);
		g2.fillRect(0, bY, ScreenConfig.WIDTH, bH);
	}

	/**
	 * Draws the "WAVE COMPLETE!" title and returns the baseline Y for the sub-line.
	 */
	private int drawBannerTitle(Graphics2D g2) {
		final int bY = ScreenConfig.HEIGHT / 2 - 130 / 2;
		String title = "WAVE COMPLETE!";

		g2.setFont(FONT_BANNER);
		FontMetrics fm = g2.getFontMetrics();
		int titleX = ScreenConfig.WIDTH / 2 - fm.stringWidth(title) / 2;
		int titleY = bY + 52;

		g2.setColor(TEXT_SHADOW);
		g2.drawString(title, titleX + SHADOW_OFFSET, titleY + SHADOW_OFFSET);
		g2.setColor(BANNER_TITLE);
		g2.drawString(title, titleX, titleY);

		return titleY + fm.getHeight() - 4;
	}

	private void drawBannerDelta(Graphics2D g2, ScoreSystem score, int subY) {
		int delta = score.getLastCrossingDelta();
		boolean perfect = score.wasLastCrossingPerfect();
		String deltaText = (delta >= 0 ? "+" : "") + delta + " pts" + (perfect ? " !" : "");

		g2.setFont(FONT_BANNER_SUB);
		FontMetrics fm = g2.getFontMetrics();
		int subX = ScreenConfig.WIDTH / 2 - fm.stringWidth(deltaText) / 2;

		g2.setColor(TEXT_SHADOW);
		g2.drawString(deltaText, subX + 1, subY + 1);
		g2.setColor(perfect ? BANNER_PERFECT : BANNER_DELTA);
		g2.drawString(deltaText, subX, subY);
	}

	/**
	 * Fade in over first 300 ms, hold, fade out over last 600 ms.
	 */
	private float computeBannerAlpha(long remainingMs, long totalMs) {
		long elapsedMs = totalMs - remainingMs;
		if (elapsedMs < 300L)
			return (float) elapsedMs / 300f;
		if (remainingMs < 600L)
			return (float) remainingMs / 600f;
		return 1f;
	}

	private void drawShadowedString(Graphics2D g2, Font font, Color colour, String text, int x, int y) {
		g2.setFont(font);
		g2.setColor(TEXT_SHADOW);
		g2.drawString(text, x + SHADOW_OFFSET, y + SHADOW_OFFSET);
		g2.setColor(colour);
		g2.drawString(text, x, y);
	}

	private void enableAntialiasing(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
}