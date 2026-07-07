package com.lobsterchops.brainlessgamejam.core;

import com.lobsterchops.brainlessgamejam.audio.AudioService;
import com.lobsterchops.brainlessgamejam.audio.AudioType;
import com.lobsterchops.brainlessgamejam.audio.JavaSoundAudioService;
import com.lobsterchops.brainlessgamejam.entity.entities.SlimeChild;
import com.lobsterchops.brainlessgamejam.entity.entities.SlimeParent;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.graphics.Camera;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.DebugMetrics;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.scene.GameOverScene;
import com.lobsterchops.brainlessgamejam.scene.GameUpdater;
import com.lobsterchops.brainlessgamejam.scene.MenuScene;
import com.lobsterchops.brainlessgamejam.scene.PausedScene;
import com.lobsterchops.brainlessgamejam.scene.PlayingScene;
import com.lobsterchops.brainlessgamejam.scene.SceneManager;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.RiverLayout;
import com.lobsterchops.brainlessgamejam.world.RoadLayout;
import com.lobsterchops.brainlessgamejam.world.ScoreSystem;
import com.lobsterchops.brainlessgamejam.world.TileMap;
import com.lobsterchops.brainlessgamejam.world.WaveManager;

public class GameContext {

    // Core
    private EventBus eventBus;
    private InputManager inputManager;
    private GameSystem gameSystem;

    // Rendering
    private DebugMetrics debugMetrics;
    private Camera camera;
    private TileMap tileMap;
    private RenderPipeline renderPipeline;

    // Audio
    private AudioService audioService;

    // World
    private ScoreSystem scoreSystem;
    private WaveManager waveManager;

    // Scenes
    private SceneManager sceneManager;
    private PlayingScene playingScene;
    private MenuScene menuScene;
    private GameOverScene gameOverScene;
    private PausedScene pausedScene;

    public GameContext() {
        createCore();
        createRendering();
        createAudio();
        createWorld();
        createScenes();

        registerServices();

        sceneManager.switchTo(menuScene);
    }

    public void setupNewRun() {
        resetGame();

        SlimeParent parent = createParent();

        spawnChildren(parent);
    }

    private void resetGame() {
        gameSystem.clear();
        audioService.playMusic(AudioType.GAMEPLAY_MUSIC);
        scoreSystem.reset();
        waveManager.reset();
    }

    private void spawnChildren(SlimeParent parent) {
        for (int i = 0; i < waveManager.getCurrentChildCount(); i++) {
            gameSystem.addObject(new SlimeChild(i, parent.getPositionHistory()));
        }
    }

    private SlimeParent createParent() {
        float startX = tileMap.worldWidth() / 2f;
        float startY = tileMap.worldHeight() - TileMap.TILE_SIZE * 1.5f;

        SlimeParent parent = new SlimeParent(new Vector2(startX, startY));

        parent.initPositionHistory();

        gameSystem.addObject(parent);

        return parent;
    }

    private void createCore() {
        eventBus = new EventBus();
        inputManager = new InputManager();
        gameSystem = new GameSystem(eventBus);
        debugMetrics = new DebugMetrics();
    }

    private void createRendering() {
        camera = new Camera();
        camera.setZoom(1.5f);

        tileMap = TileMap.load("/maps/map.txt");

        renderPipeline = new RenderPipeline(gameSystem, debugMetrics, camera, tileMap);
    }

    private void createAudio() {
        audioService = new JavaSoundAudioService();
        audioService.init();
    }

    private void createWorld() {
        scoreSystem = new ScoreSystem(eventBus);

        RoadLayout road = new RoadLayout(tileMap);
        RiverLayout river = new RiverLayout(tileMap);

        waveManager = new WaveManager(gameSystem, eventBus, tileMap, road, river);
    }

    private void createScenes() {
        playingScene = new PlayingScene(gameSystem, renderPipeline);

        sceneManager = new SceneManager(null);

        menuScene = new MenuScene(sceneManager, playingScene, this::setupNewRun);

        gameOverScene = new GameOverScene(sceneManager, menuScene, this::setupNewRun, eventBus);

        pausedScene = new PausedScene(audioService, sceneManager, playingScene);

        GameUpdater updater = new GameUpdater(gameSystem, inputManager, renderPipeline, audioService, this::setupNewRun,
                sceneManager, playingScene, pausedScene, menuScene, gameOverScene);

        ServiceLocator.register(GameUpdater.class, updater);
    }

    private void registerServices() {
        ServiceLocator.register(EventBus.class, eventBus);
        ServiceLocator.register(InputManager.class, inputManager);
        ServiceLocator.register(GameSystem.class, gameSystem);

        ServiceLocator.register(DebugMetrics.class, debugMetrics);
        ServiceLocator.register(RenderPipeline.class, renderPipeline);

        ServiceLocator.register(AudioService.class, audioService);

        ServiceLocator.register(SceneManager.class, sceneManager);

        ServiceLocator.register(Camera.class, camera);
        ServiceLocator.register(TileMap.class, tileMap);

        ServiceLocator.register(ScoreSystem.class, scoreSystem);
        ServiceLocator.register(WaveManager.class, waveManager);

        ServiceLocator.register(MenuScene.class, menuScene);
        ServiceLocator.register(GameOverScene.class, gameOverScene);
    }
}