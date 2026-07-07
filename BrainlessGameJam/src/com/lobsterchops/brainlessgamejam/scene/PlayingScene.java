package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.audio.AudioService;
import com.lobsterchops.brainlessgamejam.audio.AudioType;
import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.Car;
import com.lobsterchops.brainlessgamejam.entity.SlimeChild;
import com.lobsterchops.brainlessgamejam.entity.SlimeParent;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.event.CollisionEvent;
import com.lobsterchops.brainlessgamejam.event.EntityDestroyed;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.graphics.Camera;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.WaveManager;

public class PlayingScene implements Scene {

    private static final long  SHAKE_DURATION_NANOS = 200_000_000L;
    private static final float SHAKE_MAGNITUDE      = 4f;

    private long lastCollisionSfxTick = -1L;

    private final GameSystem     gameSystem;
    private final RenderPipeline renderPipeline;

    public PlayingScene(GameSystem gameSystem, RenderPipeline renderPipeline) {
        this.gameSystem     = gameSystem;
        this.renderPipeline = renderPipeline;
    }

    @Override
    public void enter() {
        EventBus eventBus = ServiceLocator.resolve(EventBus.class);
        eventBus.subscribe(EntityDestroyed.class, this::onEntityDestroyed);
        eventBus.subscribe(CollisionEvent.class,  this::onCollision);
    }

    @Override
    public void update(UpdateContext context) {
        WaveManager waveManager = ServiceLocator.resolve(WaveManager.class);
        waveManager.update(context);
        gameSystem.update();
    }

    @Override
    public void render(Graphics2D g2) {
        renderPipeline.renderWorld(g2);
    }

    private void onCollision(CollisionEvent event) {
        boolean aIsCar = event.a() instanceof Car;
        boolean bIsCar = event.b() instanceof Car;

        // Only care about car collisions
        if (!aIsCar && !bIsCar) return;

        long currentTick = gameSystem.getTick();
        if (currentTick - lastCollisionSfxTick < 30) return;
        lastCollisionSfxTick = currentTick;

        AudioService audioService = ServiceLocator.resolve(AudioService.class);
        audioService.playSfx(AudioType.COLLISION_SFX);

        Camera camera = ServiceLocator.resolve(Camera.class);
        camera.shake(SHAKE_DURATION_NANOS, SHAKE_MAGNITUDE);
    }

    private void onEntityDestroyed(EntityDestroyed event) {
        AudioService audioService = ServiceLocator.resolve(AudioService.class);

        if (event.entity() instanceof SlimeChild) {
            Camera camera = ServiceLocator.resolve(Camera.class);
            camera.shake(SHAKE_DURATION_NANOS, SHAKE_MAGNITUDE);
            audioService.playSfx(AudioType.COLLISION_SFX);
        } else if (event.entity() instanceof SlimeParent) {
            audioService.playSfx(AudioType.COLLISION_SFX);
        }
    }
}