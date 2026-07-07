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

	private static final int POINTS_PER_CHILD = 100;
	private static final int PERFECT_BONUS = 500;
	private static int MAX_LIVES = 10;
	private static int WAVE_COMPLETE_BONUS_LIVES = 2;

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
		int delta = event.childrenAlive() * POINTS_PER_CHILD;
		if (event.allAlive()) {
			delta += PERFECT_BONUS;
		}
		score += delta;
		lastCrossingDelta = delta;
		lastCrossingWasPerfect = event.allAlive();
		
		MAX_LIVES += event.childrenAlive() / 2; // Increase max lives based on children alive
		// unless we add+= WAVE_COMPLETE_BONUS_LIVES
		
	}

	private void onEntityDestroyed(EntityDestroyed event) {
		if (event.entity() instanceof SlimeChild) {
			lives--;
			if (lives <= 0) {
				triggerGameOver();
			}
			return;
		}
		if (event.entity() instanceof SlimeParent) {
			lives--;
			if (lives <= 0) {
				triggerGameOver();
			}
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
		lives = MAX_LIVES;
		lastCrossingDelta = 0;
		lastCrossingWasPerfect = false;
	}
}