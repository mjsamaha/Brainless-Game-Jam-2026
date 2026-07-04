package com.lobsterchops.brainlessgamejam.core;

import com.lobsterchops.brainlessgamejam.audio.AudioService;
import com.lobsterchops.brainlessgamejam.audio.JavaSoundAudioService;
import com.lobsterchops.brainlessgamejam.entity.SlimeChild;
import com.lobsterchops.brainlessgamejam.entity.SlimeParent;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.graphics.Camera;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.DebugMetrics;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.scene.GameUpdater;
import com.lobsterchops.brainlessgamejam.scene.PausedScene;
import com.lobsterchops.brainlessgamejam.scene.PlayingScene;
import com.lobsterchops.brainlessgamejam.scene.SceneManager;
import com.lobsterchops.brainlessgamejam.state.GameState;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.RiverLayout;
import com.lobsterchops.brainlessgamejam.world.RoadLayout;
import com.lobsterchops.brainlessgamejam.world.TileMap;

public class GameContext {

	public GameContext() {

	    EventBus eventBus = new EventBus();
	    InputManager inputManager = new InputManager();
	    GameSystem gameSystem = new GameSystem(eventBus);
	    DebugMetrics debugMetrics = new DebugMetrics();

	    Camera camera = new Camera();
	    camera.setZoom(1.5f);

	    TileMap tileMap = TileMap.load("/maps/map.txt");

	    RenderPipeline renderPipeline = new RenderPipeline(gameSystem, debugMetrics, camera, tileMap);

	    AudioService audioService = new JavaSoundAudioService();
	    audioService.init();

	    PlayingScene playingScene = new PlayingScene(gameSystem, renderPipeline, eventBus);
	    SceneManager sceneManager = new SceneManager(playingScene);
	    PausedScene pausedScene = new PausedScene(audioService, sceneManager, playingScene);

	    GameUpdater updater = new GameUpdater(gameSystem, inputManager, renderPipeline, audioService,
	            this::restartRun, sceneManager, playingScene, pausedScene);

	    ServiceLocator.register(EventBus.class, eventBus);
	    ServiceLocator.register(InputManager.class, inputManager);
	    ServiceLocator.register(GameSystem.class, gameSystem);
	    ServiceLocator.register(DebugMetrics.class, debugMetrics);
	    ServiceLocator.register(RenderPipeline.class, renderPipeline);
	    ServiceLocator.register(AudioService.class, audioService);
	    ServiceLocator.register(GameUpdater.class, updater);
	    ServiceLocator.register(SceneManager.class, sceneManager);
	    ServiceLocator.register(Camera.class, camera);
	    ServiceLocator.register(TileMap.class, tileMap);
	}

	public void setupNewRun() {
	    TileMap tileMap = ServiceLocator.resolve(TileMap.class);
	    GameSystem gameSystem = ServiceLocator.resolve(GameSystem.class);
	    
	    gameSystem.clear();

	    gameSystem.setState(GameState.PLAYING);

	    float startX = tileMap.worldWidth()  / 2f;
	    float startY = tileMap.worldHeight() - TileMap.TILE_SIZE * 1.5f; 
	    
	    SlimeParent mama = new SlimeParent(new Vector2(startX, startY));
	    gameSystem.addObject(mama);
	    
	    for (int i = 0; i < SlimeChild.NUM_CHILDREN; i++) {
	    	gameSystem.addObject(new SlimeChild(i, mama.getPositionHistory()));
	    }
	    
	    RoadLayout roadLayout = new RoadLayout(tileMap);
	    roadLayout.spawnCars(1, gameSystem);
	    
	    RiverLayout riverLayout = new RiverLayout(tileMap);
	    riverLayout.spawnLogs(1, gameSystem);
	}

	public void restartRun() {
		setupNewRun();
	}

}