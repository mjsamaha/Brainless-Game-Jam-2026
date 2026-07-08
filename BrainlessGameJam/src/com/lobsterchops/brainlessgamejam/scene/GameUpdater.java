package com.lobsterchops.brainlessgamejam.scene;

import com.lobsterchops.brainlessgamejam.audio.AudioService;
import com.lobsterchops.brainlessgamejam.config.GameLoopConfig;
import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.entity.entities.SlimeParent;
import com.lobsterchops.brainlessgamejam.graphics.Camera;
import com.lobsterchops.brainlessgamejam.input.Command;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.world.GameSystem;

public class GameUpdater {

	private final GameSystem gameSystem;
	private final InputManager input;
	private final RenderPipeline renderPipeline;
	private final AudioService audioService;
	private final Runnable restartCallback;
	private final SceneManager sceneManager;
	private final PlayingScene playingScene;
	private final PausedScene pausedScene;
	private final MenuScene menuScene;
	private final GameOverScene gameOverScene;

	public GameUpdater(GameSystem gameSystem, InputManager input, RenderPipeline renderPipeline,
			AudioService audioService, Runnable restartCallback, SceneManager sceneManager, PlayingScene playingScene,
			PausedScene pausedScene, MenuScene menuScene, GameOverScene gameOverScene) {
		this.gameSystem = gameSystem;
		this.input = input;
		this.renderPipeline = renderPipeline;
		this.audioService = audioService;
		this.restartCallback = restartCallback;
		this.sceneManager = sceneManager;
		this.playingScene = playingScene;
		this.pausedScene = pausedScene;
		this.menuScene = menuScene;
		this.gameOverScene = gameOverScene;
	}

	public void update() {
		processCommands();
		followPlayer();
		UpdateContext context = UpdateContext.fixed(gameSystem, gameSystem.getTick(), gameSystem.getElapsedMillis());
		sceneManager.update(context);
		audioService.update();
	}

	private void followPlayer() {
		Camera camera = ServiceLocator.resolve(Camera.class);

		// Tick the shake animation every frame using the fixed interval
		camera.update((long) GameLoopConfig.DRAW_INTERVAL_NANOS);

		gameSystem.getObjects().stream().filter(o -> o instanceof SlimeParent).map(o -> (SlimeParent) o).findFirst()
				.ifPresent(p -> camera.follow(p.getPosition()));
	}

	private void processCommands() {
		Command command;
		while ((command = input.pollCommand()) != null) {
			switch (command) {
			case TOGGLE_DEBUG -> renderPipeline.toggleDebug();
			case TOGGLE_PAUSE -> togglePause();
			}
		}
	}

	private void togglePause() {
		Scene current = sceneManager.getCurrentScene();
		if (current == menuScene || current == gameOverScene)
			return;
		if (current == playingScene) {
			sceneManager.switchTo(pausedScene);
		} else if (current == pausedScene) {
			pausedScene.unpause();
		}
	}
}