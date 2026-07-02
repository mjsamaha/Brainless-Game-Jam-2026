package com.lobsterchops.brainlessgamejam.core;

import java.util.Random;

import com.lobsterchops.brainlessgamejam.audio.AudioService;
import com.lobsterchops.brainlessgamejam.audio.JavaSoundAudioService;
<<<<<<< main
import com.lobsterchops.brainlessgamejam.entity.FriendlyEntity;
import com.lobsterchops.brainlessgamejam.entity.ImposterEntity;
import com.lobsterchops.brainlessgamejam.entity.PlayerEntity;
import com.lobsterchops.brainlessgamejam.event.EventBus;
=======
import com.lobsterchops.brainlessgamejam.audio.SoundType;
>>>>>>> 91d3270 event bus implementation
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.math.Bounds;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.DebugMetrics;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.render.TrailRenderer;
import com.lobsterchops.brainlessgamejam.scene.GameUpdater;
import com.lobsterchops.brainlessgamejam.scene.PausedScene;
import com.lobsterchops.brainlessgamejam.scene.PlayingScene;
import com.lobsterchops.brainlessgamejam.scene.SceneManager;
import com.lobsterchops.brainlessgamejam.state.GameState;
import com.lobsterchops.brainlessgamejam.world.Arena;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.TrailSystem;


public class GameContext {
	
	private static final int INITIAL_FRIENDLIES = 5;
	private static final int INITIAL_IMPOSTERS = 3;
	
	public GameContext() {
<<<<<<< main

		EventBus eventBus = new EventBus();

=======
		
>>>>>>> 91d3270 event bus implementation
		InputManager inputManager = new InputManager();
		GameSystem gameSystem = new GameSystem();
		DebugMetrics debugMetrics = new DebugMetrics();
		Arena arena = new Arena();
		TrailSystem trailSystem = new TrailSystem();
		TrailRenderer trailRenderer = new TrailRenderer(trailSystem);

		RenderPipeline renderPipeline = new RenderPipeline(gameSystem, debugMetrics, arena, trailRenderer);

		AudioService audioService = new JavaSoundAudioService();
		audioService.init();

		PlayingScene playingScene = new PlayingScene(gameSystem, renderPipeline, eventBus, trailSystem);
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
		ServiceLocator.register(Arena.class, arena);
		ServiceLocator.register(TrailSystem.class, trailSystem);
		ServiceLocator.register(TrailRenderer.class, trailRenderer);
		
	}
	
	public void setupNewRun() {
		InputManager inputManager = ServiceLocator.resolve(InputManager.class);
		GameSystem gameSystem = ServiceLocator.resolve(GameSystem.class);
		Arena arena = ServiceLocator.resolve(Arena.class);
		TrailSystem trailSystem = ServiceLocator.resolve(TrailSystem.class);

		Vector2 spawnPosition = arena.getCenter();
		PlayerEntity player = new PlayerEntity(spawnPosition, inputManager, arena, trailSystem);

		gameSystem.setState(GameState.PLAYING);
		gameSystem.addObject(player);

		ServiceLocator.register(PlayerEntity.class, player);

		spawnInitialSwarm(gameSystem, player, arena);

		//audioService.playMusic(SoundType.GAMEPLAY_MUSIC);
	}
	
	public void restartRun() {
<<<<<<< main
		ServiceLocator.clear();
		new GameContext();
		ServiceLocator.resolve(EventBus.class).clear();
=======
>>>>>>> 91d3270 event bus implementation
		ServiceLocator.resolve(GameSystem.class).clear();
		setupNewRun();
	}

	private void spawnInitialSwarm(GameSystem gameSystem, PlayerEntity player, Arena arena) {
	    Random random = new Random();
	    Bounds bounds = arena.getBounds();

	    for (int i = 0; i < INITIAL_FRIENDLIES; i++) {
	        Vector2 pos = randomSpawnPosition(random, bounds);
	        gameSystem.addObject(new FriendlyEntity(pos, player, arena));
	    }

	    for (int i = 0; i < INITIAL_IMPOSTERS; i++) {
	        Vector2 pos = randomSpawnPosition(random, bounds);
	        gameSystem.addObject(new ImposterEntity(pos, player, arena, random));
	    }
	}

	private Vector2 randomSpawnPosition(Random random, Bounds bounds) {
		float x = bounds.x() + random.nextFloat() * bounds.width();
		float y = bounds.y() + random.nextFloat() * bounds.height();
		return new Vector2(x, y);
	}

}