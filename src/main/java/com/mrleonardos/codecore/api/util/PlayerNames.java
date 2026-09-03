package com.mrleonardos.codecore.api.util;

import java.util.UUID;

/**
 * Ник по идентификатору игрока.
 *
 * <p>
 * Тело переехало в {@link com.mrleonardos.codecore.platform.PlayerNames}: класс работает с кэшем профилей
 * сервера, и в api ему не место. Здесь остался переходник, чтобы соседние моды переставили импорт своим
 * проходом. Класс уходит вместе со сносом старых подписей.
 */
public final class PlayerNames {

    private PlayerNames() {}

    /** Ник игрока или {@code null}, если сервер о нём ничего не знает. */
    public static String byId(UUID player) {
        return com.mrleonardos.codecore.platform.PlayerNames.byId(player);
    }

    /** Идентификатор игрока по нику или {@code null}, если сервер о нём ничего не знает. */
    public static UUID idByName(String name) {
        return com.mrleonardos.codecore.platform.PlayerNames.idByName(name);
    }
}
