package com.lobsterchops.brainlessgamejam.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;


public final class ResourceLoader {

    private static final Logger LOGGER = Logger.getLogger(ResourceLoader.class.getName());

    private ResourceLoader() {}

    /**
     * Loads a PNG from the classpath.
     *
     * @param path  e.g. "/sprites/player.png"
     * @return the loaded image, or null if not found / unreadable
     */
    public static BufferedImage loadImage(String path) {
        URL url = ResourceLoader.class.getResource(path);

        if (url == null) {
            LOGGER.warning("Sprite not found on classpath: " + path);
            return null;
        }

        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read sprite: " + path, e);
            return null;
        }
    }
}