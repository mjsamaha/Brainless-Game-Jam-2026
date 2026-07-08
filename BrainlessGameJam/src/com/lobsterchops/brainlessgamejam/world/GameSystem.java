package com.lobsterchops.brainlessgamejam.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lobsterchops.brainlessgamejam.config.GameLoopConfig;
import com.lobsterchops.brainlessgamejam.entity.GameObject;
import com.lobsterchops.brainlessgamejam.entity.Renderable;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.world.common.GameState;

public class GameSystem {

	private final List<GameObject> objects = new ArrayList<>();
	private final List<GameObject> pendingObjects = new ArrayList<>();
	private final CollisionSystem collisionSystem;

	private GameState state = GameState.MENU;

	private long tick;
	private long elapsedMillis;

	public GameSystem(EventBus eventBus) {
		this.collisionSystem = new CollisionSystem(eventBus);
	}

	public void update() {
		if (state != GameState.PLAYING) {
			return;
		}

		beginUpdate();
		updateTime();
		updateMetaSystems();

		UpdateContext context = createContext();

		updateObjects(context);
		updateSystems(context);

		endUpdate();
	}

	public void addObject(GameObject object) {
		if (object != null) {
			pendingObjects.add(object);
		}
	}

	public void clear() {
		tick = 0;
		elapsedMillis = 0;
		objects.clear();
		pendingObjects.clear();
		state = GameState.PLAYING;
	}

	private void beginUpdate() {
		flushPendingObjects();
	}

	private void updateTime() {
		tick++;
		elapsedMillis += Math.round(GameLoopConfig.MILLIS_PER_SECOND / GameLoopConfig.TARGET_FPS);
	}

	private void updateMetaSystems() {
	}

	private UpdateContext createContext() {
		return UpdateContext.fixed(this, tick, elapsedMillis);
	}

	private void updateObjects(UpdateContext context) {
		for (GameObject object : objects) {
			if (object.isActive()) {
				object.update(context);
			}
		}
	}

	private void updateSystems(UpdateContext context) {
		collisionSystem.resolve(objects);
	}

	private void endUpdate() {
		flushPendingObjects();
		removeInactiveObjects();
	}

	private void flushPendingObjects() {
		if (!pendingObjects.isEmpty()) {
			objects.addAll(pendingObjects);
			pendingObjects.clear();
		}
	}

	private void removeInactiveObjects() {
		objects.removeIf(object -> !object.isActive());
	}

	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public long getTick() {
		return tick;
	}

	public long getElapsedMillis() {
		return elapsedMillis;
	}

	public CollisionSystem getCollisionSystem() {
		return collisionSystem;
	}

	public List<GameObject> getObjects() {
		return Collections.unmodifiableList(objects);
	}

	public List<Renderable> getRenderableObjects() {
		List<Renderable> renderables = new ArrayList<>();
		for (GameObject object : objects) {
			if (object instanceof Renderable renderable && object.isActive()) {
				renderables.add(renderable);
			}
		}
		return renderables;
	}
}