package com.forgeessentials.api;

import com.forgeessentials.api.permissions.IPermissionsHelper;

/**
 * Подставная точка входа ForgeEssentials.
 *
 * <p>
 * Настоящий FE держит здесь публичные статические поля и заполняет их по мере того, как поднимаются его модули. Мост
 * ищет поле по имени, поэтому для проверки хватает поля с тем же именем и типом, а пустое поле это состояние сервера
 * до того, как модуль прав FE поднялся.
 */
public final class APIRegistry {

    public static IPermissionsHelper perms;

    private APIRegistry() {}
}
