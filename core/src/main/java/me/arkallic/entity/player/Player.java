package me.arkallic.entity.player;

import com.badlogic.gdx.Gdx;
import me.arkallic.entity.Entity;
import me.arkallic.input.GameInput;
import me.arkallic.model.Position;

public class Player implements Entity {

    private final Position position;
    private boolean running;

    public Player(Position position) {
        this.position = position;
    }

    @Override
    public String name() {
        return "Player";
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public void tick() {

    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
