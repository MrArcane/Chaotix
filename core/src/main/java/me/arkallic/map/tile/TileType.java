package me.arkallic.map.tile;

import com.badlogic.gdx.graphics.Color;

public enum TileType {

    // Grass: natural greens with subtle variation
    GRASS(Material.GRASS, new Color(0.30f, 0.52f, 0.22f, 1f)),
    GRASS_DARK(Material.GRASS, new Color(0.24f, 0.43f, 0.18f, 1f)),
    GRASS_LIGHT(Material.GRASS, new Color(0.37f, 0.59f, 0.27f, 1f)),

    // Earth: warm, earthy browns
    DIRT(Material.DIRT, new Color(0.43f, 0.31f, 0.20f, 1f)),
    PATH(Material.DIRT, new Color(0.53f, 0.39f, 0.25f, 1f)),
    PATH_DARK(Material.DIRT, new Color(0.37f, 0.26f, 0.17f, 1f)),
    SAND(Material.SAND, new Color(0.76f, 0.67f, 0.45f, 1f)),

    // Stone: muted gray with a slight warm tint
    STONE(Material.STONE, new Color(0.48f, 0.47f, 0.44f, 1f)),
    STONE_DARK(Material.STONE, new Color(0.36f, 0.36f, 0.34f, 1f)),
    STONE_LIGHT(Material.STONE, new Color(0.62f, 0.61f, 0.57f, 1f)),

    // Water: deep blue with lighter variations
    WATER(Material.WATER, new Color(0.12f, 0.34f, 0.62f, 1f)),
    WATER_DARK(Material.WATER, new Color(0.08f, 0.25f, 0.47f, 1f)),
    WATER_LIGHT(Material.WATER, new Color(0.20f, 0.44f, 0.70f, 1f)),

    // Other terrain
    MUD(Material.MUD, new Color(0.32f, 0.25f, 0.18f, 1f)),
    SNOW(Material.SNOW, new Color(0.88f, 0.90f, 0.91f, 1f)),
    ICE(Material.ICE, new Color(0.64f, 0.81f, 0.88f, 1f)),

    // Wooden structures
    WOOD(Material.WOOD, new Color(0.39f, 0.27f, 0.17f, 1f)),
    BRIDGE(Material.WOOD, new Color(0.52f, 0.37f, 0.22f, 1f)),

    // Stone variations
    COBBLESTONE(Material.STONE, new Color(0.42f, 0.41f, 0.39f, 1f)),
    MARBLE(Material.STONE, new Color(0.77f, 0.75f, 0.69f, 1f)),

    // Volcanic terrain
    LAVA(Material.LAVA, new Color(0.90f, 0.25f, 0.07f, 1f)),
    ASH(Material.ASH, new Color(0.29f, 0.27f, 0.25f, 1f));

    public enum Material {
        GRASS,
        DIRT,
        SAND,
        STONE,
        WATER,
        MUD,
        SNOW,
        ICE,
        WOOD,
        LAVA,
        ASH
    }

    private final Material material;
    private final Color color;

    TileType(Material material, Color color) {
        this.material = material;
        this.color = color;
    }

    public Material material() {
        return material;
    }

    public Color color() {
        return color;
    }
}
