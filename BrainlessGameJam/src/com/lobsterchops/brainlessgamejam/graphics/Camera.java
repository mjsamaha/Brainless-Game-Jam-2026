package com.lobsterchops.brainlessgamejam.graphics;

import com.lobsterchops.brainlessgamejam.config.ScreenConfig;
import com.lobsterchops.brainlessgamejam.math.Bounds;
import com.lobsterchops.brainlessgamejam.math.Vector2;

public class Camera {

	private float offsetX = 0f;
	private float offsetY = 0f;

	private final int viewWidth;
	private final int viewHeight;

	private long shakeRemainingNanos = 0L;
	private float shakeMagnitude = 0f;
	private float shakeOffsetX = 0f;
	private float shakeOffsetY = 0f;
	
	private float zoom = 1f;

	/** Creates a camera sized to the full screen. */
	public Camera() {
		this(ScreenConfig.WIDTH, ScreenConfig.HEIGHT);
	}

	/**
	 * Creates a camera with an explicit viewport size.
	 *
	 * @param viewWidth  viewport width in pixels
	 * @param viewHeight viewport height in pixels
	 */
	public Camera(int viewWidth, int viewHeight) {
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
	}

	/**
	 * Centres the camera on a world-space position. Call once per tick before the render pass.
	 *
	 * @param worldX centre target, world X in pixels
	 * @param worldY centre target, world Y in pixels
	 */
	public void follow(float worldX, float worldY) {
	    offsetX = worldX - (viewWidth / zoom) / ScreenConfig.CENTER_DIVISOR;
	    offsetY = worldY - (viewHeight / zoom) / ScreenConfig.CENTER_DIVISOR;
	}

	/** Convenience overload for {@link Vector2}. */
	public void follow(Vector2 position) {
		follow(position.x(), position.y());
	}

	/**
	 * Clamps the camera so it never reveals space outside the world bounds. Call
	 * <em>after</em> {@link #follow} each tick.
	 *
	 * @param worldMinX left edge of the world in pixels (usually 0)
	 * @param worldMinY top edge of the world in pixels (usually 0)
	 * @param worldMaxX right edge of the world in pixels (e.g. mapWidthTiles * tileSize)
	 * @param worldMaxY bottom edge of the world in pixels
	 */
	public void clamp(float worldMinX, float worldMinY, float worldMaxX, float worldMaxY) {
		float maxOffsetX = worldMaxX - viewWidth;
		float maxOffsetY = worldMaxY - viewHeight;
		offsetX = Math.max(worldMinX, Math.min(offsetX, maxOffsetX));
		offsetY = Math.max(worldMinY, Math.min(offsetY, maxOffsetY));
	}

	/**
	 * Starts a camera shake effect. A subsequent call while a shake is already
	 * active will reset the shake with the new parameters (last caller wins).
	 *
	 * @param durationNanos total duration of the shake in nanoseconds
	 * @param magnitude     maximum pixel displacement per axis (e.g. 4.0 for a
	 *                      subtle hit, 8.0 for a heavy impact)
	 */
	public void shake(long durationNanos, float magnitude) {
		this.shakeRemainingNanos = durationNanos;
		this.shakeMagnitude = magnitude;
	}

	/**
	 * Advances the shake timer. Must be called once per tick when a shake is
	 * active. Safe to call unconditionally — is a no-op when no shake is running.
	 *
	 * @param elapsedNanos nanoseconds since the last tick
	 */
	public void update(long elapsedNanos) {
		if (shakeRemainingNanos <= 0L) {
			shakeOffsetX = 0f;
			shakeOffsetY = 0f;
			return;
		}
		shakeRemainingNanos -= elapsedNanos;
		// Simple pseudo-random displacement using time-based trig mixing.
		float t = shakeRemainingNanos / 1_000_000f; // millis, arbitrary time base
		shakeOffsetX = (float) (Math.sin(t * 0.37) * Math.cos(t * 0.53)) * shakeMagnitude;
		shakeOffsetY = (float) (Math.cos(t * 0.41) * Math.sin(t * 0.67)) * shakeMagnitude;
	}

	public int toScreenX(float worldX) {
	    return (int) ((worldX - offsetX + shakeOffsetX) * zoom);
	}

	public int toScreenY(float worldY) {
	    return (int) ((worldY - offsetY + shakeOffsetY) * zoom);
	}

	public float getZoom() {
	    return zoom;
	}

	public void setZoom(float zoom) {
	    this.zoom = Math.max(0.1f, zoom); // guard against zero/negative
	}

	/**
	 * Returns the visible world region this frame, in world-space pixel coordinates.
	 * Use this to skip rendering of objects outside the viewport.
	 *
	 * <pre>
	 * Bounds view = camera.getViewBounds();
	 * for (Entity e : gameSystem.getObjects()) {
	 * 	if (view.intersects(e.getBounds())) {
	 * 		// render it
	 * 	}
	 * }
	 * </pre>
	 *
	 * @return the visible world bounds
	 */
	public Bounds getViewBounds() {
		return new Bounds(offsetX - shakeOffsetX, offsetY - shakeOffsetY, viewWidth, viewHeight);
	}

	public int getOffsetX() {
		return (int) offsetX;
	}

	public int getOffsetY() {
		return (int) offsetY;
	}

	public int getViewWidth() {
		return viewWidth;
	}

	public int getViewHeight() {
		return viewHeight;
	}

	public boolean isShaking() {
		return shakeRemainingNanos > 0L;
	}
	
	

}