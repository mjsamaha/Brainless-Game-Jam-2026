package com.lobsterchops.brainlessgamejam.core;

import com.lobsterchops.brainlessgamejam.audio.AudioService;
import com.lobsterchops.brainlessgamejam.audio.JavaSoundAudioService;
import com.lobsterchops.brainlessgamejam.entity.SlimeParent;
import com.lobsterchops.brainlessgamejam.graphics.Camera;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.BackgroundRenderer;
import com.lobsterchops.brainlessgamejam.render.DebugMetrics;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.scene.GameUpdater;
import com.lobsterchops.brainlessgamejam.scene.PausedScene;
import com.lobsterchops.brainlessgamejam.scene.PlayingScene;
import com.lobsterchops.brainlessgamejam.scene.SceneManager;
import com.lobsterchops.brainlessgamejam.world.GameSystem;

public class GameContext {

	public GameContext() {

		InputManager inputManager = new InputManager();
		GameSystem gameSystem = new GameSystem();
		DebugMetrics debugMetrics = new DebugMetrics();
		Camera camera = new Camera();
		camera.setZoom(1.0f);
		RenderPipeline renderPipeline = new RenderPipeline(gameSystem, debugMetrics, camera);

		AudioService audioService = new JavaSoundAudioService();
		audioService.init();

		PlayingScene playingScene = new PlayingScene(gameSystem, renderPipeline);
		SceneManager sceneManager = new SceneManager(playingScene);
		PausedScene pausedScene = new PausedScene(audioService, sceneManager, playingScene);

		GameUpdater updater = new GameUpdater(gameSystem, inputManager, renderPipeline, audioService, this::restartRun,
				sceneManager, playingScene, pausedScene);

		ServiceLocator.register(InputManager.class, inputManager);
		ServiceLocator.register(GameSystem.class, gameSystem);
		ServiceLocator.register(DebugMetrics.class, debugMetrics);
		ServiceLocator.register(RenderPipeline.class, renderPipeline);
		ServiceLocator.register(AudioService.class, audioService);
		ServiceLocator.register(GameUpdater.class, updater);
		ServiceLocator.register(SceneManager.class, sceneManager);
		ServiceLocator.register(Camera.class, camera);
	}

	public void setupNewRun() {
		GameSystem gameSystem = ServiceLocator.resolve(GameSystem.class);
		gameSystem.clear(); // clears all objects, resets tick/time, sets state to PLAYING

		// Spawn the player
		float startX = BackgroundRenderer.WORLD_WIDTH  / 2f;
		float startY = BackgroundRenderer.WORLD_HEIGHT / 2f;
		gameSystem.addObject(new SlimeParent(new Vector2(startX, startY)));
	}

	public void restartRun() {
		setupNewRun();
	}

}