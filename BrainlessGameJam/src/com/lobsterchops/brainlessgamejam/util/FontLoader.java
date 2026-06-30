package com.lobsterchops.brainlessgamejam.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FontLoader {

    private static final Logger LOGGER = Logger.getLogger(FontLoader.class.getName());

    private FontLoader() {}

    /**
     * Loads a TrueType font from the classpath.
     *
     * @param path  e.g. "/fonts/myfont.ttf"
     * @param size  point size
     * @return the loaded Font, or a plain fallback if not found / unreadable
     */
    public static Font load(String path, float size) {
        InputStream stream = FontLoader.class.getResourceAsStream(path);

        if (stream == null) {
            LOGGER.warning("Font not found on classpath: " + path);
            return fallback(size);
        }

        try (stream) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            return font.deriveFont(size);
        } catch (FontFormatException | IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load font: " + path, e);
            return fallback(size);
        }
    }

    /**
     * Loads and derives a styled variant (e.g. Font.BOLD).
     *
     * @param path  e.g. "/fonts/myfont.ttf"
     * @param style e.g. Font.BOLD, Font.PLAIN
     * @param size  point size
     */
    public static Font load(String path, int style, float size) {
        return load(path, size).deriveFont(style, size);
    }

    private static Font fallback(float size) {
        LOGGER.warning("Falling back to system Monospaced font.");
        return new Font(Font.MONOSPACED, Font.PLAIN, (int) size);
    }

}