package me.arkallic.map;

import me.arkallic.map.tile.Tile;
import me.arkallic.map.tile.TileType;
import me.arkallic.model.Position;
import me.arkallic.world.World;

import java.util.ArrayList;

public class GameMap {
    private final World world;

    public GameMap(World world) {
        this.world = world;
        createMaps();
    }

    public void createMaps() {
        createAshfordChunk(0, 0);
        createChunk(1, 0);
    }

    public void createChunk(int chunkX, int chunkY) {
        final int SIZE = 64;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {

                TileType type = TileType.GRASS;

                // ==========================================
                // GRASS VARIATION
                // ==========================================

                if ((x * 17 + y * 31) % 13 == 0) {
                    type = TileType.GRASS_DARK;
                }

                if ((x * 23 + y * 11) % 29 == 0) {
                    type = TileType.GRASS_LIGHT;
                }


                // ==========================================
                // DIRT PATCHES
                // ==========================================

                if (x >= 8 && x <= 18 &&
                    y >= 42 && y <= 52) {

                    type = TileType.DIRT;
                }

                if (x >= 42 && x <= 55 &&
                    y >= 8 && y <= 17) {

                    type = TileType.DIRT;
                }


                // ==========================================
                // SMALL WATER LAKE
                // ==========================================

                if (x >= 5 && x <= 15 &&
                    y >= 5 && y <= 14) {

                    type = TileType.WATER;
                }

                // Lake variation
                if (x >= 7 && x <= 13 &&
                    y >= 7 && y <= 12) {

                    type = TileType.WATER_DARK;
                }


                // ==========================================
                // STONE PATCH
                // ==========================================

                if (x >= 45 && x <= 57 &&
                    y >= 45 && y <= 55) {

                    type = TileType.STONE;
                }

                // Stone variation
                if ((x + y) % 5 == 0 &&
                    x >= 45 && x <= 57 &&
                    y >= 45 && y <= 55) {

                    type = TileType.STONE_LIGHT;
                }


                // ==========================================
                // DIRT TRAIL
                // ==========================================

                if (x >= 28 && x <= 31) {
                    type = TileType.PATH;
                }

                if (y >= 28 && y <= 31) {
                    type = TileType.PATH;
                }


                // ==========================================
                // SMALL CROSSROADS
                // ==========================================

                if (x >= 27 && x <= 32 &&
                    y >= 27 && y <= 32) {

                    type = TileType.COBBLESTONE;
                }


                // ==========================================
                // BRIDGE
                // ==========================================

                if (x >= 5 && x <= 15 &&
                    y >= 9 && y <= 10) {

                    type = TileType.BRIDGE;
                }


                // ==========================================
                // ASH / DEAD GROUND
                // ==========================================

                if (x >= 48 && x <= 58 &&
                    y >= 20 && y <= 28) {

                    type = TileType.ASH;
                }


                // ==========================================
                // BOUNDARY
                // ==========================================

                if (x == 0 || y == 0 ||
                    x == SIZE - 1 || y == SIZE - 1) {

                    type = TileType.STONE_DARK;
                }


                // ==========================================
                // ADD TILE
                // ==========================================

                world.addTile(
                    new Tile(
                        new Position(
                            chunkX * SIZE + x,
                            chunkY * SIZE + y,
                            0
                        ),
                        type
                    )
                );
            }
        }
    }
    public void createAshfordChunk(int chunkX, int chunkY) {
        final int SIZE = 32;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {

                TileType type = TileType.GRASS;

                // ==========================================
                // GRASS VARIATION
                // ==========================================

                if ((x * 13 + y * 7) % 17 == 0) {
                    type = TileType.GRASS_DARK;
                }

                if ((x * 5 + y * 19) % 31 == 0) {
                    type = TileType.GRASS_LIGHT;
                }


                // ==========================================
                // MAIN NORTH / SOUTH ROAD
                // ==========================================

                if (x >= 14 && x <= 17) {
                    type = TileType.PATH;
                }


                // ==========================================
                // MAIN WEST / EAST ROAD
                // ==========================================

                if (y >= 14 && y <= 17) {
                    type = TileType.PATH;
                }


                // ==========================================
                // CENTRAL TOWN SQUARE
                // ==========================================

                if (x >= 12 && x <= 19 &&
                    y >= 12 && y <= 19) {

                    type = TileType.STONE;
                }


                // ==========================================
                // INNER SQUARE
                // ==========================================

                if (x >= 14 && x <= 17 &&
                    y >= 14 && y <= 17) {

                    type = TileType.COBBLESTONE;
                }


                // ==========================================
                // NORTHWEST HOUSE
                // ==========================================

                if (x >= 3 && x <= 9 &&
                    y >= 21 && y <= 27) {

                    type = TileType.STONE;
                }

                // Door / entrance
                if (x >= 5 && x <= 7 && y == 21) {
                    type = TileType.PATH;
                }


                // ==========================================
                // NORTHEAST HOUSE
                // ==========================================

                if (x >= 22 && x <= 28 &&
                    y >= 21 && y <= 27) {

                    type = TileType.STONE;
                }

                if (x >= 24 && x <= 26 && y == 21) {
                    type = TileType.PATH;
                }


                // ==========================================
                // SOUTHWEST HOUSE
                // ==========================================

                if (x >= 3 && x <= 9 &&
                    y >= 4 && y <= 10) {

                    type = TileType.STONE;
                }

                if (x >= 5 && x <= 7 && y == 10) {
                    type = TileType.PATH;
                }


                // ==========================================
                // SOUTHEAST HOUSE
                // ==========================================

                if (x >= 22 && x <= 28 &&
                    y >= 4 && y <= 10) {

                    type = TileType.STONE;
                }

                if (x >= 24 && x <= 26 && y == 10) {
                    type = TileType.PATH;
                }


                // ==========================================
                // WESTERN POND
                // ==========================================

                if (x >= 2 && x <= 7 &&
                    y >= 24 && y <= 29) {

                    type = TileType.WATER;
                }

                if (x >= 3 && x <= 6 &&
                    y >= 25 && y <= 28) {

                    type = TileType.WATER_DARK;
                }


                // ==========================================
                // EASTERN POND
                // ==========================================

                if (x >= 24 && x <= 29 &&
                    y >= 2 && y <= 7) {

                    type = TileType.WATER;
                }

                if (x >= 25 && x <= 28 &&
                    y >= 3 && y <= 6) {

                    type = TileType.WATER_DARK;
                }


                // ==========================================
                // NORTH GATE
                // ==========================================

                if (x >= 14 && x <= 17 && y == 31) {
                    type = TileType.PATH;
                }


                // ==========================================
                // SOUTH GATE
                // ==========================================

                if (x >= 14 && x <= 17 && y == 0) {
                    type = TileType.PATH;
                }


                // ==========================================
                // WEST GATE
                // ==========================================

                if (y >= 14 && y <= 17 && x == 0) {
                    type = TileType.PATH;
                }


                // ==========================================
                // EAST GATE
                // ==========================================

                if (y >= 14 && y <= 17 && x == 31) {
                    type = TileType.PATH;
                }


                // ==========================================
                // SMALL PLAZA DETAILS
                // ==========================================

                if ((x == 12 || x == 19) &&
                    y >= 12 && y <= 19) {

                    type = TileType.COBBLESTONE;
                }

                if ((y == 12 || y == 19) &&
                    x >= 12 && x <= 19) {

                    type = TileType.COBBLESTONE;
                }


                // ==========================================
                // ADD TILE
                // ==========================================

                world.addTile(
                    new Tile(
                        new Position(
                            chunkX * SIZE + x,
                            chunkY * SIZE + y,
                            0
                        ),
                        type
                    )
                );
            }
        }
    }


}
