package me.arkallic.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import me.arkallic.map.MapData;

import java.io.IOException;

/**
 * The editor application. It is only glue:
 * <pre>
 *   EditorState  - what's selected + the MapData being edited
 *   GridView     - draws the map, turns mouse clicks into edits
 *   EditorUi     - the sidebar; asks us (the Listener) to clear/save/load
 *   MapFile      - JSON in and out, using the same Json + MapData as the game
 * </pre>
 */
public class MapEditor extends ApplicationAdapter implements EditorUi.Listener {

    private static final String MAP_DIR = "maps/";
    private static final String EXTENSION = ".json";

    private final int mapSize;

    private EditorState state;
    private Stage stage;
    private EditorUi ui;
    private GridView grid;

    public MapEditor(int mapSize) {
        this.mapSize = mapSize;
    }

    @Override
    public void create() {
        state = new EditorState(MapOps.create("untitled", mapSize));
        stage = new Stage(new ScreenViewport());
        ui = new EditorUi(stage, state, this);
        grid = new GridView(stage, state);

        // The stage gets each click first; if it isn't on a widget, the grid gets it.
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, grid));

        ui.setStatus("New " + mapSize + "x" + mapSize + " map. Paint something!");
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        ui.layout(stage.getHeight());
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.07f, 0.08f, 0.10f, 1f);
        stage.getViewport().apply();

        grid.render();
        ui.setHover(grid.hoverText());

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    // ------------------------------------------------------------------
    // EditorUi.Listener
    // ------------------------------------------------------------------

    @Override
    public void onClear() {
        MapOps.clear(state.map);
        ui.setStatus("Cleared. Every tile is " + MapOps.DEFAULT_TILE.name() + " and every height is 0.");
    }

    @Override
    public void onSave(String rawName) {
        String name = cleanName(rawName);
        if (name == null) {
            ui.setStatus("Type a file name first.");
            return;
        }
        FileHandle file = Gdx.files.local(MAP_DIR + name + EXTENSION);
        MapData map = state.map;
        if ("untitled".equals(map.name)) {
            map.name = name; // keep descriptive names you already gave a map; only name unnamed ones
        }
        try {
            // Build the whole text first, so a failure can't leave a half-written file.
            file.writeString(MapFile.toJson(map), false, "UTF-8");
            ui.setStatus("Saved " + file.path() + " (" + map.width + "x" + map.height + ")");
        } catch (IOException | GdxRuntimeException e) {
            ui.setStatus("Save failed: " + e.getMessage());
        }
    }

    @Override
    public void onLoad(String rawName) {
        String name = cleanName(rawName);
        if (name == null) {
            ui.setStatus("Type the name of a map to load.");
            return;
        }
        FileHandle file = Gdx.files.local(MAP_DIR + name + EXTENSION);
        if (!file.exists()) {
            ui.setStatus("No such file: " + file.path());
            return;
        }
        try {
            state.map = MapFile.parse(file.readString("UTF-8")); // only replaced if the whole file is valid
            ui.setStatus("Loaded " + file.path() + " (" + state.map.width + "x" + state.map.height + ")");
        } catch (IOException | GdxRuntimeException e) {
            ui.setStatus("Load failed: " + e.getMessage());
        }
    }

    /** Keeps only letters, digits, '_' and '-', so a typed name can never escape the maps folder. */
    private static String cleanName(String raw) {
        String name = raw == null ? "" : raw.trim().replaceAll("[^A-Za-z0-9_-]", "");
        return name.isEmpty() ? null : name;
    }

    @Override
    public void dispose() {
        grid.dispose();
        ui.dispose();
        stage.dispose();
    }
}
