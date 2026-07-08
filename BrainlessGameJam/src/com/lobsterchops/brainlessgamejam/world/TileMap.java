package com.lobsterchops.brainlessgamejam.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.lobsterchops.brainlessgamejam.world.common.TileType;

public class TileMap {

	private static final Logger LOGGER = Logger.getLogger(TileMap.class.getName());

	public static final int TILE_SIZE = 32; // world pixels per tile

	private final TileType[][] grid;
	public final int cols;
	public final int rows;

	private TileMap(TileType[][] grid) {
		this.grid = grid;
		this.rows = grid.length;
		this.cols = rows > 0 ? grid[0].length : 0;
	}

	public static TileMap load(String resourcePath) {
		InputStream stream = TileMap.class.getResourceAsStream(resourcePath);

		if (stream == null) {
			LOGGER.warning("Map not found on classpath: " + resourcePath);
			return fallback();
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			List<TileType[]> rows = new ArrayList<>();

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty())
					continue;

				TileType[] row = new TileType[line.length()];
				for (int col = 0; col < line.length(); col++) {
					row[col] = TileType.fromChar(line.charAt(col));
				}
				rows.add(row);
			}

			return new TileMap(rows.toArray(new TileType[0][]));

		} catch (IOException e) {
			LOGGER.warning("Failed to read map: " + resourcePath + " — " + e.getMessage());
			return fallback();
		}
	}

	public TileType get(int col, int row) {
		if (row < 0 || row >= rows || col < 0 || col >= cols)
			return TileType.GRASS;
		return grid[row][col];
	}

	public int worldWidth() {
		return cols * TILE_SIZE;
	}

	public int worldHeight() {
		return rows * TILE_SIZE;
	}

	/** 5x5 all-grass map used if the real map fails to load. */
	private static TileMap fallback() {
		TileType[][] grid = new TileType[5][5];
		for (TileType[] row : grid) {
			java.util.Arrays.fill(row, TileType.GRASS);
		}
		return new TileMap(grid);
	}
}
