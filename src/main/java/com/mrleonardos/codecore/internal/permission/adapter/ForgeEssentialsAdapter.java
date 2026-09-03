package com.mrleonardos.codecore.internal.permission.adapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.mrleonardos.codecore.api.adapter.PermissionCapabilities;
import com.mrleonardos.codecore.api.adapter.RoleAdapter;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.adapter.RoleOwnerKind;
import com.mrleonardos.codecore.api.adapter.RoleServices;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.service.PermissionService;

/**
 * Заявка на права от ForgeEssentials.
 *
 * <p>
 * Заявка закрывает {@code has}, {@code group} и {@code meta}. Срока жизни узла в api ForgeEssentials нет вовсе,
 * треков нет, а контекст у него это точка, область или зона, куда пара ключ-значение не ложится. Поэтому
 * {@code expiry}, {@code tracks} и {@code contexts} остаются в перечне недоступного роли, и подсистемы, которые на
 * них держатся, не регистрируются.
 *
 * <p>
 * {@code available()} отвечает по наличию классов FE, а не по заполненности {@code APIRegistry.perms}: роли решаются
 * в конце постинициализации, и модуль прав FE к этому времени может ещё не подняться. Заполненность поля мост
 * проверяет перед каждым вопросом сам.
 */
public final class ForgeEssentialsAdapter implements RoleAdapter {

    /** Имя, которое админ пишет в {@code [owners] permissions}. */
    public static final String NAME = "forgeessentials";

    private PermissionService service;
    private boolean probed;

    @Override
    public String role() {
        return ConfigRoles.PERMISSIONS;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public RoleOwnerKind kind() {
        return RoleOwnerKind.ADAPTER;
    }

    @Override
    public boolean available() {
        return probe() != null;
    }

    @Override
    public Set<RoleCapability> capabilities() {
        return new HashSet<>(
            Arrays.asList(PermissionCapabilities.HAS, PermissionCapabilities.GROUP, PermissionCapabilities.META));
    }

    @Override
    public RoleServices create() {
        return RoleServices.builder()
            .add(PermissionService.class, probe())
            .build();
    }

    private PermissionService probe() {
        if (!probed) {
            service = ForgeEssentialsPermissions.create();
            probed = true;
        }
        return service;
    }
}
