package com.lobsterchops.brainlessgamejam.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public final class ScoreRepository {
	
	private static final String DIR  = System.getProperty("user.home") + "/.brainlessgamejam";
    private static final String PATH = DIR + "/highscore.dat";

    private ScoreRepository() {}

    public static int load() {
        File file = new File(PATH);
        if (!file.exists()) return 0;

        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            int score = in.readInt();
            Logger.info("High score loaded: " + score);
            return score;
        } catch (Exception e) {
            Logger.error("Failed to load high score", e);
            return 0;
        }
    }

    public static void save(int score) {
        File dir = new File(DIR);
        if (!dir.exists()) dir.mkdirs();

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(PATH))) {
            out.writeInt(score);
        } catch (Exception e) {
            Logger.error("Failed to save high score", e);
        }
    }
}
