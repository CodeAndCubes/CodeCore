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

/** Заявка на права от ядра UltraMine. Отражение живёт внутри заявки и срабатывает один раз. */
public final class UltraMineAdapter implements RoleAdapter {

    /** Имя, которое админ пишет в {@code [owners] permissions}. */
    public static final String NAME = "ultramine";

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
            service = UltraMinePermissions.create();
            probed = true;
        }
        return service;
    }
}
