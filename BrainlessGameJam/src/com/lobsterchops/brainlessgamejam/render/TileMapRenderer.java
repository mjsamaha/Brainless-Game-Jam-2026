package com.lobsterchops.brainlessgamejam.render;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import com.lobsterchops.brainlessgamejam.graphics.AssetManager;
import com.lobsterchops.brainlessgamejam.graphics.Camera;
import com.lobsterchops.brainlessgamejam.math.Bounds;
import com.lobsterchops.brainlessgamejam.world.TileMap;
import com.lobsterchops.brainlessgamejam.world.common.TileType;

public class TileMapRenderer {

    private static final int T = TileMap.TILE_SIZE;

    private final TileMap tileMap;
    private final Camera camera;
    private final Map<TileType, BufferedImage> sprites;

    public TileMapRenderer(TileMap tileMap, Camera camera) {
        this.tileMap = tileMap;
        this.camera = camera;
        this.sprites = loadSprites();
    }

    public void render(Graphics2D g2) {
        AffineTransform saved = g2.getTransform();

        // Same transform as RenderPipeline applies for entities
        g2.scale(camera.getZoom(), camera.getZoom());
        g2.translate(-camera.getOffsetX(), -camera.getOffsetY());

        // Only draw tiles visible in the camera viewport — culling
        Bounds view = camera.getViewBounds();

        int minCol = Math.max(0, (int) (view.x() / T));
        int minRow = Math.max(0, (int) (view.y() / T));
        int maxCol = Math.min(tileMap.cols - 1, (int) ((view.x() + view.width())  / T));
        int maxRow = Math.min(tileMap.rows - 1, (int) ((view.y() + view.height()) / T));

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                TileType type = tileMap.get(col, row);
                BufferedImage sprite = sprites.get(type);
                if (sprite != null) {
                    g2.drawImage(sprite, col * T, row * T, null);
                }
            }
        }

        g2.setTransform(saved);
    }

    private Map<TileType, BufferedImage> loadSprites() {
        Map<TileType, BufferedImage> map = new EnumMap<>(TileType.class);
        for (TileType type : TileType.values()) {
            map.put(type, AssetManager.get(type.spritePath, T, T));
        }
        return map;
    }
}