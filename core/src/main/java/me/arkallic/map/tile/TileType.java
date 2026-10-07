package me.arkallic.map.tile;

import com.badlogic.gdx.graphics.Color;

public enum TileType {

    GRASS(new Color(0.25f, 0.55f, 0.20f, 1f)),
    GRASS_DARK(new Color(0.18f, 0.42f, 0.15f, 1f)),
    GRASS_LIGHT(new Color(0.35f, 0.65f, 0.25f, 1f)),

    DIRT(new Color(0.45f, 0.32f, 0.18f, 1f)),
    PATH(new Color(0.55f, 0.40f, 0.20f, 1f)),
    PATH_DARK(new Color(0.40f, 0.28f, 0.14f, 1f)),
    SAND(new Color(0.75f, 0.65f, 0.40f, 1f)),

    STONE(new Color(0.45f, 0.45f, 0.45f, 1f)),
    STONE_DARK(new Color(0.32f, 0.32f, 0.32f, 1f)),
    STONE_LIGHT(new Color(0.60f, 0.60f, 0.60f, 1f)),

    WATER(new Color(0.10f, 0.35f, 0.75f, 1f)),
    WATER_DARK(new Color(0.05f, 0.22f, 0.55f, 1f)),
    WATER_LIGHT(new Color(0.20f, 0.50f, 0.85f, 1f)),

    MUD(new Color(0.30f, 0.22f, 0.12f, 1f)),
    SNOW(new Color(0.90f, 0.90f, 0.90f, 1f)),
    ICE(new Color(0.65f, 0.85f, 0.95f, 1f)),

    WOOD(new Color(0.40f, 0.25f, 0.12f, 1f)),
    BRIDGE(new Color(0.50f, 0.30f, 0.12f, 1f)),

    COBBLESTONE(new Color(0.38f, 0.38f, 0.38f, 1f)),
    MARBLE(new Color(0.75f, 0.75f, 0.70f, 1f)),

    LAVA(new Color(0.85f, 0.20f, 0.05f, 1f)),
    ASH(new Color(0.25f, 0.23f, 0.22f, 1f));

    private final Color color;

    TileType(Color color) {
        this.color = color;
    }

    public Color color() {
        return color;
    }
}
