package com.mrleonardos.codecore.internal.permission;

import com.mrleonardos.codecore.api.adapter.PermissionCapabilities;
import com.mrleonardos.codecore.api.adapter.RoleFallback;
import com.mrleonardos.codecore.api.adapter.RoleServices;
import com.mrleonardos.codecore.api.adapter.RoleSpec;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.service.PermissionService;

/**
 * Роль прав: её объявляет ядро, потому что реализация по умолчанию живёт здесь же.
 *
 * <p>
 * Перечень умений полный, а каждая заявка называет своё подмножество: разница и есть то, что на сервере
 * не работает. Сами имена умений лежат в {@link PermissionCapabilities}, чтобы их мог назвать и тот, у
 * кого на руках только api-джар.
 */
public final class PermissionRole {

    /** Чем закрыта роль, оставшаяся ничьей: строка уходит в стартовую сводку. */
    private static final String OPERATORS = "permission checks fall back to the server operator list";

    private PermissionRole() {}

    public static RoleSpec spec() {
        return RoleSpec.of(ConfigRoles.PERMISSIONS)
            .capabilities(
                PermissionCapabilities.HAS,
                PermissionCapabilities.GROUP,
                PermissionCapabilities.META,
                PermissionCapabilities.CONTEXTS,
                PermissionCapabilities.EXPIRY,
                PermissionCapabilities.TRACKS)
            .services(PermissionService.class)
            .fallback(RoleFallback.of(OPERATORS, PermissionRole::operators, PermissionCapabilities.HAS))
            .build();
    }

    private static RoleServices operators() {
        return RoleServices.builder()
            .add(PermissionService.class, new OperatorPermissions(OperatorLookup::isOperator))
            .build();
    }
}
