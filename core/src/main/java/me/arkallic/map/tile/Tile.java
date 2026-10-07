package me.arkallic.map.tile;

import me.arkallic.model.Position;

public class Tile {

    private final Position position;
    private final TileType tileType;
    public static final int TILE_SIZE = 32;

    public Tile(Position position, TileType tileType) {
        this.position = position;
        this.tileType = tileType;
    }

    public Position getPosition() {
        return position;
    }

    public TileType getTileType() {
        return tileType;
    }
}
