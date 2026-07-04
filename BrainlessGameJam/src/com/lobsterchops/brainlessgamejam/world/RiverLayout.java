package com.lobsterchops.brainlessgamejam.world;


import java.util.ArrayList;
import java.util.List;
 
import com.lobsterchops.brainlessgamejam.entity.Log;
import com.lobsterchops.brainlessgamejam.math.Vector2;
 
public class RiverLayout {
 
    private static final int   BASE_LOGS_PER_ROW   = 2;
    private static final float BASE_SPEED           = 1.2f;
    private static final float SPEED_SCALE_PER_WAVE = 1.1f;
    private static final int   MAX_RIVER_ROWS       = 5;
 
    private final TileMap tileMap;
 
    public RiverLayout(TileMap tileMap) {
        this.tileMap = tileMap;
    }
 
    /**
     * Scans the tile map for water rows, then spawns logs into the game system
     * scaled to the given wave number (1-indexed).
     */
    public void spawnLogs(int wave, GameSystem gameSystem) {
        List<Float> riverYCentres = findRiverRowCentres();
 
        int activeRows = Math.min(wave + 1, Math.min(riverYCentres.size(), MAX_RIVER_ROWS));
        int logsPerRow = BASE_LOGS_PER_ROW + (wave - 1);
        float speed = BASE_SPEED * (float) Math.pow(SPEED_SCALE_PER_WAVE, wave - 1);
 
        float worldWidth = tileMap.worldWidth();
 
        for (int rowIndex = 0; rowIndex < activeRows; rowIndex++) {
            float rowY = riverYCentres.get(rowIndex);
 
            // Alternate row directions: even rows go right, odd go left
            float rowSpeed = (rowIndex % 2 == 0) ? speed : -speed;
 
            // Stagger starting positions evenly so gaps exist between logs
            float spacing = worldWidth / logsPerRow;
 
            for (int logIndex = 0; logIndex < logsPerRow; logIndex++) {
                float startX = spacing * logIndex + spacing / 2f;
                gameSystem.addObject(new Log(new Vector2(startX, rowY), rowSpeed));
            }
        }
    }
 
    /**
     * Marks all active Log objects inactive — call before spawning a new wave.
     */
    public void clearLogs(GameSystem gameSystem) {
        gameSystem.getObjects().stream()
                .filter(o -> o instanceof Log)
                .map(o -> (Log) o)
                .forEach(log -> log.markInactive());
    }
 
    /**
     * Returns the world-space Y bounds (top, bottom) of every contiguous water
     * zone in the map. Used by entities to check if they're standing in water.
     */
    public List<float[]> getRiverBounds() {
        List<float[]> bounds = new ArrayList<>();
        boolean inRiver = false;
        float riverTop = 0;
 
        for (int row = 0; row < tileMap.rows; row++) {
            boolean isWater = rowContains(row, TileType.WATER);
            if (isWater && !inRiver) {
                inRiver = true;
                riverTop = row * TileMap.TILE_SIZE;
            } else if (!isWater && inRiver) {
                inRiver = false;
                float riverBottom = row * TileMap.TILE_SIZE;
                bounds.add(new float[]{ riverTop, riverBottom });
            }
        }
 
        // Handle river that runs to the bottom of the map
        if (inRiver) {
            bounds.add(new float[]{ riverTop, tileMap.worldHeight() });
        }
 
        return bounds;
    }
 
    private List<Float> findRiverRowCentres() {
        List<Float> centres = new ArrayList<>();
        for (int row = 0; row < tileMap.rows; row++) {
            if (rowContains(row, TileType.WATER)) {
                centres.add(row * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f);
            }
        }
        return centres;
    }
 
    private boolean rowContains(int row, TileType type) {
        for (int col = 0; col < tileMap.cols; col++) {
            if (tileMap.get(col, row) == type) return true;
        }
        return false;
    }
}