package com.lobsterchops.brainlessgamejam.world;

import com.lobsterchops.brainlessgamejam.graphics.Gfx;

public enum TileType {

    GRASS          ('G', Gfx.TILE_GRASS),
    ROAD           ('R', Gfx.TILE_ROAD),
    ROAD_WHITE     ('S', Gfx.TILE_ROAD_WHITE_STRIP),
    ROAD_YELLOW    ('Y', Gfx.TILE_ROAD_YELLOW_STRIP),
    WATER          ('W', Gfx.TILE_WATER);

    public final char symbol;
    public final String spritePath;

    TileType(char symbol, String spritePath) {
        this.symbol = symbol;
        this.spritePath = spritePath;
    }

    public static TileType fromChar(char c) {
        for (TileType t : values()) {
            if (t.symbol == c) return t;
        }
        throw new IllegalArgumentException("Unknown tile symbol: '" + c + "'");
    }
}