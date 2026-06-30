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
		backgroundRenderer.render(g2);

		renderLayer(g2, RenderLayer.ENTITIES);
	


		if (gameSystem.getState() == GameState.PAUSED) {
			// Render paused screen
			
		} else if (gameSystem.getState() == GameState.GAME_OVER) {
			// Render game over screen
		}

		if (debugMode) {
			// Render debug information
			debugRenderer.render(g2, gameSystem, debugMetrics);
		}
	}
	
	private void renderLayer(Graphics2D g2, RenderLayer layer) {
		for (Renderable renderable : gameSystem.getRenderableObjects()) {
			if (renderable.getRenderLayer() == layer) {
				renderable.render(g2);
			}
		}
	}
	
	public boolean isDebugEnabled() {
		return debugMode;
	}
	
	public void toggleDebug() {
		debugMode = !debugMode;
	}
}