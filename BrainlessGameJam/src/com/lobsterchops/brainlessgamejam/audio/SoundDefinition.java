package com.lobsterchops.brainlessgamejam.audio;

import java.util.Objects;

public record SoundDefinition(String resourcePath, AudioCategory category, boolean looping, float baseVolume, // 0.0f to
																												// 1.0f
		int maxInstances) {

	public SoundDefinition {
		resourcePath = validateResourcePath(resourcePath);
		Objects.requireNonNull(category, "AudioCategory cannot be null");
		baseVolume = clamp01(baseVolume);
		maxInstances = Math.max(1, maxInstances);
	}

	public static SoundDefinition music(String resourcePath, float baseVolume) {
		return new SoundDefinition(resourcePath, AudioCategory.MUSIC, true, baseVolume, 1);
	}

	public static SoundDefinition sfx(String resourcePath, float baseVolume, int maxInstances) {
		return new SoundDefinition(resourcePath, AudioCategory.SFX, false, baseVolume, maxInstances);
	}

	private static float clamp01(float value) {
		if (value < 0f)
			return 0f;
		if (value > 1f)
			return 1f;
		return value;
	}

	private static String validateResourcePath(String resourcePath) {
		Objects.requireNonNull(resourcePath, "Resource path cannot be null");
		String trimmed = resourcePath.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("Resource path cannot be empty");
		}
		if (!trimmed.startsWith("/")) {
			throw new IllegalArgumentException("Resource path must start with '/'");
		}
		return trimmed;
	}

}