package com.lobsterchops.brainlessgamejam.world;

import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.GameObject;
import com.lobsterchops.brainlessgamejam.entity.entities.Car;
import com.lobsterchops.brainlessgamejam.entity.entities.SlimeChild;
import com.lobsterchops.brainlessgamejam.entity.entities.SlimeParent;
import com.lobsterchops.brainlessgamejam.event.CollisionEvent;
import com.lobsterchops.brainlessgamejam.event.CrossingCompleted;
import com.lobsterchops.brainlessgamejam.event.EntityDestroyed;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.event.GameOverEvent;

public class ScoreSystem {

	// Constants for scoring and lives
	private static final int POINTS_PER_CHILD = 100;
	
	// Bonus points for a perfect crossing (all children alive)
	private static final int PERFECT_BONUS = 500;
	
	// Starting number of lives for the player
	private static final int STARTING_LIVES = 10;
	
	// Bonus lives awarded for completing a wave
	private static final int WAVE_COMPLETE_BONUS_LIVES = 3;
	
	// Wave number threshold for awarding extra lives
	private static final int LATE_WAVE_THRESHOLD = 3;

	private int score;
	private int highScore;
	private int lives;

	private int lastCrossingDelta;
	private boolean lastCrossingWasPerfect;

	private final EventBus eventBus;

	public ScoreSystem(EventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.subscribe(CrossingCompleted.class, this::onCrossingCompleted);
		eventBus.subscribe(EntityDestroyed.class, this::onEntityDestroyed);
		eventBus.subscribe(CollisionEvent.class, this::onCollision);
	}

	private void onCollision(CollisionEvent event) {
		boolean aIsCar = event.a() instanceof Car;
		boolean bIsCar = event.b() instanceof Car;
		if (!aIsCar && !bIsCar)
			return;
		GameObject slime = aIsCar ? event.b() : event.a();
		if (slime instanceof SlimeChild child && child.isActive()) {
			child.markInactive();
			eventBus.publish(new EntityDestroyed(child));
		}
	}

	private void onCrossingCompleted(CrossingCompleted event) {
	    applyScoreReward(event);
	    applyLivesReward(event);
	}

	private void applyScoreReward(CrossingCompleted event) {
	    int delta = event.childrenAlive() * POINTS_PER_CHILD;
	    if (event.allAlive()) {
	        delta += PERFECT_BONUS;
	    }
	    score += delta;
	    lastCrossingDelta = delta;
	    lastCrossingWasPerfect = event.allAlive();
	}

	private void applyLivesReward(CrossingCompleted event) {
	    if (event.waveNumber() >= LATE_WAVE_THRESHOLD) {
	        lives += WAVE_COMPLETE_BONUS_LIVES * 2 + event.childrenAlive();
	    } else {
	        lives += WAVE_COMPLETE_BONUS_LIVES;
	    }
	}

	private void onEntityDestroyed(EntityDestroyed event) {
	    if (event.entity() instanceof SlimeChild || event.entity() instanceof SlimeParent) {
	        loseLife();
	    }
	}
	
	private void loseLife() {
		lives--;
		if (lives <= 0) {
			triggerGameOver();
		}
	}

	private void triggerGameOver() {
		if (score > highScore) {
			highScore = score;
		}
		WaveManager waveManager = ServiceLocator.resolve(WaveManager.class);
		eventBus.publish(new GameOverEvent(score, waveManager.getCurrentWave()));
	}

	public int getScore() {
		return score;
	}

	public int getHighScore() {
		return highScore;
	}

	public int getLives() {
		return lives;
	}

	public int getLastCrossingDelta() {
		return lastCrossingDelta;
	}

	public boolean wasLastCrossingPerfect() {
		return lastCrossingWasPerfect;
	}

	public void reset() {
	    score = 0;
	    lives = STARTING_LIVES;  
	    lastCrossingDelta = 0;
	    lastCrossingWasPerfect = false;
	}
}