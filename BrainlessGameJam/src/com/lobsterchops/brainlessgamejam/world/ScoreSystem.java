package com.lobsterchops.brainlessgamejam.world;

import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.Car;
import com.lobsterchops.brainlessgamejam.entity.GameObject;
import com.lobsterchops.brainlessgamejam.entity.SlimeChild;
import com.lobsterchops.brainlessgamejam.entity.SlimeParent;
import com.lobsterchops.brainlessgamejam.event.CollisionEvent;
import com.lobsterchops.brainlessgamejam.event.CrossingCompleted;
import com.lobsterchops.brainlessgamejam.event.EntityDestroyed;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.event.GameOverEvent;


public class ScoreSystem {
 
    private static final int POINTS_PER_CHILD = 100;
    private static final int PERFECT_BONUS    = 500;
    private static final int CAR_HIT_PENALTY  = 150;
 
    private int score;
    private int highScore;
 
    private int     lastCrossingDelta;
    private boolean lastCrossingWasPerfect;
 
    private final EventBus eventBus;
 
    public ScoreSystem(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe(CrossingCompleted.class, this::onCrossingCompleted);
        eventBus.subscribe(EntityDestroyed.class,   this::onEntityDestroyed);
        eventBus.subscribe(CollisionEvent.class, this::onCollision);
    }
    
    private void onCollision(CollisionEvent event) {
        boolean aIsCar = event.a() instanceof Car;
        boolean bIsCar = event.b() instanceof Car;
        if (!aIsCar && !bIsCar) return;

        GameObject slime = aIsCar ? event.b() : event.a();
        if (slime instanceof SlimeChild child && child.isActive()) {
            score -= CAR_HIT_PENALTY;
            child.markInactive();
        }
    }
 
    private void onCrossingCompleted(CrossingCompleted event) {
        int delta = event.childrenAlive() * POINTS_PER_CHILD;
        if (event.allAlive()) {
            delta += PERFECT_BONUS;
        }
        score                  += delta;
        lastCrossingDelta       = delta;
        lastCrossingWasPerfect  = event.allAlive();
    }
 
    private void onEntityDestroyed(EntityDestroyed event) {
        if (event.entity() instanceof SlimeChild) {
            return; // hook for future extension
        }

        if (event.entity() instanceof SlimeParent) {
            if (score > highScore) {
                highScore = score;
            }
            WaveManager waveManager = ServiceLocator.resolve(WaveManager.class);
            eventBus.publish(new GameOverEvent(score, waveManager.getCurrentWave()));
        }
    }
 
    /** Total accumulated score for this run. */
    public int getScore() {
        return score;
    }
 
    /** All-time in-memory high score across runs. */
    public int getHighScore() {
        return highScore;
    }
 
    /** Points gained on the crossing that just completed. Used by the HUD banner. */
    public int getLastCrossingDelta() {
        return lastCrossingDelta;
    }
 
    /** True if the last crossing was perfect (all children survived). */
    public boolean wasLastCrossingPerfect() {
        return lastCrossingWasPerfect;
    }
 
    /**
     * Resets run state for a new game. High score is preserved across resets
     * and only updated when the parent dies.
     */
    public void reset() {
        score                  = 0;
        lastCrossingDelta      = 0;
        lastCrossingWasPerfect = false;
    }
}
 