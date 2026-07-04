package com.lobsterchops.brainlessgamejam;

import com.lobsterchops.brainlessgamejam.core.GameStage;

public final class Version {

	public static final String TITLE = "Brainless Game Jam";
	public static final String YEAR = "2026";
	public static final String VERSION = "0.0.0";

	public static final GameStage GAME_STAGE_ENUM = GameStage.PRE_ALPHA;

	private Version() {
	}

	public static String getWindowTitle() {
		return TITLE + " " + YEAR + " - v" + VERSION + GAME_STAGE_ENUM.getVersionSuffix() + " ("
				+ BuildInfo.getCommitHash() + ")" + " [" + BuildInfo.getBuildTime() + "]";
	}

	public static String getDebugTitle() {
		return TITLE + " " + YEAR + " v" + VERSION;
	}
}
