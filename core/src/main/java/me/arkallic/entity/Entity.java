package me.arkallic.entity;

import me.arkallic.model.Position;

public interface Entity {
    String name();
    Position position();
    void tick();
}
