package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
 
/**
 * <h4>Wraps the existing gameplay loop (GameSystem + RenderPipeline) as a {@link Scene}.</h4>
 * <p>This is an adapter only — it does not change how GameSystem or RenderPipeline
 * behave. Update/render calls are simply forwarded.</p>
 */
public class PlayingScene implements Scene {
 
	private final GameSystem gameSystem;
	private final RenderPipeline renderPipeline;
 
	public PlayingScene(GameSystem gameSystem, RenderPipeline renderPipeline) {
		this.gameSystem = gameSystem;
		this.renderPipeline = renderPipeline;
	}
 
	@Override
	public void update(UpdateContext context) {
		gameSystem.update();
	}
 
	@Override
	public void render(Graphics2D g2) {
		renderPipeline.render(g2);
	}
 
}