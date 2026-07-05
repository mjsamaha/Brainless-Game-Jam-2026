package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.WaveManager;
 

public class PlayingScene implements Scene {
 
    private final GameSystem    gameSystem;
    private final RenderPipeline renderPipeline;
 
    public PlayingScene(GameSystem gameSystem, RenderPipeline renderPipeline) {
        this.gameSystem     = gameSystem;
        this.renderPipeline = renderPipeline;
    }
 
    @Override
    public void update(UpdateContext context) {
        // WaveManager must tick regardless of GameState so the inter-wave
        // timer can expire even while state == WAVE_COMPLETE (which pauses
        // entity updates in GameSystem but doesn't block scene updates).
        WaveManager waveManager = ServiceLocator.resolve(WaveManager.class);
        waveManager.update(context);
 
        gameSystem.update();
    }
 
    @Override
    public void render(Graphics2D g2) {
        renderPipeline.render(g2);
    }
}