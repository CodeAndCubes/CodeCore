package com.mrleonardos.codecore.internal.permission.adapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.mrleonardos.codecore.api.adapter.RoleAdapter;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.adapter.RoleOwnerKind;
import com.mrleonardos.codecore.api.adapter.RoleServices;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.service.PermissionService;
import com.mrleonardos.codecore.internal.permission.PermissionRole;

/**
 * Заявка на права от LuckPerms.
 *
 * <p>
 * Отражение живёт внутри заявки и срабатывает один раз: {@code available()} спрашивают у всех заявок
 * роли, а {@code create()} только у победителя, поэтому найденный мост запоминается.
 */
public final class LuckPermsAdapter implements RoleAdapter {

    /** Имя, которое админ пишет в {@code [owners] permissions}. */
    public static final String NAME = "luckperms";

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
        return new HashSet<>(Arrays.asList(PermissionRole.HAS, PermissionRole.GROUP, PermissionRole.META));
    }

    @Override
    public RoleServices create() {
        return RoleServices.builder()
            .add(PermissionService.class, probe())
            .build();
    }

    private PermissionService probe() {
        if (!probed) {
            service = LuckPermsPermissions.create();
            probed = true;
        }
        return service;
    }
}
