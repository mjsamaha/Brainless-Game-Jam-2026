package com.lobsterchops.brainlessgamejam.entity;

import com.lobsterchops.brainlessgamejam.config.GameLoopConfig;
import com.lobsterchops.brainlessgamejam.world.GameSystem;

public record UpdateContext(
		GameSystem gameSystem,
		long tick,
		long elapsedMillis,
		float fixedDeltaSeconds
		) {
	
	public static UpdateContext fixed(GameSystem world, long tick, long elapsedMillis) {
		return new UpdateContext(
				world,
				tick,
				elapsedMillis,
				1f / GameLoopConfig.TARGET_FPS
		);
	}

}
