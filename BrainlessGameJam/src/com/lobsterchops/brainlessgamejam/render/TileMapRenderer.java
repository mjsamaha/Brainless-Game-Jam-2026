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
		AffineTransform saved = applyWorldTransform(g2);
		renderVisibleTiles(g2);
		g2.setTransform(saved);
	}

	/**
	 * Applies the same world-space transform used by {@link RenderPipeline} for
	 * entities, and returns the saved transform for later restoration.
	 */
	private AffineTransform applyWorldTransform(Graphics2D g2) {
		AffineTransform saved = g2.getTransform();
		g2.scale(camera.getZoom(), camera.getZoom());
		g2.translate(-camera.getOffsetX(), -camera.getOffsetY());
		return saved;
	}

	private void renderVisibleTiles(Graphics2D g2) {
		TileBounds visible = computeVisibleTiles();
		for (int row = visible.minRow; row <= visible.maxRow; row++) {
			for (int col = visible.minCol; col <= visible.maxCol; col++) {
				renderTile(g2, col, row);
			}
		}
	}

	private void renderTile(Graphics2D g2, int col, int row) {
		BufferedImage sprite = sprites.get(tileMap.get(col, row));
		if (sprite != null) {
			g2.drawImage(sprite, col * T, row * T, null);
		}
	}

	private TileBounds computeVisibleTiles() {
		Bounds view = camera.getViewBounds();
		int minCol = clampCol((int) (view.x() / T));
		int minRow = clampRow((int) (view.y() / T));
		int maxCol = clampCol((int) ((view.x() + view.width()) / T));
		int maxRow = clampRow((int) ((view.y() + view.height()) / T));
		return new TileBounds(minCol, minRow, maxCol, maxRow);
	}

	private int clampCol(int col) {
		return Math.clamp(col, 0, tileMap.cols - 1);
	}

	private int clampRow(int row) {
		return Math.clamp(row, 0, tileMap.rows - 1);
	}

	private record TileBounds(int minCol, int minRow, int maxCol, int maxRow) {
	}

	private Map<TileType, BufferedImage> loadSprites() {
		Map<TileType, BufferedImage> map = new EnumMap<>(TileType.class);
		for (TileType type : TileType.values()) {
			map.put(type, AssetManager.get(type.spritePath, T, T));
		}
		return map;
	}

}