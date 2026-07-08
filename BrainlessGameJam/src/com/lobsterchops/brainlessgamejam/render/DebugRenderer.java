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

	public void render(Graphics2D g2, GameSystem gameSystem, DebugMetrics metrics) {
		g2.setFont(DEBUG_FONT);

		List<DebugLine> leftLines = buildLeftLines(metrics);
		List<DebugLine> rightLines = buildRightLines(gameSystem);

		renderColumn(g2, leftLines, START_X_LEFT, START_Y);
		renderColumn(g2, rightLines, START_X_RIGHT, START_Y);
	}

	private List<DebugLine> buildLeftLines(DebugMetrics metrics) {
		return List.of(new DebugLine(null, Version.getDebugTitle()),

				DebugLine.BLANK, new DebugLine("fps", String.format("%3d", metrics.getFps())),

				// draw game stage
				new DebugLine("stage", Version.GAME_STAGE_ENUM.getDisplayName()),

				DebugLine.BLANK, new DebugLine(null, "Developed by LobsterChops"),
				new DebugLine(null, "Brainless Game Jam 2026"), new DebugLine(null, "Java & AWT/Swing"));
	}

	private List<DebugLine> buildRightLines(GameSystem gameSystem) {
		Runtime rt = Runtime.getRuntime();
		long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
		long totalMb = rt.totalMemory() / (1024 * 1024);
		long maxMb = rt.maxMemory() / (1024 * 1024);

		return List.of(
				// OS & Java
				new DebugLine(null, System.getProperty("os.name") + " " + System.getProperty("os.version")),
				new DebugLine(null, System.getProperty("os.arch")),
				new DebugLine(null, "Java " + System.getProperty("java.version")), DebugLine.BLANK,

				// CPU
				new DebugLine("cpu", System.getProperty("os.arch")), new DebugLine("cores", rt.availableProcessors()),
				DebugLine.BLANK,

				// Memory
				new DebugLine("mem used", usedMb + " MB"), new DebugLine("mem total", totalMb + " MB"),
				new DebugLine("mem max", maxMb + " MB"), DebugLine.BLANK,

				// Display
				new DebugLine("display", getDisplayInfo()), DebugLine.BLANK,

				// Game
				new DebugLine("state", gameSystem.getState()), new DebugLine("tick", gameSystem.getTick()),
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

			String text = line.label() == null ? String.valueOf(line.value()) : line.label() + ": " + line.value();

			int textWidth = g2.getFontMetrics().stringWidth(text);
			int textHeight = g2.getFontMetrics().getHeight();

			// Background pill
			g2.setColor(ColorConfig.DEBUG_BG);
			g2.fillRect(startX, y, textWidth + PADDING_X * 2, textHeight + PADDING_Y);

			int textX = startX + PADDING_X;
			int textY = y + textHeight - PADDING_Y;

			if (line.label() != null) {
				String label = line.label() + ": ";
				int labelWidth = g2.getFontMetrics().stringWidth(label);

				// Shadow
				g2.setColor(ColorConfig.DEBUG_SHADOW);
				g2.drawString(label, textX + SHADOW_OFFSET, textY + SHADOW_OFFSET);
				g2.drawString(String.valueOf(line.value()), textX + labelWidth + SHADOW_OFFSET, textY + SHADOW_OFFSET);

				// Label
				g2.setColor(ColorConfig.DEBUG_LABEL);
				g2.drawString(label, textX, textY);

				// Value
				g2.setColor(ColorConfig.DEBUG_TEXT);
				g2.drawString(String.valueOf(line.value()), textX + labelWidth, textY);
			} else {
				// Shadow
				g2.setColor(ColorConfig.DEBUG_SHADOW);
				g2.drawString(text, textX + SHADOW_OFFSET, textY + SHADOW_OFFSET);

				// Text
				g2.setColor(ColorConfig.DEBUG_TEXT);
				g2.drawString(text, textX, textY);
			}

			y += LINE_HEIGHT;
		}
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

	private record DebugLine(String label, Object value) {
		static final DebugLine BLANK = new DebugLine(null, null);

		boolean isBlank() {
			return label == null && value == null;
		}
	}
}