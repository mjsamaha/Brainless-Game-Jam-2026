package com.lobsterchops.brainlessgamejam.world;

import com.lobsterchops.brainlessgamejam.entity.SlimeChild;
import com.lobsterchops.brainlessgamejam.event.CrossingCompleted;
import com.lobsterchops.brainlessgamejam.event.EntityDestroyed;
import com.lobsterchops.brainlessgamejam.event.EventBus;

public class ScoreSystem {
	
	private static final int POINTS_PER_CHILD = 100;
	private static final int PERFECT_BONUS = 500;
	
	private int score;
	
	private int lastCrossingDelta;
	
	private boolean lastCrossingWasPerfect;
	
	public ScoreSystem(EventBus eventBus) {
		eventBus.subscribe(CrossingCompleted.class, this::onCrossingCompleted);
        eventBus.subscribe(EntityDestroyed.class,   this::onEntityDestroyed);

	}
	
	private void onCrossingCompleted(CrossingCompleted event) {
        int delta = event.childrenAlive() * POINTS_PER_CHILD;
        if (event.allAlive()) {
            delta += PERFECT_BONUS;
        }
        score += delta;
        lastCrossingDelta     = delta;
        lastCrossingWasPerfect = event.allAlive();
    }
	
	private void onEntityDestroyed(EntityDestroyed event) {
        // Future: could apply a small penalty here, or track stats.
        // Currently scoring is only awarded on successful crossing, so
        // losing a child just reduces the crossing bonus — no separate deduction needed.
        if (!(event.entity() instanceof SlimeChild)) return;
        // no-op for now; hook is here for easy extension
    }
	
	/** Total accumulated score for this run. */
    public int getScore() {
        return score;
    }
 
    /** Points gained on the crossing that just completed. Used by the HUD banner. */
    public int getLastCrossingDelta() {
        return lastCrossingDelta;
    }
 
    /** True if the last crossing was perfect (all children survived). */
    public boolean wasLastCrossingPerfect() {
        return lastCrossingWasPerfect;
    }
 
    /** Resets all state for a new run. Called from {@code GameContext.setupNewRun()}. */
    public void reset() {
        score                  = 0;
        lastCrossingDelta      = 0;
        lastCrossingWasPerfect = false;
    }

}
