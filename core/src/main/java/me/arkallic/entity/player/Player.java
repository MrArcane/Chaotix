package me.arkallic.entity.player;

import com.badlogic.gdx.Gdx;
import me.arkallic.entity.Entity;
import me.arkallic.input.GameInput;
import me.arkallic.model.Position;

public class Player implements Entity {

    private final String username;
    private final Position position;
    private Position targetPosition;
    private boolean running;

    public Player(String username, Position position) {
        this.username = username;
        this.position = position;
        this.targetPosition = new Position(position.getX(), position.getY(), position.getZ());
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
        float dx = targetPosition.getX() - position().getX();
        float dy = targetPosition.getY() - position().getY();

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.01f) {
            float movement = Math.min(0.2f / 20f, distance);

            position().setX(position().getX() + dx / distance * movement);
            position().setY(position().getY() + dy / distance * movement);
        }
    }

    public Position getPosition() {
        return position;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    public void moveTo(float x, float y) {
        targetPosition.setX(x);
        targetPosition.setY(y);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isMoving() {
        float dx = targetPosition.getX() - position.getX();
        float dy = targetPosition.getY() - position.getY();

        return dx * dx + dy * dy > 0.0001f;
    }
    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getUsername() {
        return username;
    }
}
