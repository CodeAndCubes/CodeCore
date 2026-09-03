package com.mrleonardos.codecore.internal.permission.adapter;

import java.lang.reflect.Method;
import java.util.UUID;

import com.mrleonardos.codecore.api.command.CommandSender;
import com.mrleonardos.codecore.api.command.SenderKind;
import com.mrleonardos.codecore.api.service.PermissionService;
import com.mrleonardos.codecore.platform.PlayerNames;

/**
 * Права из ядра UltraMine.
 *
 * <p>
 * Используются строковые методы {@code hasGlobally} и {@code getMeta}: они принимают ник и не зависят от
 * того, какими именами собран сервер. Спрашивать по идентификатору UltraMine не умеет, поэтому ник берётся
 * из {@link PlayerNames}: сначала у тех, кто в сети, потом из кэша профилей сервера.
 */
final class UltraMinePermissions implements PermissionService {

    private static final String HANDLER = "org.ultramine.server.PermissionHandler";
    private static final String GET_INSTANCE = "getInstance";
    private static final String HAS_GLOBALLY = "hasGlobally";
    private static final String GET_META = "getMeta";
    private static final String GROUP_META = "group";
    private static final String GLOBAL_WORLD = "";
    private static final String NO_GROUP = "";

    private final Object handler;
    private final Method hasGlobally;
    private final Method getMeta;

    private UltraMinePermissions(Object handler, Method hasGlobally, Method getMeta) {
        this.handler = handler;
        this.hasGlobally = hasGlobally;
        this.getMeta = getMeta;
    }

    /** Адаптер или {@code null}, если UltraMine на этом сервере нет. */
    static UltraMinePermissions create() {
        Class<?> type = Reflected.type(HANDLER);
        if (type == null) {
            return null;
        }

        Method instance = Reflected.method(type, GET_INSTANCE);
        Method has = Reflected.method(type, HAS_GLOBALLY, String.class, String.class);
        Method meta = Reflected.method(type, GET_META, String.class, String.class, String.class);
        if (instance == null || has == null || meta == null) {
            return null;
        }

        Object handler = Reflected.call(instance, null);
        return handler == null ? null : new UltraMinePermissions(handler, has, meta);
    }

    @Override
    public boolean has(UUID player, String node) {
        String name = nameOf(player);
        return name != null && Reflected.flag(Reflected.call(hasGlobally, handler, name, node));
    }

    @Override
    public boolean has(CommandSender sender, String node) {
        if (sender.kind() != SenderKind.PLAYER) {
            return true;
        }
        return Reflected.flag(Reflected.call(hasGlobally, handler, sender.name(), node));
    }

    @Override
    public String group(UUID player) {
        return meta(player, GROUP_META, NO_GROUP);
    }

    @Override
    public String meta(UUID player, String key, String fallback) {
        String name = nameOf(player);
        if (name == null) {
            return fallback;
        }
        return Reflected.text(Reflected.call(getMeta, handler, GLOBAL_WORLD, name, key), fallback);
    }

    private static String nameOf(UUID player) {
        return PlayerNames.byId(player);
    }
}
