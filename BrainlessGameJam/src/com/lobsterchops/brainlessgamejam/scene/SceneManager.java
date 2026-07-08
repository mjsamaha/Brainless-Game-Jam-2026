package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Graphics2D;
import java.util.logging.Logger;

import com.lobsterchops.brainlessgamejam.entity.UpdateContext;

/**
 * <h4>Owns the currently active {@link Scene} and switches between scenes.</h4>
 * <p>
 * Not yet wired into the update/render loop — see GameUpdater / RenderPipeline.
 * Construct with an initial scene, then call {@link #switchTo(Scene)} to
 * transition.
 * </p>
 */
public class SceneManager {

	private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());

	private Scene currentScene;

	public SceneManager(Scene initialScene) {
		if (initialScene != null)
			switchTo(initialScene);
	}

	public void switchTo(Scene nextScene) {
		if (nextScene == null) {
			LOGGER.warning("Attempted to switch to a null scene; ignoring.");
			return;
		}

		if (currentScene != null) {
			currentScene.exit();
		}

		currentScene = nextScene;
		currentScene.enter();
	}

	public void update(UpdateContext context) {
		if (currentScene != null) {
			currentScene.update(context);
		}
	}

	public void render(Graphics2D g2) {
		if (currentScene != null) {
			currentScene.render(g2);
		}
	}

	public Scene getCurrentScene() {
		return currentScene;
	}

}