package com.lobsterchops.brainlessgamejam.audio;

import javax.sound.sampled.FloatControl;

public final class AudioMath {
	
	private static final float MIN_LINEAR = 0.0001f; // -80 dB
	
	private AudioMath() {
		// Prevent instantiation
	}
	
	public static float clamp01(float value) {
		if (value < 0f) return 0f;
		if (value > 1f) return 1f;
		return value;
	}

	public static float linearToDb(float linear) {
		float safe = Math.max(MIN_LINEAR, clamp01(linear));
		return (float) (20.0 * Math.log10(safe));
	}

	public static float toControlDb(float linear, FloatControl control) {
		float db = linearToDb(linear);
		if (db < control.getMinimum()) return control.getMinimum();
		if (db > control.getMaximum()) return control.getMaximum();
		return db;
	}

}
