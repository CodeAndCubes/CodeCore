package com.mrleonardos.codecore.internal.permission.adapter;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.mrleonardos.codecore.api.adapter.RoleAdapter;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.service.PermissionService;
import com.mrleonardos.codecore.internal.permission.PermissionServiceContract;

/**
 * Контракт роли прав на мосте к ForgeEssentials.
 *
 * <p>
 * Чужой api подставлен классами с теми же именами пакетов, полями и сигнатурами: отражение моста находит их так же,
 * как нашло бы настоящие. Состояние наводится через api FE, а не мимо него, поэтому после каждой правки идёт
 * {@code setDirty(true)}, как того требует FE от любого, кто пишет.
 */
class ForgeEssentialsContractTest extends PermissionServiceContract {

    private final RoleAdapter adapter = new ForgeEssentialsAdapter();
    private final PermissionService service = adapter.create()
        .find(PermissionService.class);
    private final FakeForgeEssentials perms = new FakeForgeEssentials();

    @BeforeEach
    void permissionModuleIsUp() {
        APIRegistry.perms = perms;
    }

    @AfterEach
    void permissionModuleIsGone() {
        APIRegistry.perms = null;
    }

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
        perms.setPlayerPermission(UserIdent.get(player, null), node, true);
        perms.setDirty(true);
    }

    @Override
    protected void assign(UUID player, String group) {
        if (!perms.groupExists(group)) {
            perms.createGroup(group);
        }
        perms.addPlayerToGroup(UserIdent.get(player, null), group);
        perms.setDirty(true);
    }

    @Override
    protected void meta(UUID player, String key, String value) {
        perms.setPlayerPermissionProperty(UserIdent.get(player, null), key, value);
        perms.setDirty(true);
    }
}
