package me.arkallic.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import me.arkallic.map.MapData;
import me.arkallic.map.tile.TileType;

import java.util.Locale;

/**
 * Draws the map as a flat top-down grid and turns mouse input into edits.
 * <p>
 * Orientation matches the game and the JSON: x grows to the right (east), y grows
 * UP the screen (north). tiles[0][0] is the bottom-left corner.
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

        // Height view: brighten high ground, darken low ground, relative to this map's range.
        float lo = 0f;
        float range = 1f;
        if (state.showHeights) {
            float hi = Float.NEGATIVE_INFINITY;
            lo = Float.POSITIVE_INFINITY;
            for (int y = 0; y < map.height; y++) {
                for (int x = 0; x < map.width; x++) {
                    lo = Math.min(lo, map.heights[y][x]);
                    hi = Math.max(hi, map.heights[y][x]);
                }
            }
            range = Math.max(hi - lo, 0.0001f);
        }

        // 1. the tiles
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < map.height; y++) {
            for (int x = 0; x < map.width; x++) {
                Color base = MapOps.getTile(map, x, y).color();
                if (state.showHeights) {
                    float k = 0.45f + 0.9f * ((map.heights[y][x] - lo) / range);
                    shapes.setColor(Math.min(1f, base.r * k), Math.min(1f, base.g * k), Math.min(1f, base.b * k), 1f);
                } else {
                    shapes.setColor(base);
                }
                shapes.rect(left + x * cell, bottom + y * cell, cell, cell);
            }
        }
        shapes.end();

        // 2. lines on top (alpha needs blending switched on)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Line);

        float w = map.width * cell;
        float h = map.height * cell;

        if (cell >= 6) { // thin lines between every tile, only when they're big enough to see
            shapes.setColor(0f, 0f, 0f, 0.25f);
            for (int x = 0; x <= map.width; x++) {
                shapes.line(left + x * cell, bottom, left + x * cell, bottom + h);
            }
            for (int y = 0; y <= map.height; y++) {
                shapes.line(left, bottom + y * cell, left + w, bottom + y * cell);
            }
        }

        shapes.setColor(0f, 0f, 0f, 0.6f); // darker line every 8 tiles, to help you count
        for (int x = 0; x <= map.width; x += MAJOR_LINE_EVERY) {
            shapes.line(left + x * cell, bottom, left + x * cell, bottom + h);
        }
        for (int y = 0; y <= map.height; y += MAJOR_LINE_EVERY) {
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
        if (!MapOps.inBounds(map, hx, hy)) {
            return;
        }
        int size = brushSize();
        int x0 = hx - size / 2;
        int y0 = hy - size / 2;
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.rect(left + x0 * cell, bottom + y0 * cell, size * cell, size * cell);
    }

    /** Fill always works on one connected area, so it shows a single tile; the others use the brush size. */
    private int brushSize() {
        return state.tool == Tool.FILL ? 1 : state.brush;
    }

    /** "x, y: TILE  h=1.25" for the tile under the mouse, or "" when the mouse is elsewhere. */
    String hoverText() {
        MapData map = state.map;
        int hx = cellX(mouseX());
        int hy = cellY(mouseY());
        TileType type = MapOps.getTile(map, hx, hy);
        if (type == null) {
            return "";
        }
        return String.format(Locale.ROOT, "%d, %d: %s  h=%.2f", hx, hy, type.name(), MapOps.getHeight(map, hx, hy));
    }

    // ------------------------------------------------------------------
    // Mouse input
    // ------------------------------------------------------------------

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT && button != Input.Buttons.RIGHT) {
            return false;
        }
        stage.unfocusAll(); // clicking the map takes the keyboard away from the text boxes

        toStage(screenX, screenY);
        int x = cellX(mouse.x);
        int y = cellY(mouse.y);
        if (cell <= 0 || !MapOps.inBounds(state.map, x, y)) {
            return false;
        }

        dragButton = button;
        lastX = x;
        lastY = y;

        switch (state.tool) {
            case FILL:
                Painter.floodFill(state.map, x, y, tileFor(button));
                break;
            case HEIGHT:
                Painter.stampHeight(state.map, x, y, state.brush, heightFor(button));
                break;
            default:
                Painter.stamp(state.map, x, y, state.brush, tileFor(button));
                break;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (dragButton < 0 || state.tool == Tool.FILL) {
            return false; // fill happens once per click, never while dragging
        }
        toStage(screenX, screenY);
        int x = cellX(mouse.x);
        int y = cellY(mouse.y);
        if (state.tool == Tool.HEIGHT) {
            Painter.lineHeight(state.map, lastX, lastY, x, y, state.brush, heightFor(dragButton));
        } else {
            Painter.line(state.map, lastX, lastY, x, y, state.brush, tileFor(dragButton));
        }
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
    private TileType tileFor(int button) {
        return button == Input.Buttons.RIGHT ? MapOps.DEFAULT_TILE : state.tile;
    }

    /** Left button sets the height in the Height box. Right button flattens back to 0. */
    private float heightFor(int button) {
        return button == Input.Buttons.RIGHT ? 0f : state.height;
    }

    // ------------------------------------------------------------------
    // Screen <-> tile maths
    // ------------------------------------------------------------------

    /** Fits the whole map into the space to the right of the sidebar, centred. */
    private void layout(MapData map) {
        float areaX = EditorUi.WIDTH + MARGIN;
        float areaW = stage.getWidth() - areaX - MARGIN;
        float areaH = stage.getHeight() - 2 * MARGIN;

        cell = (float) Math.floor(Math.min(areaW / map.width, areaH / map.height));
        if (cell < 1f) {
            cell = 1f;
        }
        left = areaX + (areaW - cell * map.width) / 2f;
        bottom = MARGIN + (areaH - cell * map.height) / 2f;
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
