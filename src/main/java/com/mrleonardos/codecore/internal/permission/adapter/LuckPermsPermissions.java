package com.mrleonardos.codecore.internal.permission.adapter;

import java.lang.reflect.Method;
import java.util.UUID;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrleonardos.codecore.api.service.PermissionService;

/**
 * Права из LuckPerms.
 *
 * <p>
 * Работа идёт через кэш прав пользователя: {@code getCachedData} отвечает сразу и без обращения к базе, а
 * значит его можно спрашивать в обработке сообщения чата. Незагруженный пользователь считается
 * бесправным: подгружать его синхронно здесь нельзя.
 *
 * <p>
 * Методы ищутся отражением один раз на класс получателя и дальше берутся из {@link ReflectedMethod}. Иначе
 * шестьдесят игроков в чате давали бы 360 вызовов {@code getMethod} на одно сообщение в главном потоке.
 */
final class LuckPermsPermissions implements PermissionService {

    private static final String PROVIDER = "net.luckperms.api.LuckPermsProvider";
    private static final String GET = "get";
    private static final String GET_USER_MANAGER = "getUserManager";
    private static final String GET_USER = "getUser";
    private static final String GET_CACHED_DATA = "getCachedData";
    private static final String GET_PERMISSION_DATA = "getPermissionData";
    private static final String CHECK_PERMISSION = "checkPermission";
    private static final String AS_BOOLEAN = "asBoolean";
    private static final String GET_META_DATA = "getMetaData";
    private static final String GET_META_VALUE = "getMetaValue";
    private static final String GET_PRIMARY_GROUP = "getPrimaryGroup";
    private static final String NO_GROUP = "";

    private final ReflectedMethod userManager = new ReflectedMethod(GET_USER_MANAGER);
    private final ReflectedMethod user = new ReflectedMethod(GET_USER, UUID.class);
    private final ReflectedMethod cachedData = new ReflectedMethod(GET_CACHED_DATA);
    private final ReflectedMethod permissionData = new ReflectedMethod(GET_PERMISSION_DATA);
    private final ReflectedMethod checkPermission = new ReflectedMethod(CHECK_PERMISSION, String.class);
    private final ReflectedMethod asBoolean = new ReflectedMethod(AS_BOOLEAN);
    private final ReflectedMethod metaData = new ReflectedMethod(GET_META_DATA);
    private final ReflectedMethod metaValue = new ReflectedMethod(GET_META_VALUE, String.class);
    private final ReflectedMethod primaryGroup = new ReflectedMethod(GET_PRIMARY_GROUP);

    private final Object luckPerms;

    private LuckPermsPermissions(Object luckPerms) {
        this.luckPerms = luckPerms;
    }

    /** Адаптер или {@code null}, если LuckPerms на этом сервере нет. */
    static LuckPermsPermissions create() {
        Class<?> provider = Reflected.type(PROVIDER);
        if (provider == null) {
            return null;
        }
        Method get = Reflected.method(provider, GET);
        if (get == null) {
            return null;
        }
        Object instance = Reflected.call(get, null);
        return instance == null ? null : new LuckPermsPermissions(instance);
    }

    @Override
    public boolean has(UUID player, String node) {
        Object permissions = permissionDataOf(player);
        if (permissions == null) {
            return false;
        }
        Object tristate = call(checkPermission, permissions, node);
        if (tristate == null) {
            return false;
        }
        return Reflected.flag(call(asBoolean, tristate));
    }

    @Override
    public boolean has(ICommandSender sender, String node) {
        if (!(sender instanceof EntityPlayerMP)) {
            return true;
        }
        return has(((EntityPlayerMP) sender).getUniqueID(), node);
    }

    @Override
    public String group(UUID player) {
        Object found = userOf(player);
        if (found == null) {
            return NO_GROUP;
        }
        return Reflected.text(call(primaryGroup, found), NO_GROUP);
    }

    @Override
    public String meta(UUID player, String key, String fallback) {
        Object cached = cachedDataOf(player);
        if (cached == null) {
            return fallback;
        }
        Object meta = call(metaData, cached);
        if (meta == null) {
            return fallback;
        }
        return Reflected.text(call(metaValue, meta, key), fallback);
    }

    private Object permissionDataOf(UUID player) {
        Object cached = cachedDataOf(player);
        return cached == null ? null : call(permissionData, cached);
    }

    private Object cachedDataOf(UUID player) {
        Object found = userOf(player);
        return found == null ? null : call(cachedData, found);
    }

    private Object userOf(UUID player) {
        Object manager = call(userManager, luckPerms);
        return manager == null ? null : call(user, manager, player);
    }

    private static Object call(ReflectedMethod method, Object target, Object... arguments) {
        Method resolved = method.of(target.getClass());
        return resolved == null ? null : Reflected.call(resolved, target, arguments);
    }
}
