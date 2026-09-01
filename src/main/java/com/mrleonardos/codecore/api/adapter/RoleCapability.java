package com.mrleonardos.codecore.api.adapter;

/**
 * Умение роли: то, что владелец роли либо закрывает целиком, либо не закрывает вовсе.
 *
 * <p>
 * Перечень умений объявляет тот, у кого есть реализация роли по умолчанию, а каждая заявка называет своё
 * подмножество. Разница между перечнем роли и подмножеством владельца это и есть то, что на сервере не
 * работает: команды и подсистемы, которые держатся на недостающем умении, не регистрируются.
 */
public final class RoleCapability {

    private final String name;

    private RoleCapability(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Capability name must not be empty");
        }
        this.name = name;
    }

    /** Умение с этим именем: {@code has}, {@code tracks}, {@code transfer}. */
    public static RoleCapability of(String name) {
        return new RoleCapability(name);
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RoleCapability && name.equals(((RoleCapability) other).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
