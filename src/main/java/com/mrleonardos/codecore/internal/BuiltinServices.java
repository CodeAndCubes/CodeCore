package com.mrleonardos.codecore.internal;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.CoreConstants;
import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.api.service.PermissionService;
import com.mrleonardos.codecore.api.service.ServicePriority;
import com.mrleonardos.codecore.internal.permission.JsonPermissionService;
import com.mrleonardos.codecore.internal.permission.OperatorLookup;
import com.mrleonardos.codecore.internal.permission.PermissionFile;
import com.mrleonardos.codecore.internal.permission.PermissionKeys;
import com.mrleonardos.codecore.internal.permission.adapter.PermissionAdapters;

/**
 * Встроенные реализации сервисов ядра.
 *
 * <p>
 * Все они регистрируются с наименьшим весом: специализированный мод вытеснит их, ничего не ломая.
 */
public final class BuiltinServices {

    private BuiltinServices() {}

    public static void register(Logger log) {
        ConfigFile<PermissionFile> permissions = CodeApi.configs()
            .open(permissionSpec());
        CodeApi.services()
            .register(
                PermissionService.class,
                new JsonPermissionService(permissions, OperatorLookup::isOperator),
                ServicePriority.BUILTIN);
        PermissionAdapters.register(log);
    }

    private static ConfigSpec<PermissionFile> permissionSpec() {
        return ConfigSpec.of(CoreConstants.MODID, PermissionKeys.FILE_NAME, PermissionFile.class)
            .scope(ConfigScope.SETTINGS)
            .defaults(PermissionFile::defaults)
            .validator(PermissionFile::normalize)
            .build();
    }
}
