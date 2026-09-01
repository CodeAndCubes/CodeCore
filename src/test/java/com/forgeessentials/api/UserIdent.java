package com.forgeessentials.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Подставной UserIdent.
 *
 * <p>
 * Настоящий FE выдаёт на игрока один объект и помнит его между вызовами, поэтому здесь та же карта по
 * идентификатору. Ник приходит вторым аргументом и может отсутствовать: сервер не всегда знает имя оффлайн-игрока, а
 * искать подставное хранилище всё равно будет по идентификатору.
 */
public final class UserIdent {

    private static final Map<UUID, UserIdent> KNOWN = new HashMap<>();

    private final UUID uuid;

    private UserIdent(UUID uuid) {
        this.uuid = uuid;
    }

    public static synchronized UserIdent get(UUID uuid, String username) {
        return KNOWN.computeIfAbsent(uuid, UserIdent::new);
    }

    public UUID uuid() {
        return uuid;
    }
}
