package me.arkallic.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import me.arkallic.entity.player.Player;


public class GameInput implements InputProcessor {

    private float cameraDistance = 300f;
    private float cameraAngle = 0f;
    private float lastMouseX;
    private float lastMouseY;

    public float getCameraPitch() {
        return cameraPitch;
    }

    public void setCameraPitch(float cameraPitch) {
        this.cameraPitch = cameraPitch;
    }

    private float cameraPitch = 45f;

    @Override
    public boolean scrolled(float amountX, float amountY) {
        cameraDistance += amountY * 20f;
        cameraDistance = MathUtils.clamp(cameraDistance, 100f, 600f);
        return true;
    }

    public float getCameraDistance() {
        return cameraDistance;
    }

    public void setCameraDistance(float cameraDistance) {
        this.cameraDistance = cameraDistance;
    }

    public float moveCameraPitch(float cameraPitch) {
        float deltaTime = Gdx.graphics.getDeltaTime();
        float cameraSpeed = 50f;

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            cameraPitch += cameraSpeed * deltaTime;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            cameraPitch -= cameraSpeed * deltaTime;
        }

        return MathUtils.clamp(cameraPitch, 20f, 80f);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE) {
            lastMouseX = screenX;
            lastMouseY = screenY;
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            float mouseDeltaX = screenX - lastMouseX;
            float mouseDeltaY = screenY - lastMouseY;

            cameraAngle -= mouseDeltaX * 0.5f;
            cameraPitch -= mouseDeltaY * 0.5f;
            cameraPitch = MathUtils.clamp(cameraPitch, 20f, 80f);

            lastMouseX = screenX;
            lastMouseY = screenY;

            return true;
        }

        return false;
    }

    public float moveCamera(float cameraAngle) {
        float deltaTime = Gdx.graphics.getDeltaTime();
        float cameraSpeed = 100f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cameraAngle -= cameraSpeed * deltaTime;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cameraAngle += cameraSpeed * deltaTime;
        }

        return cameraAngle;
    }

    public void move(Player player, float speed, float runSpeed, float deltaTime) {
        float movementSpeed = speed;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            player.setRunning(true);
            movementSpeed += runSpeed;
        } else {
            player.setRunning(false);
        }

        float movement = movementSpeed * deltaTime;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            player.position().setY(player.position().getY() + movement);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            player.position().setY(player.position().getY() - movement);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.position().setX(player.position().getX() - movement);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.position().setX(player.position().getX() + movement);
        }
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }

    public float getCameraAngle() {
        return cameraAngle;
    }

    public void setCameraAngle(float cameraAngle) {
        this.cameraAngle = cameraAngle;
    }
}
