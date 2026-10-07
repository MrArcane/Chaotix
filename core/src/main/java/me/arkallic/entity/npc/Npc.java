package me.arkallic.entity.npc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import me.arkallic.entity.Entity;
import me.arkallic.model.Position;

public class Npc implements Entity {

    private final String name;
    private Position position;
    private int movementRadius;
    private final Position spawnPosition;
    private Position targetPosition;
    private float seconds;
    private final float MOVEMENT_SPEED = 2f;


    public Npc(String name, Position position, int movementRadius) {
        this.name = name;
        this.position = position;
        this.spawnPosition = new Position(
            position.getX(),
            position.getY(),
            position.getZ()
        );

        this.targetPosition = new Position(
            position.getX(),
            position.getY(),
            position.getZ()
        );
        this.movementRadius = movementRadius;
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public Position position() {
        return position;
    }

    public int getMovementRadius() {
        return movementRadius;
    }

    public void setMovementRadius(int movementRadius) {
        this.movementRadius = movementRadius;
    }

    @Override
    public void tick() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        seconds += deltaTime;
        float dx = targetPosition.getX() - position.getX();
        float dy = targetPosition.getY() - position.getY();

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 1) {
            float movement = Math.min(MOVEMENT_SPEED * deltaTime, distance);

            position.setX(position.getX() + dx / distance * movement);
            position.setY(position.getY() + dy / distance * movement);
        }

        if (seconds >= 5) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float targetDistance = MathUtils.random(0f, movementRadius);

            float x = spawnPosition.getX() + MathUtils.cos(angle) * targetDistance;
            float y = spawnPosition.getY() + MathUtils.sin(angle) * targetDistance;

            targetPosition.setX(x);
            targetPosition.setY(y);
            seconds = 0;
        }
    }

}
