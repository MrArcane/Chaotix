package me.arkallic;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import me.arkallic.engine.GameLoop;
import me.arkallic.entity.npc.Npc;
import me.arkallic.entity.player.Player;
import me.arkallic.input.GameInput;
import me.arkallic.map.GameMap;
import me.arkallic.map.tile.Tile;
import me.arkallic.world.World;

import java.util.HashMap;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Chaotix extends ApplicationAdapter {

    private World world;
    private GameMap gameMap;
    private GameLoop gameLoop;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private SpriteBatch batch;
    private PerspectiveCamera camera;
    private Player player;
    private GameInput gameInput;
    private TextureAtlas atlas;
    private final HashMap<String, TextureRegion> textures = new HashMap<>();


    public void loadSprites() {
        atlas = new TextureAtlas(Gdx.files.internal("packed/characters.atlas"));

        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
            textures.put(region.name, region);

            System.out.println(
                "Loaded: " + region.name
                    + " | " + region.getRegionWidth()
                    + "x" + region.getRegionHeight()
            );
        }
    }

    @Override
    public void create() {
        world = new World();
        gameMap = new GameMap(world);

        loadSprites();

        camera = new PerspectiveCamera(
            67,
            800,
            600
        );

        world.initialize();

        player = world.getPlayer();
        gameInput = new GameInput();
        Gdx.input.setInputProcessor(gameInput);
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        batch = new SpriteBatch();

        gameLoop = new GameLoop(world.getPlayer(), world.getNpcs());
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        gameLoop.update();

        handleCamera();

        shapeRenderer.setProjectionMatrix(camera.combined);

        drawMap();
        drawNpcs();
        handleMovement();
        drawPlayer();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

    private void handleMovement() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        gameInput.move(player, 5f, 6f, deltaTime);

        float cameraAngle = gameInput.getCameraAngle();
        cameraAngle = gameInput.moveCamera(cameraAngle);
        gameInput.setCameraAngle(cameraAngle);

        float cameraPitch = gameInput.getCameraPitch();
        cameraPitch = gameInput.moveCameraPitch(cameraPitch);
        gameInput.setCameraPitch(cameraPitch);
    }

    private void handleCamera() {
        float x = player.position().getX() * Tile.TILE_SIZE;
        float y = player.position().getY() * Tile.TILE_SIZE;

        float yaw = MathUtils.degreesToRadians * gameInput.getCameraAngle();
        float pitch = MathUtils.degreesToRadians * gameInput.getCameraPitch();

        float horizontalDistance = MathUtils.cos(pitch) * gameInput.getCameraDistance();
        float verticalDistance = MathUtils.sin(pitch) * gameInput.getCameraDistance();

        float cameraX = x + MathUtils.cos(yaw) * horizontalDistance;
        float cameraY = y + MathUtils.sin(yaw) * horizontalDistance;

        camera.position.set(
            cameraX,
            cameraY,
            verticalDistance
        );

        camera.lookAt(x, y, 0);

        camera.near = 1f;
        camera.far = 5000f;
        camera.up.set(0, 0, 1);
        camera.update();
    }

    private void drawNpcs() {

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Npc npc : world.getNpcs()) {
            float x = npc.position().getX() * Tile.TILE_SIZE;
            float y = npc.position().getY() * Tile.TILE_SIZE;
           shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.box(x, y, 20, 32, 32, 40);
        }

        shapeRenderer.end();
    }

    private void drawPlayer() {
        float x = player.position().getX() * Tile.TILE_SIZE;
        float y = player.position().getY() * Tile.TILE_SIZE;
        float z = player.position().getZ() * Tile.TILE_SIZE;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.box(x, y, 20, 32, 32, 40);
        shapeRenderer.end();
    }

    private void drawMap() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Tile tile : world.getTiles()) {
            shapeRenderer.setColor(tile.getTileType().color());
            shapeRenderer.box(tile.getPosition().getX() * Tile.TILE_SIZE,
                tile.getPosition().getY() * Tile.TILE_SIZE,
                0,
                Tile.TILE_SIZE,
                Tile.TILE_SIZE,
            1);
        }
        shapeRenderer.end();
    }
}
