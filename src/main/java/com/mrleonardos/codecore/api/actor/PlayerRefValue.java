package com.mrleonardos.codecore.api.actor;

import java.util.Objects;
import java.util.UUID;

final class PlayerRefValue implements PlayerRef {

    private final UUID id;
    private final String name;

    PlayerRefValue(UUID id, String name) {
        this.id = Objects.requireNonNull(id, "player id");
        this.name = name == null ? "" : name;
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PlayerRef && id.equals(((PlayerRef) other).id());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name.isEmpty() ? id.toString() : name + " (" + id + ")";
    }
}
