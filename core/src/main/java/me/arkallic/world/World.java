package me.arkallic.world;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import me.arkallic.entity.npc.Npc;
import me.arkallic.entity.player.Player;
import me.arkallic.input.GameInput;
import me.arkallic.map.tile.Tile;
import me.arkallic.model.Position;

import java.util.ArrayList;

import static me.arkallic.map.tile.Tile.TILE_SIZE;

public class World {

    private Player player;
    private final GameInput gameInput = new GameInput();
    private final ArrayList<Npc> npcs = new ArrayList<>();
    private final ArrayList<Tile> tiles = new ArrayList<>();

    public void initialize() {
        player = new Player("Player", new Position(0, 0, 0));
        createNpcs();
    }

    public void createNpcs() {
        addNpc(new Npc("Goblin", new Position(2, 2, 0), 3));
        addNpc(new Npc("Rat", new Position(-2, 2, 0), 3));
        addNpc(new Npc("Bat", new Position(2, -2, 0), 3));
        addNpc(new Npc("Man", new Position(-2, -2, 0), 3));
    }

    public Tile getTileAt(float x, float y) {
        for (Tile tile : tiles) {
            if (tile.getPosition().getX() == (int) x &&
                tile.getPosition().getY() == (int) y) {
                return tile;
            }
        }

        return null;
    }

    public Player getPlayer() {
        return player;
    }

    public GameInput getGameInput() {
        return gameInput;
    }

    public void addNpc(Npc npc) {
        npcs.add(npc);
    }

    public void removeNpc(Npc npc) {
        npcs.remove(npc);
    }

    public ArrayList<Npc> getNpcs() {
        return npcs;
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

    public void addTile(Tile tile) {
        tiles.add(tile);
    }
    public void removeTile(Tile tile) {
        tiles.remove(tile);
    }
}
