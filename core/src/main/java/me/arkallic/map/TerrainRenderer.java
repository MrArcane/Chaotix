package me.arkallic.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;

import me.arkallic.map.tile.Tile;
import me.arkallic.map.tile.TileType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TerrainRenderer {

    private static final float TILE_SIZE = 32f;
    private static final float HEIGHT_SCALE = 10f;

    // Keep variation subtle so the terrain looks natural.
    private static final float GRASS_VARIATION = 0.10f;
    private static final float OTHER_VARIATION = 0.045f;

    private static final int FLOATS_PER_VERTEX = 7;

    private final int width;
    private final int height;

    private final Tile[][] tiles;
    private final float[][] heights;

    private final Map<TileType.Material, Mesh> meshes =
        new EnumMap<>(TileType.Material.class);

    public TerrainRenderer(float[][] heights, Tile[][] tiles) {
        this.heights = heights;
        this.tiles = tiles;

        this.height = heights.length;
        this.width = heights[0].length;

        buildMeshes();
    }

    private void buildMeshes() {

        Map<TileType.Material, List<Float>> vertexGroups =
            new EnumMap<>(TileType.Material.class);

        Map<TileType.Material, List<Short>> indexGroups =
            new EnumMap<>(TileType.Material.class);

        for (TileType.Material material : TileType.Material.values()) {
            vertexGroups.put(material, new ArrayList<>());
            indexGroups.put(material, new ArrayList<>());
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Tile tile = tiles[x][y];

                if (tile == null || tile.getTileType() == null) {
                    continue;
                }

                TileType type = tile.getTileType();
                TileType.Material material = type.material();

                List<Float> vertices = vertexGroups.get(material);
                List<Short> indices = indexGroups.get(material);

                Color color = getTileColor(type, x, y);

                addTile(vertices, indices, x, y, color);
            }
        }

        for (TileType.Material material : TileType.Material.values()) {

            List<Float> vertices = vertexGroups.get(material);
            List<Short> indices = indexGroups.get(material);

            if (indices.isEmpty()) {
                continue;
            }

            float[] vertexArray = new float[vertices.size()];

            for (int i = 0; i < vertices.size(); i++) {
                vertexArray[i] = vertices.get(i);
            }

            short[] indexArray = new short[indices.size()];

            for (int i = 0; i < indices.size(); i++) {
                indexArray[i] = indices.get(i);
            }

            Mesh mesh = new Mesh(
                true,
                vertexArray.length / FLOATS_PER_VERTEX,
                indexArray.length,
                new VertexAttribute(
                    VertexAttributes.Usage.Position,
                    3,
                    ShaderProgram.POSITION_ATTRIBUTE
                ),
                new VertexAttribute(
                    VertexAttributes.Usage.ColorUnpacked,
                    4,
                    ShaderProgram.COLOR_ATTRIBUTE
                )
            );

            mesh.setVertices(vertexArray);
            mesh.setIndices(indexArray);

            meshes.put(material, mesh);
        }
    }

    private Color getTileColor(TileType type, int x, int y) {

        Color base = type.color();

        // Stable per-coordinate variation.
        int hash = x * 374761393 + y * 668265263;
        hash = (hash ^ (hash >>> 13)) * 1274126177;
        hash ^= hash >>> 16;

        float random = (hash & 0x7fffffff) / (float) Integer.MAX_VALUE;
        float variation = type.material() == TileType.Material.GRASS
            ? GRASS_VARIATION
            : OTHER_VARIATION;

        // Adjust brightness without modifying the enum's shared Color.
        float brightness = 1f + (random * 2f - 1f) * variation;

        return new Color(
            MathUtils.clamp(base.r * brightness, 0f, 1f),
            MathUtils.clamp(base.g * brightness, 0f, 1f),
            MathUtils.clamp(base.b * brightness, 0f, 1f),
            base.a
        );
    }

    private void addTile(
        List<Float> vertices,
        List<Short> indices,
        int x,
        int y,
        Color color
    ) {

        float x0 = (x - 0.5f) * TILE_SIZE;
        float x1 = (x + 0.5f) * TILE_SIZE;

        float y0 = (y - 0.5f) * TILE_SIZE;
        float y1 = (y + 0.5f) * TILE_SIZE;

        float z00 = getVertexHeight(x, y);
        float z10 = getVertexHeight(x + 1, y);
        float z01 = getVertexHeight(x, y + 1);
        float z11 = getVertexHeight(x + 1, y + 1);

        int baseVertex = vertices.size() / FLOATS_PER_VERTEX;

        addVertex(vertices, x0, y0, z00, color);
        addVertex(vertices, x1, y0, z10, color);
        addVertex(vertices, x0, y1, z01, color);
        addVertex(vertices, x1, y1, z11, color);

        // First triangle.
        indices.add((short) baseVertex);
        indices.add((short) (baseVertex + 1));
        indices.add((short) (baseVertex + 2));

        // Second triangle.
        indices.add((short) (baseVertex + 1));
        indices.add((short) (baseVertex + 3));
        indices.add((short) (baseVertex + 2));
    }

    private void addVertex(
        List<Float> vertices,
        float x,
        float y,
        float z,
        Color color
    ) {

        vertices.add(x);
        vertices.add(y);
        vertices.add(z);

        vertices.add(color.r);
        vertices.add(color.g);
        vertices.add(color.b);
        vertices.add(color.a);
    }

    private float getVertexHeight(int x, int y) {

        float total = 0f;
        int count = 0;

        if (x > 0 && y > 0) {
            total += heights[y - 1][x - 1];
            count++;
        }

        if (x < width && y > 0) {
            total += heights[y - 1][x];
            count++;
        }

        if (x > 0 && y < height) {
            total += heights[y][x - 1];
            count++;
        }

        if (x < width && y < height) {
            total += heights[y][x];
            count++;
        }

        return count == 0
            ? 0f
            : (total / count) * HEIGHT_SCALE;
    }

    public float getHeightAt(float x, float y) {

        float tileX = x / TILE_SIZE;
        float tileY = y / TILE_SIZE;

        int gridX = MathUtils.floor(tileX + 0.5f);
        int gridY = MathUtils.floor(tileY + 0.5f);

        if (gridX < 0 || gridX >= width ||
            gridY < 0 || gridY >= height) {
            return 0f;
        }

        return heights[gridY][gridX] * HEIGHT_SCALE;
    }

    public void render(ShaderProgram shader) {

        shader.bind();

        for (Mesh mesh : meshes.values()) {
            mesh.render(shader, GL20.GL_TRIANGLES);
        }
    }

    public void dispose() {

        for (Mesh mesh : meshes.values()) {
            mesh.dispose();
        }

        meshes.clear();
    }
}
