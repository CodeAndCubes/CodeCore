package com.mrleonardos.codecore.internal.permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.mrleonardos.codecore.api.adapter.PermissionCapabilities;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.service.PermissionService;

/** Контракт роли прав на встроенной реализации ядра. */
class BuiltinPermissionsContractTest extends PermissionServiceContract {

    private final CoreGroupsFile groups = CoreGroupsFile.defaults();
    private final PermissionsSection section = new PermissionsSection();
    private final PermissionService service = new GroupsPermissionService(
        new StaticConfig<>(groups),
        new StaticConfig<>(section),
        player -> false);

    @Override
    protected PermissionService service() {
        return service;
    }

    @Override
    protected Set<RoleCapability> capabilities() {
        return new HashSet<>(
            Arrays.asList(PermissionCapabilities.HAS, PermissionCapabilities.GROUP, PermissionCapabilities.META));
    }

    @Override
    protected void grant(UUID player, String node) {
        entry(player).nodes.add(node);
    }

    @Override
    protected void assign(UUID player, String group) {
        entry(player).group = group;
        groups.groups.put(group, new GroupEntry());
    }

    @Override
    protected void meta(UUID player, String key, String value) {
        entry(player).meta.put(key, value);
    }

    private PlayerEntry entry(UUID player) {
        return groups.players.computeIfAbsent(player.toString(), id -> new PlayerEntry());
    }
}
