package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
 
/**
 * <h4>A self-contained game screen.</h4>
 * <p>Examples: main menu, the playing scene, a pause overlay, game over screen.</p>
 * <p>Implementations own whatever state/objects belong to that screen. The
 * {@link SceneManager} is responsible for calling these methods in order and
 * for handing off between scenes via {@link #enter()} / {@link #exit()}.</p>
 */
public interface Scene {
 
	/** Called once when this scene becomes the active scene. */
	default void enter() {
	}
 
	/** Called once per tick while this scene is active. */
	void update(UpdateContext context);
 
	/** Called once per repaint while this scene is active. */
	void render(Graphics2D g2);
 
	/** Called once when this scene stops being the active scene. */
	default void exit() {
	}
 
}