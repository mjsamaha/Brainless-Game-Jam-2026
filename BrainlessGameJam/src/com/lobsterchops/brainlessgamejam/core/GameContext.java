package com.lobsterchops.brainlessgamejam.core;

import com.lobsterchops.brainlessgamejam.audio.AudioService;
import com.lobsterchops.brainlessgamejam.audio.JavaSoundAudioService;
import com.lobsterchops.brainlessgamejam.audio.SoundType;
import com.lobsterchops.brainlessgamejam.input.InputManager;
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
		
		RenderPipeline renderPipeline = new RenderPipeline(gameSystem, debugMetrics);
		
		AudioService audioService = new JavaSoundAudioService();
		audioService.init();
		
		PlayingScene playingScene = new PlayingScene(gameSystem, renderPipeline);
		SceneManager sceneManager = new SceneManager(playingScene);
		PausedScene pausedScene = new PausedScene(audioService, sceneManager, playingScene);

		GameUpdater updater = new GameUpdater(gameSystem, inputManager, renderPipeline, audioService,
				this::restartRun, sceneManager, playingScene, pausedScene);

		ServiceLocator.register(InputManager.class, inputManager);
		ServiceLocator.register(GameSystem.class, gameSystem);
		ServiceLocator.register(DebugMetrics.class, debugMetrics);
		ServiceLocator.register(RenderPipeline.class, renderPipeline);
		ServiceLocator.register(AudioService.class, audioService);
		ServiceLocator.register(GameUpdater.class, updater);
		ServiceLocator.register(SceneManager.class, sceneManager);
		
		
	}
	
	public void setupNewRun() {
		InputManager inputManager = ServiceLocator.resolve(InputManager.class);
		GameSystem gameSystem = ServiceLocator.resolve(GameSystem.class);
		AudioService audioService = ServiceLocator.resolve(AudioService.class);
		
		//audioService.playMusic(SoundType.GAMEPLAY_MUSIC);
		
	}
	
	public void restartRun() {
		ServiceLocator.resolve(GameSystem.class).clear();
		setupNewRun();
	}

}