package com.mrleonardos.codecore.internal.permission.adapter;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.service.PermissionService;
import com.mrleonardos.codecore.api.service.ServicePriority;

/**
 * Подключает права из специализированных модов, если они есть на сервере.
 *
 * <p>
 * Адаптер встаёт весом {@link ServicePriority#ADDON} и вытесняет встроенную реализацию на json: сервер, где
 * уже настроены группы, не должен настраивать их второй раз. Порядок проверки идёт от более специфичного
 * к общему: LuckPerms ставят ради самих прав, а UltraMine ради ядра сервера целиком.
 */
public final class PermissionAdapters {

    private PermissionAdapters() {}

    public static void register(Logger log) {
        PermissionService adapter = LuckPermsPermissions.create();
        String name = "LuckPerms";

        if (adapter == null) {
            adapter = UltraMinePermissions.create();
            name = "UltraMine";
        }
        if (adapter == null) {
            return;
        }

        CodeApi.services()
            .register(PermissionService.class, adapter, ServicePriority.ADDON);
        log.info("Permissions are taken from {}", name);
    }
}
