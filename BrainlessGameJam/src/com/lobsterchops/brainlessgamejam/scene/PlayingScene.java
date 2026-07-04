package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Graphics2D;
import java.util.Comparator;
import java.util.Optional;
 
import com.lobsterchops.brainlessgamejam.entity.Car;
import com.lobsterchops.brainlessgamejam.entity.SlimeChild;
import com.lobsterchops.brainlessgamejam.entity.SlimeParent;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.event.CollisionEvent;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.state.GameState;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
 
public class PlayingScene implements Scene {
 
    private final GameSystem gameSystem;
    private final RenderPipeline renderPipeline;
    private final EventBus eventBus;
 
    public PlayingScene(GameSystem gameSystem, RenderPipeline renderPipeline, EventBus eventBus) {
        this.gameSystem = gameSystem;
        this.renderPipeline = renderPipeline;
        this.eventBus = eventBus;
    }
 
    @Override
    public void enter() {
        eventBus.subscribe(CollisionEvent.class, this::onCollision);
    }
 
    @Override
    public void exit() {
        eventBus.clear();
    }
 
    @Override
    public void update(UpdateContext context) {
        gameSystem.update();
    }
 
    @Override
    public void render(Graphics2D g2) {
        renderPipeline.render(g2);
    }

 
    private void onCollision(CollisionEvent event) {
        boolean aCar = event.a() instanceof Car;
        boolean bCar = event.b() instanceof Car;
        boolean aParent = event.a() instanceof SlimeParent;
        boolean bParent = event.b() instanceof SlimeParent;
        boolean aChild  = event.a() instanceof SlimeChild;
        boolean bChild  = event.b() instanceof SlimeChild;
 
        if ((aCar && bParent) || (bCar && aParent)) {
            triggerGameOver();
        } else if ((aCar && bChild) || (bCar && aChild)) {
            loseLastChild();
        }
    }
 
    private void triggerGameOver() {
        gameSystem.setState(GameState.GAME_OVER);
    }
 
    private void loseLastChild() {
        // Find the rearmost still-active SlimeChild (highest historyOffset = furthest from mama)
        Optional<SlimeChild> rearmost = gameSystem.getObjects().stream()
                .filter(o -> o instanceof SlimeChild sc && sc.isActive())
                .map(o -> (SlimeChild) o)
                .max(Comparator.comparingInt(SlimeChild::getHistoryOffset));
 
        rearmost.ifPresent(child -> {
            child.markInactive();
            // GameSystem.removeInactiveObjects() clears it before the next tick,
            // so the CollisionSystem won't fire again for this child.
        });
    }
}