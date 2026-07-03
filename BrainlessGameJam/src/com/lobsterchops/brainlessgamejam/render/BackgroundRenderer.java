package com.lobsterchops.brainlessgamejam.render;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.config.ScreenConfig;
import com.lobsterchops.brainlessgamejam.graphics.Camera;

public class BackgroundRenderer {

    // Height of each terrain strip in world pixels
    public static final int STRIP_HEIGHT = 64;

    // How many strips tall the world is
    private static final int STRIP_COUNT = 20;

    // World height derived from strip layout
    public static final int WORLD_HEIGHT = STRIP_HEIGHT * STRIP_COUNT;
    public static final int WORLD_WIDTH  = ScreenConfig.WIDTH * 4;

    private final Camera camera;

    public BackgroundRenderer(Camera camera) {
        this.camera = camera;
    }

    public void render(Graphics2D g2) {
        AffineTransform saved = g2.getTransform();

        // Match the same transform RenderPipeline applies for entities
        g2.scale(camera.getZoom(), camera.getZoom());
        g2.translate(-camera.getOffsetX(), -camera.getOffsetY());

        for (int i = 0; i < STRIP_COUNT; i++) {
            int y = i * STRIP_HEIGHT;
            g2.setColor(stripColor(i));
            g2.fillRect(0, y, WORLD_WIDTH, STRIP_HEIGHT);

            // Road centre-line dashes on road strips
            if (isRoad(i)) {
                g2.setColor(ColorConfig.ROAD_LINE);
                int lineY = y + STRIP_HEIGHT / 2 - 2;
                for (int x = 0; x < WORLD_WIDTH; x += 40) {
                    g2.fillRect(x, lineY, 20, 4);
                }
            }
        }

        g2.setTransform(saved);
    }

    private java.awt.Color stripColor(int index) {
        return switch (index % 4) {
            case 0 -> ColorConfig.GRASS;
            case 1 -> ColorConfig.ROAD;
            case 2 -> ColorConfig.GRASS_ALT;
            case 3 -> ColorConfig.WATER;
            default -> ColorConfig.GRASS;
        };
    }

    private boolean isRoad(int index) {
        return index % 4 == 1;
    }
}