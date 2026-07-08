package com.lobsterchops.brainlessgamejam.world;

import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.entity.entities.SlimeChild;
import com.lobsterchops.brainlessgamejam.entity.entities.SlimeParent;
import com.lobsterchops.brainlessgamejam.event.CrossingCompleted;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.event.WaveCompleted;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.util.Timer;
import com.lobsterchops.brainlessgamejam.world.common.GameState;

public class WaveManager {

	private int currentChildCount = SlimeChild.NUM_CHILDREN;

	public static final long INTER_WAVE_DELAY_MS = 3_000L;

	private final GameSystem gameSystem;
	private final EventBus eventBus;
	private final TileMap tileMap;
	private final RoadLayout roadLayout;
	private final RiverLayout riverLayout;

	private int currentWave = 1;
	private int waveStartChildCount = currentChildCount;
	private boolean interWaveActive = false;
	private long interWaveStartMs = 0L;
	private Timer interWaveTimer = null;

	public WaveManager(GameSystem gameSystem, EventBus eventBus, TileMap tileMap, RoadLayout roadLayout,
			RiverLayout riverLayout) {
		this.gameSystem = gameSystem;
		this.eventBus = eventBus;
		this.tileMap = tileMap;
		this.roadLayout = roadLayout;
		this.riverLayout = riverLayout;

		eventBus.subscribe(CrossingCompleted.class, this::onCrossingCompleted);
	}

	/**
	 * Call once per tick from {@code PlayingScene.update()}. Ticks the inter-wave
	 * timer and starts the next wave when it expires.
	 */
	public void update(UpdateContext context) {
		if (!interWaveActive)
			return;
		if (interWaveTimer != null && interWaveTimer.finished()) {
			beginNextWave();
		}
	}

	/**
	 * Resets to wave 1 and spawns the initial hazard set. Call from
	 * {@code GameContext.setupNewRun()}.
	 */
	public void reset() {
		currentChildCount = SlimeChild.NUM_CHILDREN;
		currentWave = 1;
		interWaveActive = false;
		interWaveTimer = null;
		interWaveStartMs = 0L;
		waveStartChildCount = currentChildCount;
		spawnHazards();
	}

	/** Current wave number (1-based). */
	public int getCurrentWave() {
		return currentWave;
	}

	/** True while the inter-wave countdown is running. */
	public boolean isInterWaveActive() {
		return interWaveActive;
	}

	/**
	 * Milliseconds remaining in the inter-wave delay, or 0 when none is active.
	 * Used by {@code HudRenderer} to drive the banner fade.
	 */
	public long getInterWaveRemainingMs() {
		if (!interWaveActive || interWaveTimer == null)
			return 0L;
		long elapsed = System.currentTimeMillis() - interWaveStartMs;
		return Math.max(0L, INTER_WAVE_DELAY_MS - elapsed);
	}

	/**
	 * Number of children that were alive at the start of the current wave. Used by
	 * {@code HudRenderer} to draw the correct number of dot-silhouette slots.
	 */
	public int getWaveStartChildCount() {
		return waveStartChildCount;
	}

	private void onCrossingCompleted(CrossingCompleted event) {
		if (interWaveActive)
			return; // guard against double-fire

		gameSystem.setState(GameState.WAVE_COMPLETE);
		repositionParent();

		interWaveActive = true;
		interWaveStartMs = System.currentTimeMillis();
		interWaveTimer = new Timer(INTER_WAVE_DELAY_MS);
	}

	private void beginNextWave() {
		interWaveActive = false;
		interWaveTimer = null;
		currentWave++;
		currentChildCount += currentWave;

		roadLayout.clearCars(gameSystem);
		riverLayout.clearLogs(gameSystem);
		spawnHazards();
		respawnChildren();

		waveStartChildCount = currentChildCount;

		gameSystem.setState(GameState.PLAYING);
		eventBus.publish(new WaveCompleted(currentWave));
	}

	private void spawnHazards() {
		roadLayout.spawnCars(currentWave, gameSystem);
		riverLayout.spawnLogs(currentWave, gameSystem);
	}

	private void repositionParent() {
		gameSystem.getObjects().stream().filter(o -> o instanceof SlimeParent).map(o -> (SlimeParent) o).findFirst()
				.ifPresent(parent -> {
					float startX = tileMap.worldWidth() / 2f;
					float startY = tileMap.worldHeight() - TileMap.TILE_SIZE * 1.5f;
					parent.setPosition(new Vector2(startX, startY));
					parent.resetCrossingGuard();
					parent.initPositionHistory();
				});
	}

	private void respawnChildren() {
		removeChildren();

		spawnChildren();
	}

	private void spawnChildren() {
		// Respawn fresh children on the parent's history
		gameSystem.getObjects().stream().filter(o -> o instanceof SlimeParent).map(o -> (SlimeParent) o).findFirst()
				.ifPresent(parent -> {
					for (int i = 0; i < currentChildCount; i++) {
						gameSystem.addObject(new SlimeChild(i, parent.getPositionHistory()));
					}
				});
	}

	private void removeChildren() {
		// Remove any existing children first
		gameSystem.getObjects().stream().filter(o -> o instanceof SlimeChild).map(o -> (SlimeChild) o)
				.forEach(SlimeChild::markInactive);
	}

	private int countLivingChildren() {
		return (int) gameSystem.getObjects().stream()
				.filter(o -> o instanceof com.lobsterchops.brainlessgamejam.entity.entities.SlimeChild && o.isActive())
				.count();
	}

	public int getCurrentChildCount() {
		return currentChildCount;
	}
}