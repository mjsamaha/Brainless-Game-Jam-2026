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
import com.lobsterchops.brainlessgamejam.entity.SlimeChild;
import com.lobsterchops.brainlessgamejam.entity.SlimeParent;
import com.lobsterchops.brainlessgamejam.state.GameState;
import com.lobsterchops.brainlessgamejam.util.FontLoader;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.ScoreSystem;
import com.lobsterchops.brainlessgamejam.world.WaveManager;

/**
 * Draws the game HUD in screen space (no camera transform).
 *
 * <h4>Layout</h4>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────┐  48px
 * │  ● ● ● ● ○ ○ ○ ○      WAVE  3        SCORE  004200         │
 * └──────────────────────────────────────────────────────────────┘
 * </pre>
 * Filled circles = children alive, hollow = children lost this wave.
 *
 * <p>While {@code GameState == WAVE_COMPLETE} a centred banner fades in
 * and then out over the 3-second inter-wave delay.</p>
 *
 * <h4>Wiring</h4>
 * Constructed by {@code RenderPipeline}; resolves {@code ScoreSystem},
 * {@code WaveManager}, and {@code GameSystem} from {@code ServiceLocator}
 * so that {@code RenderPipeline}'s constructor signature stays unchanged.
 */
public class HudRenderer {
 
    private static final int BAR_HEIGHT   = 48;
    private static final int SHADOW_OFFSET = 2;
 
    // Child silhouette dots
    private static final int DOT_RADIUS          = 7;
    private static final int DOT_GAP             = 5;
    private static final int DOT_MARGIN_X        = 16;
    private static final int MAX_DISPLAYED_CHILDREN = 20;
 
    private static final Color BAR_BG         = new Color(10,  10,  16,  210);
    private static final Color BAR_BORDER      = new Color(255, 255, 255, 25);
 
    private static final Color SCORE_VALUE     = new Color(255, 235, 100);  // warm gold
    private static final Color SCORE_LABEL     = new Color(180, 160,  80);  // muted gold
    private static final Color WAVE_VALUE      = new Color(120, 210, 255);  // cool blue
    private static final Color WAVE_LABEL      = new Color( 80, 150, 190);
    private static final Color TEXT_SHADOW     = new Color(  0,   0,   0, 200);
 
    private static final Color DOT_ALIVE       = new Color( 90, 200,  80);  // slime green
    private static final Color DOT_ALIVE_GLOW  = new Color(140, 255, 120,  80);
    private static final Color DOT_DEAD        = new Color( 55,  55,  55);
    private static final Color DOT_DEAD_RING   = new Color( 80,  80,  80);
 
    private static final Color BANNER_BG       = new Color(  0,   0,   0, 170);
    private static final Color BANNER_TITLE    = new Color(255, 235, 100);
    private static final Color BANNER_PERFECT  = new Color(100, 255, 130);
    private static final Color BANNER_DELTA    = new Color(220, 220, 220);

 
    private static final Font FONT_VALUE      = FontLoader.load("/fonts/Mojang-Regular.ttf", 22f);
    private static final Font FONT_LABEL      = FontLoader.load("/fonts/Mojang-Regular.ttf", 11f);
    private static final Font FONT_BANNER     = FontLoader.load("/fonts/Mojang-Regular.ttf", 36f);
    private static final Font FONT_BANNER_SUB = FontLoader.load("/fonts/Mojang-Regular.ttf", 18f);

 
    public HudRenderer() {
        // All dependencies resolved lazily from ServiceLocator in render()
        // so this can be constructed before those systems are registered.
    }
 
    public void render(Graphics2D g2) {
        GameSystem  gameSystem  = ServiceLocator.resolve(GameSystem.class);
        ScoreSystem scoreSystem = ServiceLocator.resolve(ScoreSystem.class);
        WaveManager waveManager = ServiceLocator.resolve(WaveManager.class);
 
        enableAntialiasing(g2);
        renderTopBar(g2, gameSystem, scoreSystem, waveManager);
 
        if (gameSystem.getState() == GameState.WAVE_COMPLETE) {
            renderInterWaveBanner(g2, scoreSystem, waveManager);
        }
    }
 
    private void renderTopBar(Graphics2D g2, GameSystem gameSystem,
                              ScoreSystem score, WaveManager wave) {
        final int W = ScreenConfig.WIDTH;
 
        // Background strip
        g2.setColor(BAR_BG);
        g2.fillRect(0, 0, W, BAR_HEIGHT);
 
        // 1-px bottom border line
        g2.setColor(BAR_BORDER);
        g2.drawLine(0, BAR_HEIGHT - 1, W, BAR_HEIGHT - 1);
 
        renderChildDots(g2, gameSystem, wave);
        renderWaveIndicator(g2, wave.getCurrentWave());
        renderScoreIndicator(g2, score.getScore());
    }
 
    private void renderChildDots(Graphics2D g2, GameSystem gameSystem, WaveManager wave) {
        // Count living SlimeChild instances directly from the object list.
        int childrenAlive = (int) gameSystem.getObjects().stream()
                .filter(o -> o instanceof SlimeChild && o.isActive())
                .count();
 
        // Total slots = how many children started this wave (from WaveManager),
        // capped at MAX_DISPLAYED_CHILDREN so the bar never overflows.
        int totalSlots = Math.min(wave.getWaveStartChildCount(), MAX_DISPLAYED_CHILDREN);
        int aliveSlots = Math.min(childrenAlive, totalSlots);
 
        int dotDiameter = DOT_RADIUS * 2;
        int startX = DOT_MARGIN_X + DOT_RADIUS;
        int centreY = BAR_HEIGHT / 2;
 
        for (int i = 0; i < totalSlots; i++) {
            boolean alive = i < aliveSlots;
            int cx = startX + i * (dotDiameter + DOT_GAP);
 
            if (alive) {
                // Soft glow halo
                g2.setColor(DOT_ALIVE_GLOW);
                g2.fillOval(cx - DOT_RADIUS - 2, centreY - DOT_RADIUS - 2,
                        dotDiameter + 4, dotDiameter + 4);
                // Filled dot
                g2.setColor(DOT_ALIVE);
                g2.fillOval(cx - DOT_RADIUS, centreY - DOT_RADIUS,
                        dotDiameter, dotDiameter);
            } else {
                // Dark fill + hollow ring
                g2.setColor(DOT_DEAD);
                g2.fillOval(cx - DOT_RADIUS, centreY - DOT_RADIUS,
                        dotDiameter, dotDiameter);
                g2.setColor(DOT_DEAD_RING);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(cx - DOT_RADIUS, centreY - DOT_RADIUS,
                        dotDiameter, dotDiameter);
            }
        }
    }
 
    private void renderWaveIndicator(Graphics2D g2, int waveNumber) {
        final int centreX = ScreenConfig.WIDTH / 2;
        final int centreY = BAR_HEIGHT / 2;
 
        String label = "WAVE";
        String value = String.valueOf(waveNumber);
 
        g2.setFont(FONT_LABEL);
        int labelW = g2.getFontMetrics().stringWidth(label);
 
        g2.setFont(FONT_VALUE);
        FontMetrics valueFm = g2.getFontMetrics();
        int valueW = valueFm.stringWidth(value);
 
        int gap    = 6;
        int blockW = labelW + gap + valueW;
        int blockX = centreX - blockW / 2;
 
        // Baseline: vertically centred in the bar
        int baseline = centreY + valueFm.getAscent() / 2 - 2;
 
        drawShadowedString(g2, FONT_LABEL, WAVE_LABEL,  label,              blockX,             baseline);
        drawShadowedString(g2, FONT_VALUE, WAVE_VALUE,  value,              blockX + labelW + gap, baseline);
    }
 
    private void renderScoreIndicator(Graphics2D g2, int score) {
        final int rightEdge = ScreenConfig.WIDTH - 16;
        final int centreY   = BAR_HEIGHT / 2;
 
        String label = "SCORE";
        String value = String.format("%06d", score);
 
        g2.setFont(FONT_LABEL);
        int labelW = g2.getFontMetrics().stringWidth(label);
 
        g2.setFont(FONT_VALUE);
        FontMetrics valueFm = g2.getFontMetrics();
        int valueW = valueFm.stringWidth(value);
 
        int gap    = 6;
        int blockW = labelW + gap + valueW;
        int blockX = rightEdge - blockW;
 
        int baseline = centreY + valueFm.getAscent() / 2 - 2;
 
        drawShadowedString(g2, FONT_LABEL, SCORE_LABEL, label,              blockX,             baseline);
        drawShadowedString(g2, FONT_VALUE, SCORE_VALUE, value,              blockX + labelW + gap, baseline);
    }

 
    private void renderInterWaveBanner(Graphics2D g2,
                                       ScoreSystem score, WaveManager wave) {
        long remainingMs = wave.getInterWaveRemainingMs();
        float alpha = computeBannerAlpha(remainingMs, WaveManager.INTER_WAVE_DELAY_MS);
        if (alpha <= 0f) return;
 
        Composite saved = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
 
        final int W  = ScreenConfig.WIDTH;
        final int H  = ScreenConfig.HEIGHT;
        final int bH = 130;
        final int bY = H / 2 - bH / 2;
 
        // Dark backdrop strip
        g2.setColor(BANNER_BG);
        g2.fillRect(0, bY, W, bH);
 
        // "WAVE COMPLETE!" title
        String title = "WAVE COMPLETE!";
        g2.setFont(FONT_BANNER);
        FontMetrics titleFm = g2.getFontMetrics();
        int titleX = W / 2 - titleFm.stringWidth(title) / 2;
        int titleY = bY + 52;
 
        g2.setColor(TEXT_SHADOW);
        g2.drawString(title, titleX + SHADOW_OFFSET, titleY + SHADOW_OFFSET);
        g2.setColor(BANNER_TITLE);
        g2.drawString(title, titleX, titleY);
 
        // Score delta sub-line
        int    delta     = score.getLastCrossingDelta();
        String deltaText = (delta >= 0 ? "+" : "") + delta + " pts";
        if (score.wasLastCrossingPerfect()) deltaText += " !";
        //         if (score.wasLastCrossingPerfect()) deltaText += "  \u2605 Perfect!";

 
        g2.setFont(FONT_BANNER_SUB);
        FontMetrics subFm = g2.getFontMetrics();
        int subX = W / 2 - subFm.stringWidth(deltaText) / 2;
        int subY = titleY + titleFm.getHeight() - 4;
 
        g2.setColor(TEXT_SHADOW);
        g2.drawString(deltaText, subX + 1, subY + 1);
        g2.setColor(score.wasLastCrossingPerfect() ? BANNER_PERFECT : BANNER_DELTA);
        g2.drawString(deltaText, subX, subY);
 
        g2.setComposite(saved);
    }
 
    /**
     * Fade in over first 300 ms, hold, fade out over last 600 ms.
     */
    private float computeBannerAlpha(long remainingMs, long totalMs) {
        long elapsedMs = totalMs - remainingMs;
        if (elapsedMs < 300L)    return (float) elapsedMs / 300f;
        if (remainingMs < 600L)  return (float) remainingMs / 600f;
        return 1f;
    }
 
    private void drawShadowedString(Graphics2D g2, Font font, Color colour,
                                    String text, int x, int y) {
        g2.setFont(font);
        g2.setColor(TEXT_SHADOW);
        g2.drawString(text, x + SHADOW_OFFSET, y + SHADOW_OFFSET);
        g2.setColor(colour);
        g2.drawString(text, x, y);
    }
 
    private void enableAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
    }
}