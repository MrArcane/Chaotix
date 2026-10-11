package me.arkallic.editor;

import me.arkallic.map.data.MapData;
import me.arkallic.map.tile.TileType;

import java.util.ArrayDeque;

/**
 * The editing operations, as plain functions on MapData.
 * No libGDX, no mouse, no screen: just "change these tiles".
 * That keeps them easy to read, and easy to test.
 */
final class Painter {

    private Painter() {
    }

    /**
     * Paints a size x size square around (cx, cy). Size 1 is a single tile.
     * Even sizes can't be centered exactly, so they extend one extra tile to the
     * south and west of the cursor.
     */
    static void stamp(MapData map, int cx, int cy, int size, TileType type) {
        int x0 = cx - size / 2;
        int y0 = cy - size / 2;
        for (int dy = 0; dy < size; dy++) {
            for (int dx = 0; dx < size; dx++) {
                map.set(x0 + dx, y0 + dy, type);
            }
        }
    }

    /**
     * Stamps along a straight line (Bresenham), so a fast mouse drag leaves a
     * continuous stroke instead of dots. The mouse only reports a position once
     * per frame, so without this you would get gaps.
     */
    static void line(MapData map, int x0, int y0, int x1, int y1, int size, TileType type) {
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            stamp(map, x0, y0, size, type);
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

    /**
     * Replaces the connected area of identical tiles around (x, y) with
     * "replacement". Connected means up/down/left/right (not diagonal).
     */
    static void floodFill(MapData map, int x, int y, TileType replacement) {
        TileType target = map.get(x, y);
        if (target == null || target == replacement) {
            return; // outside the map, or nothing would change
        }

        ArrayDeque<int[]> queue = new ArrayDeque<>();
        map.set(x, y, replacement); // mark as soon as we queue it, so nothing is queued twice
        queue.add(new int[]{x, y});

        int[][] neighbours = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            for (int[] n : neighbours) {
                int nx = p[0] + n[0];
                int ny = p[1] + n[1];
                if (map.get(nx, ny) == target) { // get() returns null outside the map, so edges stop the fill
                    map.set(nx, ny, replacement);
                    queue.add(new int[]{nx, ny});
                }
            }
        }
    }
}
