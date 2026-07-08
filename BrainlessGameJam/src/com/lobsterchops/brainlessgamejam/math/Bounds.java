package com.lobsterchops.brainlessgamejam.math;

public record Bounds(float x, float y, float width, float height) {

	public boolean intersects(Bounds other) {
		return x < other.x + other.width && x + width > other.x && y < other.y + other.height && y + height > other.y;
	}

	public boolean contains(Vector2 point) {
		return point.x() >= x && point.x() <= x + width && point.y() >= y && point.y() <= y + height;
	}

	public Vector2 center() {
		return new Vector2(x + width / 2f, y + height / 2f);
	}

	public static Bounds fromCenter(Vector2 center, float width, float height) {
		return new Bounds(center.x() - width / 2f, center.y() - height / 2f, width, height);
	}

}
