package com.lobsterchops.brainlessgamejam.render;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.entity.Renderable;
import com.lobsterchops.brainlessgamejam.state.GameState;
import com.lobsterchops.brainlessgamejam.world.Arena;
import com.lobsterchops.brainlessgamejam.world.GameSystem;

public class RenderPipeline {

	private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
	private final DebugRenderer debugRenderer = new DebugRenderer();
	private final GameSystem gameSystem;
	private final DebugMetrics debugMetrics;
	
	private final Arena arena;
	private final TrailRenderer trailRenderer;
	

	private boolean debugMode = false;

	public RenderPipeline(GameSystem gameSystem, DebugMetrics debugMetrics, Arena arena, TrailRenderer trailRenderer) {
		this.gameSystem = gameSystem;
		this.debugMetrics = debugMetrics;
		this.arena = arena;
		this.trailRenderer = trailRenderer;
	}

	public void render(Graphics2D g2) {
		
		backgroundRenderer.render(g2);
		
		arena.render(g2);
		
		trailRenderer.render(g2);
		
		renderLayer(g2, RenderLayer.ENTITIES);
		
		if (gameSystem.getState() == GameState.PAUSED) {
			// render paused screen
		} else if (gameSystem.getState() == GameState.GAME_OVER) {
			// render game over screen
		}
		
		// debug
		if (debugMode) {
			debugRenderer.render(g2, gameSystem, debugMetrics);
		}

	}

	private void renderLayer(Graphics2D g2, RenderLayer layer) {
//		switch (layer) {
//			case BACKGROUND -> backgroundRenderer.render(g2);
//			case ENTITIES -> renderEntities(g2);
//			case DEBUG -> renderDebugIfEnabled(g2);
//		}
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