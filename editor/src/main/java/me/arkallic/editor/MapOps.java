package me.arkallic.editor;

import me.arkallic.map.MapData;
import me.arkallic.map.tile.TileType;

/**
 * Small helpers for working with the game's MapData.
 * <p>
 * MapData (in core) is a plain bag of public fields, because that is what libGDX's
 * Json reads and writes. It stores tiles as Strings ("GRASS") and heights as floats,
 * both indexed [y][x]: y is the row (0 = south edge), x is the column (0 = west edge).
 * These helpers add the things it doesn't have: bounds checks and TileType lookups.
 * We keep them here, in the editor, so the game's own class stays untouched.
 */
final class MapOps {

    /** What "erase" turns a tile into, and what a new map is filled with. */
    static final TileType DEFAULT_TILE = TileType.GRASS;

    private MapOps() {
    }

    /** A new, flat, all-GRASS map. */
    static MapData create(String name, int size) {
        MapData map = new MapData();
        map.version = MapFile.VERSION;
        map.name = name;
        map.width = size;
        map.height = size;
        map.tiles = new String[size][size];
        map.heights = new float[size][size];
        clear(map);
        return map;
    }

    /** Every tile back to the default tile, every height back to 0. */
    static void clear(MapData map) {
        for (int y = 0; y < map.height; y++) {
            for (int x = 0; x < map.width; x++) {
                map.tiles[y][x] = DEFAULT_TILE.name();
                map.heights[y][x] = 0f;
            }
        }
    }

    static boolean inBounds(MapData map, int x, int y) {
        return x >= 0 && y >= 0 && x < map.width && y < map.height;
    }

    /** Returns null when (x, y) is outside the map. */
    static TileType getTile(MapData map, int x, int y) {
        return inBounds(map, x, y) ? TileType.valueOf(map.tiles[y][x]) : null;
    }

    /** Out-of-bounds writes are ignored, so a brush can hang over the edge safely. */
    static void setTile(MapData map, int x, int y, TileType type) {
        if (inBounds(map, x, y)) {
            map.tiles[y][x] = type.name();
        }
    }

    /** Returns 0 outside the map. */
    static float getHeight(MapData map, int x, int y) {
        return inBounds(map, x, y) ? map.heights[y][x] : 0f;
    }

    static void setHeight(MapData map, int x, int y, float height) {
        if (inBounds(map, x, y)) {
            map.heights[y][x] = height;
        }
    }
}
