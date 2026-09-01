package org.ultramine.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Подставное ядро прав UltraMine для проверки адаптера.
 *
 * <p>
 * Повторяет только то, чем пользуется адаптер: получение экземпляра, проверку по нику и чтение меты.
 * Настоящее ядро в сборку не входит и входить не должно.
 */
public final class PermissionHandler {

    private static final PermissionHandler INSTANCE = new PermissionHandler();

    private final Map<String, Boolean> permissions = new HashMap<>();
    private final Map<String, String> meta = new HashMap<>();

    private PermissionHandler() {}

    public static PermissionHandler getInstance() {
        return INSTANCE;
    }

    public boolean hasGlobally(String player, String permission) {
        return permissions.containsKey(key(player, permission));
    }

    public String getMeta(String world, String player, String key) {
        return meta.get(key(player, key));
    }

    /** Выдать право в подставном ядре. */
    public static void allow(String player, String permission) {
        INSTANCE.permissions.put(key(player, permission), Boolean.TRUE);
    }

    /** Записать мету в подставном ядре. */
    public static void meta(String player, String key, String value) {
        INSTANCE.meta.put(key(player, key), value);
    }

    private static String key(String player, String suffix) {
        return player + "/" + suffix;
    }
}
