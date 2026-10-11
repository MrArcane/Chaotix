package me.arkallic.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import me.arkallic.map.MapData;
import me.arkallic.map.tile.TileType;

import java.io.IOException;

/**
 * Reads and writes the map JSON, using the very same libGDX Json class and the very
 * same MapData class as the game's GameMap.loadMap(), so a file saved here loads in
 * the game by construction.
 * <p>
 * Reading is strict on purpose. It rejects anything the game would choke on (wrong
 * row length, a tile name that isn't a TileType), and anything the editor doesn't
 * know about (a newer version, extra fields), because quietly dropping unknown data
 * when you re-save would destroy it.
 */
final class MapFile {

    /** Version this editor writes. Bump together with MapData when the format changes. */
    static final int VERSION = 2;

    /** The most tiles per side the editor will create or accept. */
    static final int MAX_SIZE = 64;

    private MapFile() {
    }

    // ------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------

    static MapData parse(String text) throws IOException {
        MapData map;
        try {
            map = new Json().fromJson(MapData.class, text);
        } catch (RuntimeException e) { // SerializationException, bad JSON, ...
            throw new IOException(describe(e), e);
        }
        if (map == null) {
            throw new IOException("The file is empty");
        }
        validate(map);
        return map;
    }

    private static void validate(MapData map) throws IOException {
        if (map.version > VERSION) {
            throw new IOException("Map is version " + map.version + " but this editor only understands up to " + VERSION);
        }
        if (map.width < 1 || map.height < 1 || map.width > MAX_SIZE || map.height > MAX_SIZE) {
            throw new IOException("Map size must be 1-" + MAX_SIZE + " on each side, but it is " + map.width + "x" + map.height);
        }
        if (map.name == null || map.name.trim().isEmpty()) {
            map.name = "untitled";
        }

        if (map.tiles == null || map.tiles.length != map.height) {
            throw new IOException("'tiles' must have " + map.height + " rows (one per y)");
        }
        for (int y = 0; y < map.height; y++) {
            String[] row = map.tiles[y];
            if (row == null || row.length != map.width) {
                throw new IOException("'tiles' row y=" + y + " must have " + map.width + " entries");
            }
            for (int x = 0; x < map.width; x++) {
                try {
                    TileType.valueOf(row[x]); // exactly what GameMap does, so same strictness
                } catch (IllegalArgumentException | NullPointerException e) {
                    throw new IOException("Unknown tile type '" + row[x] + "' at x=" + x + ", y=" + y);
                }
            }
        }

        if (map.heights == null) { // older files had no heights: treat as flat
            map.heights = new float[map.height][map.width];
        } else {
            if (map.heights.length != map.height) {
                throw new IOException("'heights' must have " + map.height + " rows (one per y)");
            }
            for (int y = 0; y < map.height; y++) {
                if (map.heights[y] == null || map.heights[y].length != map.width) {
                    throw new IOException("'heights' row y=" + y + " must have " + map.width + " entries");
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // WRITE
    // ------------------------------------------------------------------

    /**
     * Pretty JSON with one row per line (row 0 = y 0 = the south edge first), so you
     * can read the map straight off the file and diff it sensibly.
     */
    static String toJson(MapData map) throws IOException {
        map.version = VERSION;
        try {
            Json json = new Json();
            json.setUsePrototypes(false); // always write every field, even ones equal to the default
            json.setOutputType(JsonWriter.OutputType.json);
            String compact = json.toJson(map, MapData.class); // knownType given, so no "class" tag is added

            // A row that fits within this many columns stays on one line.
            int rowColumns = map.width * 16 + 16;
            JsonValue tree = new JsonReader().parse(compact);
            return tree.prettyPrint(JsonWriter.OutputType.json, rowColumns) + "\n";
        } catch (RuntimeException e) {
            throw new IOException(describe(e), e);
        }
    }

    /** The deepest, shortest error message (libGDX nests them with long traces). */
    private static String describe(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        String message = t.getMessage() == null ? t.toString() : t.getMessage();
        int newline = message.indexOf('\n');
        return newline > 0 ? message.substring(0, newline) : message;
    }
}
