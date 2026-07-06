package com.lobsterchops.brainlessgamejam.audio;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class AudioCatalog {

	private static final Map<AudioType, SoundDefinition> DEFINITIONS;

	private static final boolean STRICT_VALIDATION = false; // flip to true when all sounds are registered

	static {
		EnumMap<AudioType, SoundDefinition> defs = new EnumMap<>(AudioType.class);

		// register sounds here when you have assets:
		// register(defs, SoundType.NAVIGATION_CLICK,
		// SoundDefinition.sfx("/audio/click.wav", 0.75f, 3));
		
		register(defs, AudioType.GAMEPLAY_MUSIC, SoundDefinition.music("/audio/preloader_music.wav", 1.0f));

		

		
		

		if (STRICT_VALIDATION) {
			for (AudioType t : AudioType.values()) {
				if (!defs.containsKey(t)) {
					throw new IllegalStateException("SoundType " + t + " is not registered in AudioCatalog.");
				}
			}
		}

		DEFINITIONS = Collections.unmodifiableMap(defs);
	}

	private AudioCatalog() {
	}

	public static SoundDefinition get(AudioType type) {
		SoundDefinition def = DEFINITIONS.get(type);
		if (def == null) {
			throw new IllegalArgumentException("SoundType " + type + " is not registered in AudioCatalog");
		}
		return def;
	}

	public static Map<AudioType, SoundDefinition> definitions() {
		return DEFINITIONS;
	}

	public static boolean has(AudioType type) {
		return DEFINITIONS.containsKey(type);
	}

	public static void register(Map<AudioType, SoundDefinition> defs, AudioType type, SoundDefinition def) {
		if (defs.put(type, def) != null) {
			throw new IllegalStateException("SoundType " + type + " is already registered in AudioCatalog");
		}
	}

}