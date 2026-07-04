package com.lobsterchops.brainlessgamejam.render;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.lobsterchops.brainlessgamejam.entity.Renderable;
import com.lobsterchops.brainlessgamejam.graphics.Camera;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.TileMap;

public class RenderPipeline {

    private final TileMapRenderer tileMapRenderer;
    private final DebugRenderer debugRenderer = new DebugRenderer();
    private final GameSystem gameSystem;
    private final DebugMetrics debugMetrics;
    private final Camera camera;

    private boolean debugMode = false;

    public RenderPipeline(GameSystem gameSystem, DebugMetrics debugMetrics,
                          Camera camera, TileMap tileMap) {
        this.gameSystem = gameSystem;
        this.debugMetrics = debugMetrics;
        this.camera = camera;
        this.tileMapRenderer = new TileMapRenderer(tileMap, camera);
    }

    public void render(Graphics2D g2) {
        tileMapRenderer.render(g2);
        renderEntities(g2);
        renderDebugIfEnabled(g2);
    }

    private void renderEntities(Graphics2D g2) {
        AffineTransform saved = g2.getTransform();

        g2.scale(camera.getZoom(), camera.getZoom());
        g2.translate(-camera.getOffsetX(), -camera.getOffsetY());

        for (Renderable renderable : gameSystem.getRenderableObjects()) {
            if (renderable.getRenderLayer() == RenderLayer.ENTITIES) {
                renderable.render(g2);
            }
        }

        g2.setTransform(saved);
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