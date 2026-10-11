package me.arkallic.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import me.arkallic.map.tile.Tile;
import me.arkallic.map.tile.TileType;
import me.arkallic.model.Position;
import me.arkallic.world.World;

public class GameMap {
    private final World world;
    private final Tile[][] tiles = new Tile[32][32];
    private MapData mapData = new MapData();

    public GameMap(World world) {
        this.world = world;
    }

    public void createMaps() {
        //createAshfordChunk(0, 0);
        loadAshford();
    }


    public void loadAshford() {
        mapData = loadMap("ashford.json");

        for (int y = 0; y < mapData.height; y++) {
            for (int x = 0; x < mapData.width; x++) {

                TileType type = TileType.valueOf(
                    mapData.tiles[y][x]
                );

                float height = mapData.heights[y][x];

                Tile tile = new Tile(
                    new Position(
                        x,
                        y,
                        height
                    ),
                    type
                );

                tiles[x][y] = tile;
                world.addTile(tile);
                System.out.println(
                    "x=" + x +
                        " y=" + y +
                        " height=" + height
                );
            }
        }
    }

    public float[][] getHeights() {
        return mapData.heights;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    private MapData loadMap(String fileName) {
        Json json = new Json();
        System.out.println("Loaded map: " + fileName);
        return json.fromJson(
            MapData.class,
            Gdx.files.internal("maps/" + fileName)
        );
    }

}
