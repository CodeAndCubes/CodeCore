package com.mrleonardos.codecore.internal.client.avatar;

import java.util.Locale;
import java.util.UUID;

/** Подстановки, которые понимают шаблоны адресов аватаров. */
final class AvatarPlaceholders {

    private static final String NAME = "{name}";
    private static final String UUID_DASHED = "{uuid}";
    private static final String UUID_PLAIN = "{uuid_nodash}";
    private static final String DASH = "-";

    private AvatarPlaceholders() {}

    static String apply(String template, UUID playerId, String playerName) {
        String id = playerId == null ? "" : playerId.toString();
        return template.replace(NAME, playerName == null ? "" : playerName)
            .replace(UUID_DASHED, id)
            .replace(UUID_PLAIN, id.replace(DASH, ""))
            .trim();
    }

    static String fileName(String playerName) {
        return playerName == null ? "" : playerName.toLowerCase(Locale.ROOT);
    }
}
