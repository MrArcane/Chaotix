package me.arkallic.editor;

import me.arkallic.map.MapData;
import me.arkallic.map.tile.TileType;

/**
 * Everything the editor is "currently doing", in one place.
 * <p>
 * The UI writes to it (you click a palette button -> tile changes),
 * the grid reads from it (you click the grid -> paint with state.tile).
 * That way the UI and the grid never need to know about each other.
 */
final class EditorState {

    /** The map being edited. Replaced wholesale when you Load a file. */
    MapData map;

    TileType tile = TileType.PATH;
    Tool tool = Tool.PAINT;
    int brush = 1;

    /** Value the Height tool paints. */
    float height = 1f;

    /** Shade the grid by height, so you can see the terrain shape. */
    boolean showHeights = false;

    EditorState(MapData map) {
        this.map = map;
    }
}
