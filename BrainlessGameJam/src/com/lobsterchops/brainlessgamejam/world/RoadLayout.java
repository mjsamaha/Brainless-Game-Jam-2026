package com.lobsterchops.brainlessgamejam.world;

import java.util.ArrayList;
import java.util.List;

import com.lobsterchops.brainlessgamejam.entity.entities.Car;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.world.common.TileType;

public class RoadLayout {

	// How many cars per lane on wave 1. Each subsequent wave adds one more.
	private static final int BASE_CARS_PER_LANE = 2;

	// Base speed in pixels per tick for wave 1 lanes.
	private static final float BASE_SPEED = 2.0f;

	// Speed multiplier applied each wave.
	private static final float SPEED_SCALE_PER_WAVE = 1.15f;

	// Maximum number of active lanes regardless of wave.
	private static final int MAX_LANES = 5;

	private final TileMap tileMap;

	public RoadLayout(TileMap tileMap) {
		this.tileMap = tileMap;
	}

	/**
	 * Scans the tile map for road lanes, then spawns cars into the game system
	 * scaled to the given wave number (1-indexed).
	 */
	public void spawnCars(int wave, GameSystem gameSystem) {
		List<Float> laneYCentres = findRoadLaneCentres();

		// Cap active lanes to MAX_LANES and scale up with wave number
		int activeLanes = Math.min(wave + 1, Math.min(laneYCentres.size(), MAX_LANES));
		int carsPerLane = BASE_CARS_PER_LANE + (wave - 1);
		float speed = BASE_SPEED * (float) Math.pow(SPEED_SCALE_PER_WAVE, wave - 1);

		float worldWidth = tileMap.worldWidth();

		for (int laneIndex = 0; laneIndex < activeLanes; laneIndex++) {
			float laneY = laneYCentres.get(laneIndex);

			// Alternate lane directions: even lanes go right, odd go left
			float laneSpeed = (laneIndex % 2 == 0) ? speed : -speed;

			// Stagger starting X positions evenly across the world width
			float spacing = worldWidth / carsPerLane;

			for (int carIndex = 0; carIndex < carsPerLane; carIndex++) {
				float startX = spacing * carIndex + spacing / 2f;
				Vector2 position = new Vector2(startX, laneY);
				gameSystem.addObject(new Car(position, laneSpeed, laneIndex));
			}
		}
	}

	/**
	 * Removes all Car objects from the game system — call before spawning a new
	 * wave.
	 */
	public void clearCars(GameSystem gameSystem) {
		gameSystem.getObjects().stream().filter(o -> o instanceof Car).map(o -> (Car) o)
				.forEach(car -> car.markInactive());
	}

	/**
	 * Scans the tile map row by row and returns the world-space Y centre of each
	 * row that contains a ROAD_YELLOW tile (the lane centre marker). Falls back to
	 * any ROAD tile row if no yellow strips are found.
	 */
	private List<Float> findRoadLaneCentres() {
		List<Float> centres = new ArrayList<>();

		for (int row = 0; row < tileMap.rows; row++) {
			if (rowContains(row, TileType.ROAD_YELLOW)) {
				float centreY = row * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
				centres.add(centreY);
			}
		}

		// Fallback: use plain ROAD rows if no yellow strips defined in map
		if (centres.isEmpty()) {
			for (int row = 0; row < tileMap.rows; row++) {
				if (rowContains(row, TileType.ROAD)) {
					float centreY = row * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
					centres.add(centreY);
				}
			}
		}

		return centres;
	}

	private boolean rowContains(int row, TileType type) {
		for (int col = 0; col < tileMap.cols; col++) {
			if (tileMap.get(col, row) == type)
				return true;
		}
		return false;
	}
}