package com.lobsterchops.brainlessgamejam.core;

public enum GameStage {

    PRE_ALPHA("Pre-Alpha - Development Build", "-dev"),
    ALPHA("Alpha", "-alpha"),
    BETA("Beta", "-beta"),
    RELEASE_CANDIDATE("Release Candidate", "-rc"),
    RELEASE("Release", "");

    private final String displayName;
    private final String versionSuffix;

    GameStage(String displayName, String versionSuffix) {
        this.displayName = displayName;
        this.versionSuffix = versionSuffix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVersionSuffix() {
        return versionSuffix;
    }
}