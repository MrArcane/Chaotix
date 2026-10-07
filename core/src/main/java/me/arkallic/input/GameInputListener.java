package me.arkallic.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class GameInputListener extends InputListener {

    private boolean up, down, left, right;
    @Override
    public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Input.Keys.W) up = true;
        if (keycode == Input.Keys.S) down = true;
        if (keycode == Input.Keys.A) left = true;
        if (keycode == Input.Keys.D) right = true;

        return true;
    }

    @Override
    public boolean keyUp(InputEvent event, int keycode) {
        if (keycode == Input.Keys.W) up = false;
        if (keycode == Input.Keys.S) down = false;
        if (keycode == Input.Keys.A) left = false;
        if (keycode == Input.Keys.D) right = false;

        return true;
    }

    public boolean isUp() {
        return up;
    }

    public boolean isDown() {
        return down;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }
}
