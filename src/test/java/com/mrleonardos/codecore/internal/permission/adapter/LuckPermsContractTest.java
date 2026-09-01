package com.mrleonardos.codecore.internal.permission.adapter;

import java.util.Set;
import java.util.UUID;

import net.luckperms.api.LuckPermsProvider;

import com.mrleonardos.codecore.api.adapter.RoleAdapter;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.service.PermissionService;
import com.mrleonardos.codecore.internal.permission.PermissionServiceContract;

/**
 * Контракт роли прав на мосте к LuckPerms.
 *
 * <p>
 * Чужой api подставлен поддельным классом в тестовом classpath: отражение моста находит его так же, как
 * нашло бы настоящий.
 */
class LuckPermsContractTest extends PermissionServiceContract {

    private final RoleAdapter adapter = new LuckPermsAdapter();
    private final PermissionService service = adapter.create()
        .find(PermissionService.class);

    @Override
    protected PermissionService service() {
        return service;
    }

    @Override
    protected Set<RoleCapability> capabilities() {
        return adapter.capabilities();
    }

    @Override
    protected void grant(UUID player, String node) {
        LuckPermsProvider.user(player)
            .allow(node);
    }

    @Override
    protected void assign(UUID player, String group) {
        LuckPermsProvider.user(player)
            .primaryGroup(group);
    }

    @Override
    protected void meta(UUID player, String key, String value) {
        LuckPermsProvider.user(player)
            .meta(key, value);
    }
}
