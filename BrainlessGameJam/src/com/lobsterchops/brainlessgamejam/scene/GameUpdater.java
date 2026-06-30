package com.lobsterchops.brainlessgamejam.scene;

import com.lobsterchops.brainlessgamejam.audio.AudioService;
import com.lobsterchops.brainlessgamejam.input.Command;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.state.GameState;
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

	public GameUpdater(GameSystem gameSystem, InputManager input, RenderPipeline renderPipeline,
			AudioService audioService, Runnable restartCallback,
			SceneManager sceneManager, PlayingScene playingScene, PausedScene pausedScene) {
		this.gameSystem = gameSystem;
		this.input = input;
		this.renderPipeline = renderPipeline;
		this.audioService = audioService;
		this.restartCallback = restartCallback;
		this.sceneManager = sceneManager;
		this.playingScene = playingScene;
		this.pausedScene = pausedScene;
	}

	public void update() {
		processCommands();
		gameSystem.update();
		audioService.update();
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

		if (current == playingScene) {
			sceneManager.switchTo(pausedScene);
		} else if (current == pausedScene) {
			pausedScene.unpause();
		}
	}

}
