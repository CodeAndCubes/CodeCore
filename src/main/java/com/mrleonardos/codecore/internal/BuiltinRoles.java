package com.mrleonardos.codecore.internal;

import com.mrleonardos.codecore.api.adapter.AdapterRegistry;
import com.mrleonardos.codecore.internal.permission.BuiltinPermissions;
import com.mrleonardos.codecore.internal.permission.PermissionRole;
import com.mrleonardos.codecore.internal.permission.adapter.LuckPermsAdapter;
import com.mrleonardos.codecore.internal.permission.adapter.UltraMineAdapter;

/**
 * Роли и заявки, которые приносит само ядро.
 *
 * <p>
 * Права объявляет ядро, потому что реализация по умолчанию живёт здесь же. Мосты к чужим модам подаются
 * заявками рядом со встроенной: сами по себе они не подключаются, их выбирают именем в {@code [owners]}.
 */
public final class BuiltinRoles {

    private BuiltinRoles() {}

    public static void install(AdapterRegistry adapters, CoreSections sections) {
        adapters.declareRole(PermissionRole.spec());
        adapters.offer(new BuiltinPermissions(sections.permissions()));
        adapters.offer(new LuckPermsAdapter());
        adapters.offer(new UltraMineAdapter());
    }
}
