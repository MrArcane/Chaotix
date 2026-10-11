package me.arkallic.editor;

import me.arkallic.map.MapData;
import me.arkallic.map.tile.TileType;

import java.util.ArrayDeque;

/**
 * The editing operations, as plain functions on MapData.
 * No libGDX, no mouse, no screen: just "change these tiles / heights".
 * That keeps them easy to read, and easy to test.
 */
final class Painter {

    /** Something to do to one cell. */
    interface Cell {
        void at(int x, int y);
    }

    private Painter() {
    }

    // ---- tiles ----------------------------------------------------------

    /**
     * Paints a size x size square around (cx, cy). Size 1 is a single tile.
     * Even sizes can't be centred exactly, so they extend one extra tile to the
     * south and west of the cursor.
     */
    static void stamp(MapData map, int cx, int cy, int size, TileType type) {
        square(cx, cy, size, (x, y) -> MapOps.setTile(map, x, y, type));
    }

    /** Stamps along a straight line, so a fast drag leaves a continuous stroke. */
    static void line(MapData map, int x0, int y0, int x1, int y1, int size, TileType type) {
        walk(x0, y0, x1, y1, (x, y) -> stamp(map, x, y, size, type));
    }

    /**
     * Replaces the connected area of identical tiles around (x, y) with
     * "replacement". Connected means up/down/left/right (not diagonal).
     * Heights are not touched.
     */
    static void floodFill(MapData map, int x, int y, TileType replacement) {
        TileType target = MapOps.getTile(map, x, y);
        if (target == null || target == replacement) {
            return; // outside the map, or nothing would change
        }

        ArrayDeque<int[]> queue = new ArrayDeque<>();
        MapOps.setTile(map, x, y, replacement); // mark as soon as we queue it, so nothing is queued twice
        queue.add(new int[]{x, y});

        int[][] neighbours = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            for (int[] n : neighbours) {
                int nx = p[0] + n[0];
                int ny = p[1] + n[1];
                if (MapOps.getTile(map, nx, ny) == target) { // null outside the map, so edges stop the fill
                    MapOps.setTile(map, nx, ny, replacement);
                    queue.add(new int[]{nx, ny});
                }
            }
        }
    }

    // ---- heights --------------------------------------------------------

    /**
     * SETS (rather than adds to) the height of a square of tiles. Setting is
     * idempotent: dragging slowly over the same tile can't pile it up higher
     * and higher, which an "add 0.25" brush would.
     */
    static void stampHeight(MapData map, int cx, int cy, int size, float height) {
        square(cx, cy, size, (x, y) -> MapOps.setHeight(map, x, y, height));
    }

    static void lineHeight(MapData map, int x0, int y0, int x1, int y1, int size, float height) {
        walk(x0, y0, x1, y1, (x, y) -> stampHeight(map, x, y, size, height));
    }

    // ---- shared shapes ----------------------------------------------------

    private static void square(int cx, int cy, int size, Cell action) {
        int x0 = cx - size / 2;
        int y0 = cy - size / 2;
        for (int dy = 0; dy < size; dy++) {
            for (int dx = 0; dx < size; dx++) {
                action.at(x0 + dx, y0 + dy);
            }
        }
    }

    /** Bresenham: visits every cell on the straight line between two cells. */
    private static void walk(int x0, int y0, int x1, int y1, Cell action) {
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            action.at(x0, y0);
            if (x0 == x1 && y0 == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
}
