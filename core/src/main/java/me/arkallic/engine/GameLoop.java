package me.arkallic.engine;

import me.arkallic.entity.npc.Npc;
import me.arkallic.entity.player.Player;

import java.util.ArrayList;

public class GameLoop {

    private static final float TICK_RATE = 1f / 20f;

    private float accumulator;
    private final Player player;
    private final ArrayList<Npc> npcs;

    public GameLoop(Player player, ArrayList<Npc> npcs) {
        this.player = player;
        this.npcs = npcs;
    }

    public void update() {
        tick();
    }

    private void tick() {
        player.tick();

        for (Npc npc : npcs) {
            npc.tick();
        }
    }
}
