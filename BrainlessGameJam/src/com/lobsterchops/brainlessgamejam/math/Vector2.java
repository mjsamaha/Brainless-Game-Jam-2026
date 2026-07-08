package com.lobsterchops.brainlessgamejam.math;

public record Vector2(float x, float y) {

	public static final Vector2 ZERO = new Vector2(0, 0);

	public Vector2 add(Vector2 other) {
		return new Vector2(x + other.x, y + other.y);
	}

	public Vector2 subtract(Vector2 other) {
		return new Vector2(x - other.x, y - other.y);
	}

	public Vector2 multiply(float scalar) {
		return new Vector2(x * scalar, y * scalar);
	}

	public float length() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public Vector2 normalized() {
		float len = length();
		if (len == 0) {
			return ZERO;
		}
		return new Vector2(x / len, y / len);
	}

	public float distanceTo(Vector2 other) {
		return subtract(other).length();
	}

	public Vector2 directionTo(Vector2 target) {
		return target.subtract(this).normalized();
	}

	public Vector2 clamp(float minX, float minY, float maxX, float maxY) {
		float clampedX = Math.max(minX, Math.min(x, maxX));
		float clampedY = Math.max(minY, Math.min(y, maxY));
		return new Vector2(clampedX, clampedY);
	}

}