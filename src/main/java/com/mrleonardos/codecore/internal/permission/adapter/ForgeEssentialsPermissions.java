package com.mrleonardos.codecore.internal.permission.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrleonardos.codecore.api.service.PermissionService;
import com.mrleonardos.codecore.platform.PlayerNames;

/**
 * Права из ForgeEssentials.
 *
 * <p>
 * Точка входа это статическое поле {@code APIRegistry.perms}. Оно пустует, пока модуль прав FE не поднялся, поэтому
 * читается перед каждым вопросом, а не один раз при создании моста: иначе ранний вопрос запомнил бы пустоту навсегда,
 * и права молча не работали бы весь запуск.
 *
 * <p>
 * Игрок собирается фабрикой {@code UserIdent.get(uuid, ник)}: ник берётся из {@link PlayerNames} и нужен FE, когда он
 * видит игрока впервые. Оффлайн-игрок так тоже находится.
 *
 * <p>
 * Компилироваться против FE нельзя: его {@code IPermissionsHelper} наследует
 * {@code net.minecraftforge.permission.IPermissionProvider}, которого в чистом Forge 10.13.4 нет, его добавляет
 * препролоадер FE. Поэтому только отражение.
 */
final class ForgeEssentialsPermissions implements PermissionService {

    private static final String REGISTRY = "com.forgeessentials.api.APIRegistry";
    private static final String USER_IDENT = "com.forgeessentials.api.UserIdent";
    private static final String PERMS = "perms";
    private static final String GET = "get";
    private static final String CHECK_USER_PERMISSION = "checkUserPermission";
    private static final String GET_PRIMARY_GROUP = "getPrimaryGroup";
    private static final String GET_USER_PERMISSION_PROPERTY = "getUserPermissionProperty";
    private static final String NO_GROUP = "";

    private final ReflectedMethod checkUserPermission;
    private final ReflectedMethod primaryGroup;
    private final ReflectedMethod userProperty;

    private final Field perms;
    private final Method ident;

    private ForgeEssentialsPermissions(Field perms, Method ident, Class<?> userIdent) {
        this.perms = perms;
        this.ident = ident;
        this.checkUserPermission = new ReflectedMethod(CHECK_USER_PERMISSION, userIdent, String.class);
        this.primaryGroup = new ReflectedMethod(GET_PRIMARY_GROUP, userIdent);
        this.userProperty = new ReflectedMethod(GET_USER_PERMISSION_PROPERTY, userIdent, String.class);
    }

    /** Мост или {@code null}, если ForgeEssentials на этом сервере нет. */
    static ForgeEssentialsPermissions create() {
        Class<?> registry = Reflected.type(REGISTRY);
        Class<?> userIdent = Reflected.type(USER_IDENT);
        if (registry == null || userIdent == null) {
            return null;
        }

        Field perms = Reflected.field(registry, PERMS);
        Method get = Reflected.method(userIdent, GET, UUID.class, String.class);
        if (perms == null || get == null) {
            return null;
        }
        return new ForgeEssentialsPermissions(perms, get, userIdent);
    }

    @Override
    public boolean has(UUID player, String node) {
        return Reflected.flag(ask(checkUserPermission, player, node));
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
        return Reflected.text(ask(primaryGroup, player), NO_GROUP);
    }

    @Override
    public String meta(UUID player, String key, String fallback) {
        return Reflected.text(ask(userProperty, player, key), fallback);
    }

    private Object ask(ReflectedMethod method, UUID player, Object... arguments) {
        Object helper = Reflected.value(perms);
        if (helper == null) {
            return null;
        }

        Object user = Reflected.call(ident, null, player, PlayerNames.byId(player));
        if (user == null) {
            return null;
        }

        Method resolved = method.of(helper.getClass());
        if (resolved == null) {
            return null;
        }

        Object[] call = new Object[arguments.length + 1];
        call[0] = user;
        System.arraycopy(arguments, 0, call, 1, arguments.length);
        return Reflected.call(resolved, helper, call);
    }
}
