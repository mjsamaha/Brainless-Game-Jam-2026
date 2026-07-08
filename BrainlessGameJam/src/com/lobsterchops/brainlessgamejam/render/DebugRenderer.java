package com.lobsterchops.brainlessgamejam.render;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import com.lobsterchops.brainlessgamejam.Version;
import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.util.FontLoader;
import com.lobsterchops.brainlessgamejam.world.GameSystem;

public class DebugRenderer {

	private static final Font DEBUG_FONT = FontLoader.load("/fonts/Mojang-Regular.ttf", 16f);

	private static final int SHADOW_OFFSET = 1;
	private static final int PADDING_X = 4;
	private static final int PADDING_Y = 4;
	private static final int START_X_LEFT = 2;
	private static final int START_X_RIGHT = 650;
	private static final int START_Y = 12;
	private static final int LINE_HEIGHT = 16;

	private record DebugLine(String label, Object value) {
		static final DebugLine BLANK = new DebugLine(null, null);

		boolean isBlank() {
			return label == null && value == null;
		}
	}

	public void render(Graphics2D g2, GameSystem gameSystem, DebugMetrics metrics) {
		g2.setFont(DEBUG_FONT);
		renderColumn(g2, buildLeftLines(metrics), START_X_LEFT, START_Y);
		renderColumn(g2, buildRightLines(gameSystem), START_X_RIGHT, START_Y);
	}

	private List<DebugLine> buildLeftLines(DebugMetrics metrics) {
		return List.of(new DebugLine(null, Version.getDebugTitle()), DebugLine.BLANK,
				new DebugLine("fps", String.format("%3d", metrics.getFps())),
				new DebugLine("stage", Version.GAME_STAGE_ENUM.getDisplayName()), DebugLine.BLANK,
				new DebugLine(null, "Developed by LobsterChops"), new DebugLine(null, "Brainless Game Jam 2026"),
				new DebugLine(null, "Java & AWT/Swing"));
	}

	private List<DebugLine> buildRightLines(GameSystem gameSystem) {
		return List.of(buildSystemLines(), buildMemoryLines(), buildGameLines(gameSystem)).stream()
				.flatMap(List::stream).toList();
	}

	private List<DebugLine> buildSystemLines() {
		return List.of(new DebugLine(null, System.getProperty("os.name") + " " + System.getProperty("os.version")),
				new DebugLine(null, System.getProperty("os.arch")),
				new DebugLine(null, "Java " + System.getProperty("java.version")), DebugLine.BLANK,
				new DebugLine("cpu", System.getProperty("os.arch")),
				new DebugLine("cores", Runtime.getRuntime().availableProcessors()), DebugLine.BLANK,
				new DebugLine("display", getDisplayInfo()), DebugLine.BLANK);
	}

	private List<DebugLine> buildMemoryLines() {
		Runtime rt = Runtime.getRuntime();
		long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
		long totalMb = rt.totalMemory() / (1024 * 1024);
		long maxMb = rt.maxMemory() / (1024 * 1024);
		return List.of(new DebugLine("mem used", usedMb + " MB"), new DebugLine("mem total", totalMb + " MB"),
				new DebugLine("mem max", maxMb + " MB"), DebugLine.BLANK);
	}

	private List<DebugLine> buildGameLines(GameSystem gameSystem) {
		return List.of(new DebugLine("state", gameSystem.getState()), new DebugLine("tick", gameSystem.getTick()),
				new DebugLine("time", formatElapsed(gameSystem.getElapsedMillis())),
				new DebugLine("objects", gameSystem.getObjects().size()));
	}

	private void renderColumn(Graphics2D g2, List<DebugLine> lines, int startX, int startY) {
		int y = startY;
		for (DebugLine line : lines) {
			if (line.isBlank()) {
				y += LINE_HEIGHT;
				continue;
			}
			renderLine(g2, line, startX, y);
			y += LINE_HEIGHT;
		}
	}

	private void renderLine(Graphics2D g2, DebugLine line, int x, int y) {
		String text = formatLineText(line);
		drawBackground(g2, x, y, g2.getFontMetrics().stringWidth(text), g2.getFontMetrics().getHeight());

		int textX = x + PADDING_X;
		int textY = y + g2.getFontMetrics().getHeight() - PADDING_Y;

		if (line.label() != null) {
			renderLabeledLine(g2, line, textX, textY);
		} else {
			renderPlainLine(g2, text, textX, textY);
		}
	}

	private void renderLabeledLine(Graphics2D g2, DebugLine line, int textX, int textY) {
		String label = line.label() + ": ";
		String value = String.valueOf(line.value());
		int labelWidth = g2.getFontMetrics().stringWidth(label);

		// Shadow
		g2.setColor(ColorConfig.DEBUG_SHADOW);
		g2.drawString(label, textX + SHADOW_OFFSET, textY + SHADOW_OFFSET);
		g2.drawString(value, textX + labelWidth + SHADOW_OFFSET, textY + SHADOW_OFFSET);

		// Label
		g2.setColor(ColorConfig.DEBUG_LABEL);
		g2.drawString(label, textX, textY);

		// Value
		g2.setColor(ColorConfig.DEBUG_TEXT);
		g2.drawString(value, textX + labelWidth, textY);
	}

	private void renderPlainLine(Graphics2D g2, String text, int textX, int textY) {
		g2.setColor(ColorConfig.DEBUG_SHADOW);
		g2.drawString(text, textX + SHADOW_OFFSET, textY + SHADOW_OFFSET);

		g2.setColor(ColorConfig.DEBUG_TEXT);
		g2.drawString(text, textX, textY);
	}

	private void drawBackground(Graphics2D g2, int x, int y, int textWidth, int textHeight) {
		g2.setColor(ColorConfig.DEBUG_BG);
		g2.fillRect(x, y, textWidth + PADDING_X * 2, textHeight + PADDING_Y);
	}

	private String formatLineText(DebugLine line) {
		return line.label() == null ? String.valueOf(line.value()) : line.label() + ": " + line.value();
	}

	private String getDisplayInfo() {
		java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
		java.awt.DisplayMode dm = ge.getDefaultScreenDevice().getDisplayMode();
		return dm.getWidth() + "x" + dm.getHeight() + " @ " + dm.getRefreshRate() + "hz";
	}

	private String formatElapsed(long millis) {
		long seconds = (millis / 1000) % 60;
		long minutes = (millis / (1000 * 60)) % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}

}