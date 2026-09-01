package com.mrleonardos.codecore.internal.permission;

import com.mrleonardos.codecore.api.adapter.RoleCapability;
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
 * не работает.
 */
public final class PermissionRole {

    /** Ответ на вопрос, есть ли у игрока право. */
    public static final RoleCapability HAS = RoleCapability.of("has");

    /** Имя группы игрока. */
    public static final RoleCapability GROUP = RoleCapability.of("group");

    /** Значения меты: префикс, лимит домов, что угодно ещё. */
    public static final RoleCapability META = RoleCapability.of("meta");

    /** Права, действующие только в мире, режиме или другом контексте. */
    public static final RoleCapability CONTEXTS = RoleCapability.of("contexts");

    /** Выдачи на срок, которые снимаются сами. */
    public static final RoleCapability EXPIRY = RoleCapability.of("expiry");

    /** Треки: порядок групп для повышения и понижения. */
    public static final RoleCapability TRACKS = RoleCapability.of("tracks");

    /** Чем закрыта роль, оставшаяся ничьей: строка уходит в стартовую сводку. */
    private static final String OPERATORS = "permission checks fall back to the server operator list";

    private PermissionRole() {}

    public static RoleSpec spec() {
        return RoleSpec.of(ConfigRoles.PERMISSIONS)
            .capabilities(HAS, GROUP, META, CONTEXTS, EXPIRY, TRACKS)
            .services(PermissionService.class)
            .fallback(RoleFallback.of(OPERATORS, PermissionRole::operators, HAS))
            .build();
    }

    private static RoleServices operators() {
        return RoleServices.builder()
            .add(PermissionService.class, new OperatorPermissions(OperatorLookup::isOperator))
            .build();
    }
}
