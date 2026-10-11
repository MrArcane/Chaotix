package me.arkallic;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.ScreenUtils;
import me.arkallic.engine.GameLoop;
import me.arkallic.entity.npc.Npc;
import me.arkallic.entity.player.Player;
import me.arkallic.input.GameInput;
import me.arkallic.map.GameMap;
import me.arkallic.map.TerrainRenderer;
import me.arkallic.map.tile.Tile;
import me.arkallic.world.World;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;


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
    private TerrainRenderer terrainRenderer;
    private ShaderProgram transitionShader;
    private ShaderProgram terrainShader;
    private SceneAsset playerAsset;
    private Scene playerScene;
    private SceneManager sceneManager;
    private String currentPlayerAnimation = "Idle_Loop";
    private float playerRotation;


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
        gameMap.createMaps();

        loadSprites();

        camera = new PerspectiveCamera(
            67,
            800,
            600
        );
        playerAsset = new GLBLoader().load(
            Gdx.files.internal("models/player.glb")
        );

        playerScene = new Scene(playerAsset.scene);
        playerScene.animationController.setAnimation("Idle_Loop", -1);
        PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
        config.numBones = 65;
        config.numDirectionalLights = 1;
        config.numPointLights = 0;
        config.numSpotLights = 0;

        DepthShader.Config depthConfig = PBRShaderProvider.createDefaultDepthConfig();
        depthConfig.numBones = 65;

        sceneManager = new SceneManager(
            new PBRShaderProvider(config),
            new PBRDepthShaderProvider(depthConfig)
        );

        sceneManager.addScene(playerScene);
        sceneManager.setCamera(camera);
        sceneManager.setAmbientLight(1f);
        world.initialize();

        player = world.getPlayer();
        gameInput = new GameInput();
        Gdx.input.setInputProcessor(gameInput);
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        batch = new SpriteBatch();

        gameLoop = new GameLoop(world.getPlayer(), world.getNpcs());
        terrainRenderer = new TerrainRenderer(
            gameMap.getHeights(),
            gameMap.getTiles()
        );
        transitionShader = new ShaderProgram(
            Gdx.files.internal("shaders/transition.vert"),
            Gdx.files.internal("shaders/transition.frag")
        );

        if (!transitionShader.isCompiled()) {
            Gdx.app.error(
                "TransitionShader",
                transitionShader.getLog()
            );
        }
        terrainShader = new ShaderProgram(
            Gdx.files.internal("shaders/terrain.vert"),
            Gdx.files.internal("shaders/terrain.frag")
        );

        if (!terrainShader.isCompiled()) {
            throw new RuntimeException(terrainShader.getLog());
        }


    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT |
                GL20.GL_DEPTH_BUFFER_BIT
        );

        gameLoop.update();
        updatePlayerAnimation();
        playerScene.animationController.update(Gdx.graphics.getDeltaTime());
        handleCamera();
        handleWorldClick();
        playerScene.animationController.update(Gdx.graphics.getDeltaTime());
        shapeRenderer.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LESS);

        terrainShader.bind();

        terrainShader.setUniformMatrix(
            "u_projViewTrans",
            camera.combined
        );

        terrainRenderer.render(terrainShader);

        handleMovement();
        drawPlayerModel();
        drawNpcs();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        if (transitionShader != null) {
            transitionShader.dispose();
        }

        if (sceneManager != null) {
            sceneManager.dispose();
        }

        if (playerAsset != null) {
            playerAsset.dispose();
        }

        if (terrainRenderer != null) {
            terrainRenderer.dispose();
        }

        if (terrainShader != null) {
            terrainShader.dispose();
        }

        if (atlas != null) {
            atlas.dispose();
        }
    }

    private void handleMovement() {
        float deltaTime = Gdx.graphics.getDeltaTime();

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

    private void handleWorldClick() {
        if (!Gdx.input.justTouched()) {
            return;
        }

        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            return;
        }

        Ray ray = camera.getPickRay(
            Gdx.input.getX(),
            Gdx.input.getY()
        );

        float distance = -ray.origin.z / ray.direction.z;

        Vector3 worldPosition = new Vector3(ray.direction)
            .scl(distance)
            .add(ray.origin);

        player.moveTo(
            worldPosition.x / Tile.TILE_SIZE,
            worldPosition.y / Tile.TILE_SIZE
        );
    }

    private void updatePlayerAnimation() {
        String desiredAnimation;

        if (!player.isMoving()) {
            desiredAnimation = "Idle_Loop";
        } else if (player.isRunning()) {
            desiredAnimation = "Sprint_Loop";
        } else {
            desiredAnimation = "Walk_Loop";
        }

        if (!desiredAnimation.equals(currentPlayerAnimation)) {
            playerScene.animationController.setAnimation(desiredAnimation, -1);
            currentPlayerAnimation = desiredAnimation;
        }
    }
    private void drawNpcs() {

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Npc npc : world.getNpcs()) {
            float x = npc.position().getX() * Tile.TILE_SIZE;
            float y = npc.position().getY() * Tile.TILE_SIZE;
           shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.box(x, y, 20, 32, 32, 20);
        }

        shapeRenderer.end();
    }

    private void drawPlayerModel() {
        float x = player.position().getX() * Tile.TILE_SIZE;
        float y = player.position().getY() * Tile.TILE_SIZE;
        float z = terrainRenderer.getHeightAt(x, y);

        float dx = player.getTargetPosition().getX() - player.position().getX();
        float dy = player.getTargetPosition().getY() - player.position().getY();

        if (dx * dx + dy * dy > 0.0001f) {
            playerRotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees + 90f;
        }

        playerScene.modelInstance.transform.idt();

        playerScene.modelInstance.transform
            .translate(x, y, z)
            .rotate(Vector3.Z, playerRotation)
            .rotate(Vector3.X, 90f)
            .scale(20f, 20f, 20f);

        sceneManager.update(Gdx.graphics.getDeltaTime());
        sceneManager.render();
    }
    private void drawMap() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Tile tile : world.getTiles()) {
            float height = tile.getPosition().getZ() * 8f;
            float thickness = 8f;
            float x = tile.getPosition().getX() * Tile.TILE_SIZE;
            float y = tile.getPosition().getY() * Tile.TILE_SIZE;

            shapeRenderer.setColor(tile.getTileType().color());

            shapeRenderer.box(
                x,
                y,
                height - thickness / 2f,
                Tile.TILE_SIZE,
                Tile.TILE_SIZE,
                thickness
            );
        }

        shapeRenderer.end();
    }
}
