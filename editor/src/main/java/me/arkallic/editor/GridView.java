package me.arkallic.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import me.arkallic.map.data.MapData;
import me.arkallic.map.tile.TileType;

/**
 * Draws the map as a flat top-down grid and turns mouse input into edits.
 * <p>
 * Orientation matches the game: x grows to the right (east), y grows UP the
 * screen (north). Tile (0, 0) is the bottom-left corner.
 * <p>
 * It only ever touches state.map, never the game's World/Player/NPCs.
 */
final class GridView extends InputAdapter implements Disposable {

    private static final float MARGIN = 20f;
    private static final int MAJOR_LINE_EVERY = 8;

    private final Stage stage;
    private final EditorState state;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Vector2 mouse = new Vector2();

    // Where the grid sits on screen. Recomputed every frame, so resizing "just works".
    private float cell;   // size of one tile in pixels
    private float left;   // x of the grid's left edge
    private float bottom; // y of the grid's bottom edge

    private int dragButton = -1;
    private int lastX;
    private int lastY;

    GridView(Stage stage, EditorState state) {
        this.stage = stage;
        this.state = state;
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    void render() {
        MapData map = state.map;
        layout(map);

        shapes.setProjectionMatrix(stage.getCamera().combined);

        // 1. the tiles
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                shapes.setColor(map.get(x, y).color());
                shapes.rect(left + x * cell, bottom + y * cell, cell, cell);
            }
        }
        shapes.end();

        // 2. lines on top (alpha needs blending switched on)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Line);

        float w = map.getWidth() * cell;
        float h = map.getHeight() * cell;

        if (cell >= 6) { // thin lines between every tile, only when they're big enough to see
            shapes.setColor(0f, 0f, 0f, 0.25f);
            for (int x = 0; x <= map.getWidth(); x++) {
                shapes.line(left + x * cell, bottom, left + x * cell, bottom + h);
            }
            for (int y = 0; y <= map.getHeight(); y++) {
                shapes.line(left, bottom + y * cell, left + w, bottom + y * cell);
            }
        }

        shapes.setColor(0f, 0f, 0f, 0.6f); // darker line every 8 tiles, to help you count
        for (int x = 0; x <= map.getWidth(); x += MAJOR_LINE_EVERY) {
            shapes.line(left + x * cell, bottom, left + x * cell, bottom + h);
        }
        for (int y = 0; y <= map.getHeight(); y += MAJOR_LINE_EVERY) {
            shapes.line(left, bottom + y * cell, left + w, bottom + y * cell);
        }

        shapes.setColor(1f, 1f, 1f, 0.8f); // outline of the whole map
        shapes.rect(left, bottom, w, h);

        drawBrushPreview(map);

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /** White square showing exactly which tiles a click would change. */
    private void drawBrushPreview(MapData map) {
        int hx = cellX(mouseX());
        int hy = cellY(mouseY());
        if (!map.inBounds(hx, hy)) {
            return;
        }
        int size = state.tool == Tool.PAINT ? state.brush : 1;
        int x0 = hx - size / 2;
        int y0 = hy - size / 2;
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.rect(left + x0 * cell, bottom + y0 * cell, size * cell, size * cell);
    }

    /** "x, y: TILE_NAME" for the tile under the mouse, or "" when the mouse is elsewhere. */
    String hoverText() {
        MapData map = state.map;
        int hx = cellX(mouseX());
        int hy = cellY(mouseY());
        TileType type = map.get(hx, hy);
        return type == null ? "" : hx + ", " + hy + ": " + type.name();
    }

    // ------------------------------------------------------------------
    // Mouse input
    // ------------------------------------------------------------------

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT && button != Input.Buttons.RIGHT) {
            return false;
        }
        stage.unfocusAll(); // clicking the map takes the keyboard away from the file-name box

        toStage(screenX, screenY);
        int x = cellX(mouse.x);
        int y = cellY(mouse.y);
        if (cell <= 0 || !state.map.inBounds(x, y)) {
            return false;
        }

        dragButton = button;
        lastX = x;
        lastY = y;

        TileType type = typeFor(button);
        if (state.tool == Tool.FILL) {
            Painter.floodFill(state.map, x, y, type);
        } else {
            Painter.stamp(state.map, x, y, state.brush, type);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (dragButton < 0 || state.tool != Tool.PAINT) {
            return false; // fill happens once per click, never while dragging
        }
        toStage(screenX, screenY);
        int x = cellX(mouse.x);
        int y = cellY(mouse.y);
        Painter.line(state.map, lastX, lastY, x, y, state.brush, typeFor(dragButton));
        lastX = x;
        lastY = y;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == dragButton) {
            dragButton = -1;
            return true;
        }
        return false;
    }

    /** Left button paints the selected tile. Right button "erases" back to the default tile. */
    private TileType typeFor(int button) {
        return button == Input.Buttons.RIGHT ? MapData.DEFAULT_TILE : state.tile;
    }

    // ------------------------------------------------------------------
    // Screen <-> tile maths
    // ------------------------------------------------------------------

    /** Fits the whole map into the space to the right of the sidebar, centred. */
    private void layout(MapData map) {
        float areaX = EditorUi.WIDTH + MARGIN;
        float areaW = stage.getWidth() - areaX - MARGIN;
        float areaH = stage.getHeight() - 2 * MARGIN;

        cell = (float) Math.floor(Math.min(areaW / map.getWidth(), areaH / map.getHeight()));
        if (cell < 1f) {
            cell = 1f;
        }
        left = areaX + (areaW - cell * map.getWidth()) / 2f;
        bottom = MARGIN + (areaH - cell * map.getHeight()) / 2f;
    }

    /** Converts window pixels (y points down) into stage coordinates (y points up) into 'mouse'. */
    private void toStage(int screenX, int screenY) {
        stage.screenToStageCoordinates(mouse.set(screenX, screenY));
    }

    private float mouseX() {
        toStage(Gdx.input.getX(), Gdx.input.getY());
        return mouse.x;
    }

    private float mouseY() {
        toStage(Gdx.input.getX(), Gdx.input.getY());
        return mouse.y;
    }

    private int cellX(float stageX) {
        return cell <= 0 ? -1 : (int) Math.floor((stageX - left) / cell);
    }

    private int cellY(float stageY) {
        return cell <= 0 ? -1 : (int) Math.floor((stageY - bottom) / cell);
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
