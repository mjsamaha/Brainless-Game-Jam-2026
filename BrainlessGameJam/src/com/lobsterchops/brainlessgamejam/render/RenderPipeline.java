package com.lobsterchops.brainlessgamejam.render;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.entity.Renderable;
import com.lobsterchops.brainlessgamejam.state.GameState;
import com.lobsterchops.brainlessgamejam.world.GameSystem;

public class RenderPipeline {

	private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
	private final DebugRenderer debugRenderer = new DebugRenderer();
	private final GameSystem gameSystem;
	private final DebugMetrics debugMetrics;

	private boolean debugMode = false;

	public RenderPipeline(GameSystem gameSystem, DebugMetrics debugMetrics) {
		this.gameSystem = gameSystem;
		this.debugMetrics = debugMetrics;
	}

	public void render(Graphics2D g2) {
		for (RenderLayer layer : RenderLayer.drawOrder()) {
			renderLayer(g2, layer);
		}
	}

	private void renderLayer(Graphics2D g2, RenderLayer layer) {
		switch (layer) {
			case BACKGROUND -> backgroundRenderer.render(g2);
			case ENTITIES -> renderEntities(g2);
			case DEBUG -> renderDebugIfEnabled(g2);
		}
	}

	private void renderEntities(Graphics2D g2) {
		for (Renderable renderable : gameSystem.getRenderableObjects()) {
			if (renderable.getRenderLayer() == RenderLayer.ENTITIES) {
				renderable.render(g2);
			}
		}

		if (gameSystem.getState() == GameState.PAUSED) {
			// Render paused screen
		} else if (gameSystem.getState() == GameState.GAME_OVER) {
			// Render game over screen
		}
	}

	private void renderDebugIfEnabled(Graphics2D g2) {
		if (debugMode) {
			debugRenderer.render(g2, gameSystem, debugMetrics);
		}
	}

	public boolean isDebugEnabled() {
		return debugMode;
	}

	public void toggleDebug() {
		debugMode = !debugMode;
	}
}